package com.example.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SpeechManager(private val context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private var speechRecognizer: SpeechRecognizer? = null

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _partialSpeech = MutableStateFlow("")
    val partialSpeech: StateFlow<String> = _partialSpeech.asStateFlow()

    private var onSpeechResultCallback: ((String) -> Unit)? = null
    private var onSpeechErrorCallback: ((String) -> Unit)? = null
    private var onTtsDoneCallback: (() -> Unit)? = null

    companion object {
        private const val TAG = "SpeechManager"
        private const val UTTERANCE_ID = "kayara_utterance"
    }

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    onTtsDoneCallback?.invoke()
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                }
            })
            Log.d(TAG, "TTS successfully initialized")
        } else {
            Log.e(TAG, "TTS initialization failed: $status")
        }
    }

    fun configureVoice(gender: String, languageCode: String) {
        if (!isTtsInitialized || tts == null) return

        try {
            val locale = Locale.forLanguageTag(languageCode.replace("_", "-"))
            val result = tts?.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.US)
            }

            // Adjust pitch according to companion gender
            if (gender == "female") {
                tts?.setPitch(1.15f) // Sweet, lively voice
                tts?.setSpeechRate(1.02f)
            } else {
                tts?.setPitch(0.9f) // Calm, warm male voice
                tts?.setSpeechRate(0.98f)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error configuring TTS: ${e.message}")
        }
    }

    fun speak(
        text: String,
        gender: String = "female",
        languageCode: String = "en-US",
        onDone: (() -> Unit)? = null
    ) {
        if (!isTtsInitialized || tts == null) {
            onDone?.invoke()
            return
        }

        configureVoice(gender, languageCode)
        onTtsDoneCallback = onDone

        // Clean markdown bold or symbols before speaking
        val cleanText = text.replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
            .replace(Regex("[#*_~`]"), "")
            .trim()

        if (cleanText.isEmpty()) {
            onDone?.invoke()
            return
        }

        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_ID)
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, params, UTTERANCE_ID)
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun startListening(
        languageCode: String = "en-US",
        onResult: (String) -> Unit,
        onError: ((String) -> Unit)? = null
    ) {
        stopSpeaking()
        stopListening()

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError?.invoke("Speech recognition is not available on this device.")
            return
        }

        onSpeechResultCallback = onResult
        onSpeechErrorCallback = onError
        _partialSpeech.value = ""

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                    }

                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isListening.value = false
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        val message = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timed out"
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permission required for microphone"
                            else -> "Recognition error ($error)"
                        }
                        onSpeechErrorCallback?.invoke(message)
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim().orEmpty()
                        if (text.isNotEmpty()) {
                            onSpeechResultCallback?.invoke(text)
                        } else {
                            onSpeechErrorCallback?.invoke("No words recognized.")
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim().orEmpty()
                        if (text.isNotEmpty()) {
                            _partialSpeech.value = text
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start speech recognizer: ${e.message}")
            _isListening.value = false
            onError?.invoke(e.localizedMessage ?: "Failed to start listening")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping recognizer: ${e.message}")
        } finally {
            speechRecognizer = null
            _isListening.value = false
        }
    }

    fun shutdown() {
        stopSpeaking()
        stopListening()
        tts?.shutdown()
        tts = null
    }
}
