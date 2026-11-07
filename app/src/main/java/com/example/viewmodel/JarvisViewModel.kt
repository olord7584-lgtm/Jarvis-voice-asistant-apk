package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.actions.BatteryInfo
import com.example.actions.DeviceActionController
import com.example.actions.SystemMemoryInfo
import com.example.ai.ActionType
import com.example.ai.JarvisAiService
import com.example.data.CommandLogEntity
import com.example.data.JarvisDatabase
import com.example.data.JarvisNoteEntity
import com.example.voice.SpeechManager
import com.example.voice.TtsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AssistantMode {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING
}

data class TelemetryData(
    val batteryPct: Int = 100,
    val isCharging: Boolean = false,
    val batteryHealth: String = "Nominal",
    val freeMemMB: Long = 2048,
    val totalMemMB: Long = 4096,
    val memoryUsedPct: Int = 50,
    val networkStatus: String = "Satellite Uplink Online",
    val isTorchActive: Boolean = false
)

data class ActionNotification(
    val message: String,
    val isSuccess: Boolean = true
)

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val db = JarvisDatabase.getDatabase(application)
    private val dao = db.jarvisDao()

    val speechManager = SpeechManager(application)
    val ttsManager = TtsManager(application)
    val deviceActions = DeviceActionController(application)
    private val aiService = JarvisAiService()

    private val _mode = MutableStateFlow(AssistantMode.IDLE)
    val mode: StateFlow<AssistantMode> = _mode.asStateFlow()

    private val _latestCommand = MutableStateFlow("Tap the Arc Reactor or Mic to issue a command.")
    val latestCommand: StateFlow<String> = _latestCommand.asStateFlow()

    private val _latestResponse = MutableStateFlow("Systems online, sir. J.A.R.V.I.S. Mark VII awaiting instructions.")
    val latestResponse: StateFlow<String> = _latestResponse.asStateFlow()

    private val _actionNotification = MutableStateFlow<ActionNotification?>(null)
    val actionNotification: StateFlow<ActionNotification?> = _actionNotification.asStateFlow()

    private val _telemetry = MutableStateFlow(TelemetryData())
    val telemetry: StateFlow<TelemetryData> = _telemetry.asStateFlow()

    private val _currentTab = MutableStateFlow("HUD")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    val commandLogs = dao.getRecentLogs().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val savedNotes = dao.getAllNotes().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    init {
        refreshTelemetry()
        // Greet on startup
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            ttsManager.speak("Systems operational, sir. JARVIS awaiting your command.")
        }
    }

    fun selectTab(tab: String) {
        _currentTab.value = tab
    }

    fun refreshTelemetry() {
        val bat = deviceActions.getBatteryInfo()
        val mem = deviceActions.getMemoryInfo()
        val net = deviceActions.getNetworkStatus()
        _telemetry.value = TelemetryData(
            batteryPct = bat.percentage,
            isCharging = bat.isCharging,
            batteryHealth = bat.health,
            freeMemMB = mem.freeMemMB,
            totalMemMB = mem.totalMemMB,
            memoryUsedPct = mem.usedPercentage,
            networkStatus = net,
            isTorchActive = _telemetry.value.isTorchActive
        )
    }

    fun toggleVoiceListening() {
        if (speechManager.isListening.value) {
            speechManager.stopListening()
            _mode.value = AssistantMode.IDLE
        } else {
            ttsManager.stop()
            _mode.value = AssistantMode.LISTENING
            speechManager.startListening { transcribedText ->
                processVoiceCommand(transcribedText)
            }
        }
    }

    fun stopSpeaking() {
        ttsManager.stop()
        _mode.value = AssistantMode.IDLE
    }

    fun toggleMute() {
        ttsManager.toggleMute()
    }

    fun processVoiceCommand(command: String) {
        if (command.isBlank()) {
            _mode.value = AssistantMode.IDLE
            return
        }

        _latestCommand.value = command
        _mode.value = AssistantMode.PROCESSING
        _actionNotification.value = null

        viewModelScope.launch {
            val parsed = aiService.classifyIntent(command)
            when (parsed.actionType) {
                ActionType.FLASHLIGHT_ON -> {
                    val res = deviceActions.setTorch(true)
                    val reply = if (res.isSuccess) {
                        _telemetry.value = _telemetry.value.copy(isTorchActive = true)
                        _actionNotification.value = ActionNotification("Illumination Subsystem: Online")
                        "Illumination subsystem active, sir."
                    } else {
                        "Flashlight unit unavailable on this hardware configuration."
                    }
                    finalizeResponse(command, reply, "FLASHLIGHT")
                }

                ActionType.FLASHLIGHT_OFF -> {
                    val res = deviceActions.setTorch(false)
                    val reply = if (res.isSuccess) {
                        _telemetry.value = _telemetry.value.copy(isTorchActive = false)
                        _actionNotification.value = ActionNotification("Illumination Subsystem: Offline")
                        "Illumination offline, sir."
                    } else {
                        "Flashlight unit unavailable."
                    }
                    finalizeResponse(command, reply, "FLASHLIGHT")
                }

                ActionType.FLASHLIGHT_TOGGLE -> {
                    val res = deviceActions.toggleTorch()
                    val newState = res.getOrDefault(false)
                    _telemetry.value = _telemetry.value.copy(isTorchActive = newState)
                    val reply = if (newState) "Flashlight engaged, sir." else "Flashlight deactivated, sir."
                    _actionNotification.value = ActionNotification(if (newState) "Torch Engaged" else "Torch Disengaged")
                    finalizeResponse(command, reply, "FLASHLIGHT")
                }

                ActionType.CHECK_BATTERY -> {
                    val bat = deviceActions.getBatteryInfo()
                    refreshTelemetry()
                    val reply = "Battery reserve is at ${bat.percentage} percent. State: ${if (bat.isCharging) "Charging via Arc Reactor connection" else "Running on internal cells, status " + bat.health}."
                    _actionNotification.value = ActionNotification("Power: ${bat.percentage}%")
                    finalizeResponse(command, reply, "BATTERY")
                }

                ActionType.SYSTEM_DIAGNOSTICS -> {
                    val mem = deviceActions.getMemoryInfo()
                    val bat = deviceActions.getBatteryInfo()
                    val net = deviceActions.getNetworkStatus()
                    refreshTelemetry()
                    val reply = "Mark VII telemetry nominal. Memory load at ${mem.usedPercentage}%, with ${mem.freeMemMB} megabytes available. Power reserves: ${bat.percentage}%. Uplink: $net."
                    _actionNotification.value = ActionNotification("Diagnostics: All Systems Nominal")
                    finalizeResponse(command, reply, "DIAGNOSTICS")
                }

                ActionType.OPEN_CAMERA -> {
                    val launched = deviceActions.launchCamera()
                    val reply = if (launched) "Optical sensors engaged, sir." else "Optical sensor interface inaccessible."
                    _actionNotification.value = ActionNotification("Camera Sensors: Engaged")
                    finalizeResponse(command, reply, "APP_LAUNCH")
                }

                ActionType.OPEN_YOUTUBE -> {
                    val launched = deviceActions.launchYouTube(parsed.parameter)
                    val reply = if (launched) "Accessing media stream, sir." else "Media interface offline."
                    _actionNotification.value = ActionNotification("Launching Media Stream")
                    finalizeResponse(command, reply, "APP_LAUNCH")
                }

                ActionType.OPEN_MAPS -> {
                    val launched = deviceActions.launchMaps(parsed.parameter)
                    val reply = if (launched) "Cartographic and navigation uplink online, sir." else "Navigation subsystem error."
                    _actionNotification.value = ActionNotification("Navigation Uplink Active")
                    finalizeResponse(command, reply, "APP_LAUNCH")
                }

                ActionType.OPEN_SETTINGS -> {
                    val launched = deviceActions.launchSettings()
                    val reply = if (launched) "Accessing system configuration." else "Configuration interface locked."
                    _actionNotification.value = ActionNotification("System Settings Opened")
                    finalizeResponse(command, reply, "APP_LAUNCH")
                }

                ActionType.SET_TIMER -> {
                    val seconds = parsed.parameter?.toIntOrNull() ?: 60
                    val launched = deviceActions.setTimer(seconds, "JARVIS Alert")
                    val mins = seconds / 60
                    val reply = if (launched) {
                        "Chronometer initialized for ${if (mins > 0) "$mins minutes" else "$seconds seconds"}, sir."
                    } else {
                        "Chronometer service unavailable."
                    }
                    _actionNotification.value = ActionNotification("Timer Set: ${seconds}s")
                    finalizeResponse(command, reply, "TIMER")
                }

                ActionType.SAVE_NOTE -> {
                    val noteText = parsed.parameter?.ifBlank { "Directive recorded" } ?: "Directive recorded"
                    dao.insertNote(
                        JarvisNoteEntity(
                            title = "Voice Directive ${System.currentTimeMillis() % 10000}",
                            content = noteText,
                            category = "DIRECTIVE"
                        )
                    )
                    val reply = "Directive safely logged in core memory, sir: '$noteText'."
                    _actionNotification.value = ActionNotification("Directive Logged")
                    finalizeResponse(command, reply, "NOTE")
                }

                ActionType.WEB_SEARCH -> {
                    val query = parsed.parameter ?: command
                    val launched = deviceActions.launchWebSearch(query)
                    val reply = if (launched) "Querying global intelligence grid for: $query." else "Global uplink failed."
                    _actionNotification.value = ActionNotification("Searching: $query")
                    finalizeResponse(command, reply, "WEB_SEARCH")
                }

                ActionType.VOLUME_UP -> {
                    val vol = deviceActions.adjustVolume(true)
                    val reply = "Acoustic output elevated to level $vol, sir."
                    _actionNotification.value = ActionNotification("Volume Increased: $vol")
                    finalizeResponse(command, reply, "VOLUME")
                }

                ActionType.VOLUME_DOWN -> {
                    val vol = deviceActions.adjustVolume(false)
                    val reply = "Acoustic output decreased to level $vol, sir."
                    _actionNotification.value = ActionNotification("Volume Decreased: $vol")
                    finalizeResponse(command, reply, "VOLUME")
                }

                ActionType.GENERAL_QUERY -> {
                    // Send to Gemini 3.8-Flash AI Logic
                    executeGeminiQuery(command)
                }
            }
        }
    }

    private suspend fun executeGeminiQuery(prompt: String) {
        val accumulated = StringBuilder()
        _latestResponse.value = "Accessing neural cores..."
        try {
            aiService.streamResponse(prompt).collect { chunk ->
                accumulated.append(chunk)
                _latestResponse.value = accumulated.toString()
            }
            val finalReply = accumulated.toString().ifBlank {
                "At your command, sir. All calculations completed."
            }
            finalizeResponse(prompt, finalReply, "AI_QUERY")
        } catch (e: Exception) {
            val errorReply = "Neural network anomaly: ${e.localizedMessage ?: "Unable to complete thought process."}"
            finalizeResponse(prompt, errorReply, "ERROR")
        }
    }

    private suspend fun finalizeResponse(command: String, response: String, actionType: String) {
        _latestResponse.value = response
        _mode.value = AssistantMode.SPEAKING

        // Save log
        dao.insertLog(
            CommandLogEntity(
                command = command,
                response = response,
                actionType = actionType,
                status = "SUCCESS"
            )
        )

        // Speak aloud through TTS
        ttsManager.speak(response) {
            _mode.value = AssistantMode.IDLE
        }
    }

    fun addManualNote(title: String, content: String, category: String = "DIRECTIVE") {
        viewModelScope.launch {
            dao.insertNote(
                JarvisNoteEntity(
                    title = title,
                    content = content,
                    category = category
                )
            )
        }
    }

    fun deleteNote(note: JarvisNoteEntity) {
        viewModelScope.launch {
            dao.deleteNote(note)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            dao.clearLogs()
        }
    }

    fun replayResponse(text: String) {
        ttsManager.stop()
        _mode.value = AssistantMode.SPEAKING
        ttsManager.speak(text) {
            _mode.value = AssistantMode.IDLE
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.destroy()
        ttsManager.shutdown()
    }
}
