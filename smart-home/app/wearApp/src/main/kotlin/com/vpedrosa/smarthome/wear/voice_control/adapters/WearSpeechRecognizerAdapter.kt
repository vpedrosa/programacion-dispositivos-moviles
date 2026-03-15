package com.vpedrosa.smarthome.wear.voice_control.adapters

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.vpedrosa.smarthome.wear.voice_control.domain.ports.WearSpeechRecognizerPort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Driven adapter that uses Android's SpeechRecognizer for on-device
 * speech-to-text on Wear OS. Emits recognized text via flows.
 */
class WearSpeechRecognizerAdapter(
    private val context: Context,
) : WearSpeechRecognizerPort {

    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    override val isListening: Flow<Boolean> = _isListening.asStateFlow()

    private val _recognizedText = MutableSharedFlow<String>(extraBufferCapacity = 1)
    override val recognizedText: Flow<String> = _recognizedText.asSharedFlow()

    private val _errors = MutableSharedFlow<String>(extraBufferCapacity = 1)
    override val errors: Flow<String> = _errors.asSharedFlow()

    private val recognitionListener = object : RecognitionListener {

        override fun onReadyForSpeech(params: Bundle?) {
            _isListening.value = true
        }

        override fun onBeginningOfSpeech() {
            // Already listening
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Could be used for visual feedback amplitude
        }

        override fun onBufferReceived(buffer: ByteArray?) {
            // Not needed for text output
        }

        override fun onEndOfSpeech() {
            _isListening.value = false
        }

        override fun onError(error: Int) {
            _isListening.value = false
            val message = mapSpeechError(error)
            _errors.tryEmit(message)
        }

        override fun onResults(results: Bundle?) {
            _isListening.value = false
            val matches = results
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val bestResult = matches?.firstOrNull()
            if (bestResult != null) {
                _recognizedText.tryEmit(bestResult)
            } else {
                _errors.tryEmit("No se reconoció ningún texto")
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            // We wait for final results
        }

        override fun onEvent(eventType: Int, params: Bundle?) {
            // Not used
        }
    }

    override fun startListening() {
        if (_isListening.value) return

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _errors.tryEmit("Reconocimiento de voz no disponible en este dispositivo")
            return
        }

        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(recognitionListener)
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
            )
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            // Support both Spanish and English
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
        }

        speechRecognizer?.startListening(intent)
    }

    override fun stopListening() {
        speechRecognizer?.stopListening()
        _isListening.value = false
    }

    override fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    private fun mapSpeechError(errorCode: Int): String = when (errorCode) {
        SpeechRecognizer.ERROR_AUDIO -> "Error de audio"
        SpeechRecognizer.ERROR_CLIENT -> "Error del cliente"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permiso de micrófono denegado"
        SpeechRecognizer.ERROR_NETWORK -> "Error de red"
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Tiempo de espera de red agotado"
        SpeechRecognizer.ERROR_NO_MATCH -> "No se reconoció ningún comando"
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Reconocedor ocupado"
        SpeechRecognizer.ERROR_SERVER -> "Error del servidor"
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No se detectó voz"
        else -> "Error desconocido ($errorCode)"
    }
}
