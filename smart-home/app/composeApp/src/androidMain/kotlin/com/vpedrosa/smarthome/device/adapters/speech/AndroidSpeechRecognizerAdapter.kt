package com.vpedrosa.smarthome.device.adapters.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.vpedrosa.smarthome.device.domain.ports.SpeechRecognizerPort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Android adapter for [SpeechRecognizerPort] using the platform SpeechRecognizer API.
 *
 * All SpeechRecognizer operations run on the main thread via a [Handler]
 * because Android's SpeechRecognizer must be created and used on the main looper.
 */
class AndroidSpeechRecognizerAdapter(
    private val context: Context,
) : SpeechRecognizerPort {

    private val mainHandler = Handler(Looper.getMainLooper())

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
            // Already set isListening in onReadyForSpeech
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Could be used for mic level visualization in the future
        }

        override fun onBufferReceived(buffer: ByteArray?) {
            // Not used
        }

        override fun onEndOfSpeech() {
            // Final state handled by onResults or onError
        }

        override fun onError(error: Int) {
            _isListening.value = false
            val message = mapErrorCode(error)
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
                _errors.tryEmit("No se reconocio ningún texto")
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            // Not using partial results for now
        }

        override fun onEvent(eventType: Int, params: Bundle?) {
            // Reserved for future use by the framework
        }
    }

    override fun startListening() {
        if (_isListening.value) return

        mainHandler.post {
            // Recreate recognizer each time to avoid stale state
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).also {
                it.setRecognitionListener(recognitionListener)
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            }

            speechRecognizer?.startListening(intent)
        }
    }

    override fun stopListening() {
        mainHandler.post {
            _isListening.value = false
            speechRecognizer?.stopListening()
        }
    }

    private fun mapErrorCode(error: Int): String = when (error) {
        SpeechRecognizer.ERROR_AUDIO ->
            "Error de audio"

        SpeechRecognizer.ERROR_CLIENT ->
            "Error del cliente"

        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
            "Permiso de microfono no concedido"

        SpeechRecognizer.ERROR_NETWORK ->
            "Error de red"

        SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
            "Tiempo de espera de red agotado"

        SpeechRecognizer.ERROR_NO_MATCH ->
            "No se reconocio ningún comando"

        SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
            "El reconocedor esta ocupado"

        SpeechRecognizer.ERROR_SERVER ->
            "Error del servidor"

        SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
            "No se detecto voz"

        else ->
            "Error desconocido ($error)"
    }
}
