package com.example.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TtsManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private var onCompleteCallback: (() -> Unit)? = null

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            val ttsEngine = tts ?: return

            // Prefer UK English for authentic JARVIS timbre
            val result = ttsEngine.setLanguage(Locale.UK)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                ttsEngine.setLanguage(Locale.US)
            }

            // Refined British AI tone: slightly deeper pitch, crisp brisk pace
            ttsEngine.setPitch(0.95f)
            ttsEngine.setSpeechRate(1.05f)

            ttsEngine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    onCompleteCallback?.invoke()
                    onCompleteCallback = null
                }

                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    onCompleteCallback = null
                }
            })
            Log.d("TtsManager", "JARVIS voice synthesizer online")
        } else {
            Log.e("TtsManager", "TextToSpeech initialization failed: $status")
        }
    }

    fun toggleMute(): Boolean {
        val newMute = !_isMuted.value
        _isMuted.value = newMute
        if (newMute) {
            stop()
        }
        return newMute
    }

    fun speak(text: String, onComplete: (() -> Unit)? = null) {
        if (_isMuted.value) {
            onComplete?.invoke()
            return
        }

        if (!isInitialized || tts == null) {
            Log.w("TtsManager", "TTS not initialized yet")
            onComplete?.invoke()
            return
        }

        // Clean markdown formatting before speaking (remove asterisks, brackets, hashes)
        val cleanText = text
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
            .replace(Regex("\\*(.*?)\\*"), "$1")
            .replace(Regex("#+\\s*"), "")
            .replace(Regex("`{1,3}.*?`{1,3}"), "")
            .replace(Regex("[\\[\\]]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()

        if (cleanText.isEmpty()) {
            onComplete?.invoke()
            return
        }

        stop()
        onCompleteCallback = onComplete
        _isSpeaking.value = true

        val utteranceId = "JARVIS_${System.currentTimeMillis()}"
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
