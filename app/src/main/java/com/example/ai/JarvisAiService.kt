package com.example.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

private const val TAG = "JarvisAiService"

data class JarvisParsedAction(
    val actionType: ActionType,
    val parameter: String? = null
)

enum class ActionType {
    GENERAL_QUERY,
    FLASHLIGHT_ON,
    FLASHLIGHT_OFF,
    FLASHLIGHT_TOGGLE,
    CHECK_BATTERY,
    SYSTEM_DIAGNOSTICS,
    OPEN_CAMERA,
    OPEN_YOUTUBE,
    OPEN_MAPS,
    OPEN_SETTINGS,
    SET_TIMER,
    SAVE_NOTE,
    WEB_SEARCH,
    VOLUME_UP,
    VOLUME_DOWN
}

class JarvisAiService {

    // Lazy initialization mandated by Firebase AI Logic skill
    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = content {
                text(
                    """
                    You are J.A.R.V.I.S. (Just A Rather Very Intelligent System), the premier AI assistant created by Tony Stark.
                    Characteristics:
                    - Highly intelligent, calm, poised, witty, and unfailingly polite with a classic British butler sensibility.
                    - Address the user with courtesy (e.g. 'sir' or 'ma'am' / 'at your service').
                    - Keep your voice answers concise, punchy, and conversational (ideally 1 to 3 clear sentences) since they are synthesized aloud to the user.
                    - If asked about system status or hardware, answer with crisp Stark-tech telemetry style.
                    - Avoid long rambling lists or excessive markdown symbols unless explicitly asked.
                    """.trimIndent()
                )
            },
            generationConfig = generationConfig {
                temperature = 0.7f
                maxOutputTokens = 600
            }
        )
    }

    /**
     * Fast local intent extraction for instant low-latency hardware actions.
     */
    fun classifyIntent(input: String): JarvisParsedAction {
        val lower = input.lowercase().trim()

        return when {
            lower.contains("torch on") || lower.contains("flashlight on") ||
            lower.contains("turn on the flashlight") || lower.contains("turn on flashlight") ||
            lower.contains("turn on the torch") || lower.contains("turn on torch") ||
            lower.contains("turn on the light") || lower.contains("lights on") ->
                JarvisParsedAction(ActionType.FLASHLIGHT_ON)

            lower.contains("torch off") || lower.contains("flashlight off") ||
            lower.contains("turn off the flashlight") || lower.contains("turn off flashlight") ||
            lower.contains("turn off the torch") || lower.contains("turn off torch") ||
            lower.contains("turn off the light") || lower.contains("lights off") ->
                JarvisParsedAction(ActionType.FLASHLIGHT_OFF)

            lower.contains("flashlight") || lower.contains("torch") ->
                JarvisParsedAction(ActionType.FLASHLIGHT_TOGGLE)

            lower.contains("battery") || lower.contains("power level") || lower.contains("energy status") ->
                JarvisParsedAction(ActionType.CHECK_BATTERY)

            lower.contains("diagnostic") || lower.contains("system status") || lower.contains("memory") || lower.contains("specs") ->
                JarvisParsedAction(ActionType.SYSTEM_DIAGNOSTICS)

            lower.contains("open camera") || lower.contains("take a photo") || lower.contains("launch camera") ->
                JarvisParsedAction(ActionType.OPEN_CAMERA)

            lower.contains("open youtube") || lower.contains("play on youtube") -> {
                val query = lower.substringAfter("open youtube", "").substringAfter("play on youtube", "").trim()
                JarvisParsedAction(ActionType.OPEN_YOUTUBE, query.ifBlank { null })
            }

            lower.contains("open maps") || lower.contains("navigate to") || lower.contains("where is") -> {
                val loc = lower.substringAfter("navigate to", "").substringAfter("where is", "").substringAfter("open maps", "").trim()
                JarvisParsedAction(ActionType.OPEN_MAPS, loc.ifBlank { null })
            }

            lower.contains("open settings") || lower.contains("system settings") ->
                JarvisParsedAction(ActionType.OPEN_SETTINGS)

            lower.contains("set a timer") || lower.contains("set timer") -> {
                // Extract number of seconds or minutes
                val seconds = extractTimerSeconds(lower)
                JarvisParsedAction(ActionType.SET_TIMER, seconds.toString())
            }

            lower.startsWith("note:") || lower.startsWith("note ") ||
            lower.startsWith("take a note") || lower.startsWith("remember") || lower.startsWith("new note") -> {
                val content = input.trim().replace(Regex("^(?i)(take a note(:)?|remember(:)?|note(:)?|new note(:)?)\\s*"), "").trim()
                JarvisParsedAction(ActionType.SAVE_NOTE, content)
            }

            lower.startsWith("search for") || lower.startsWith("google") -> {
                val query = lower.replace(Regex("^(search for|google)"), "").trim()
                JarvisParsedAction(ActionType.WEB_SEARCH, query)
            }

            lower.contains("volume up") || lower.contains("louder") || lower.contains("increase volume") ->
                JarvisParsedAction(ActionType.VOLUME_UP)

            lower.contains("volume down") || lower.contains("quieter") || lower.contains("decrease volume") ->
                JarvisParsedAction(ActionType.VOLUME_DOWN)

            else -> JarvisParsedAction(ActionType.GENERAL_QUERY)
        }
    }

    private fun extractTimerSeconds(text: String): Int {
        val numberMatch = Regex("(\\d+)\\s*(second|sec|minute|min|hour|hr)?").find(text)
        if (numberMatch != null) {
            val num = numberMatch.groupValues[1].toIntOrNull() ?: 60
            val unit = numberMatch.groupValues[2].lowercase()
            return when {
                unit.startsWith("min") -> num * 60
                unit.startsWith("hour") || unit.startsWith("hr") -> num * 3600
                else -> num
            }
        }
        return 60 // Default 1 minute
    }

    /**
     * Streaming response using Firebase AI Logic Gemini 3.8 Flash
     */
    fun streamResponse(prompt: String): Flow<String> = flow {
        withTimeout(60_000L) {
            try {
                model.generateContentStream(prompt).collect { chunk ->
                    val text = chunk.text.orEmpty()
                    if (text.isNotEmpty()) {
                        emit(text)
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Gemini stream timed out", e)
                emit("I apologize, sir, but the neural uplink timed out. Please try again.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Gemini AI stream error", e)
                emit("A brief anomaly occurred in my cognitive subroutines: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    /**
     * Unary response for quick single-turn queries
     */
    suspend fun generateResponse(prompt: String): String {
        return try {
            withTimeout(60_000L) {
                val response = model.generateContent(prompt)
                response.text ?: "At your service, sir. All systems nominal."
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini inference timed out", e)
            "My apologies, sir. Query processing timed out after 60 seconds."
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini inference failed", e)
            "Telemetry error: ${e.localizedMessage ?: "Unable to contact neural network."}"
        }
    }
}
