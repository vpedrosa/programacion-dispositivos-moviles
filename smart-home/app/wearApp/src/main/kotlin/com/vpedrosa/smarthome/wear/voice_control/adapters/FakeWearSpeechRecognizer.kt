package com.vpedrosa.smarthome.wear.voice_control.adapters

import com.vpedrosa.smarthome.wear.voice_control.domain.ports.WearSpeechRecognizerPort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Fake speech recognizer for emulators or devices without microphone.
 * Simulates a 2-second listening period and emits a sample command.
 */
class FakeWearSpeechRecognizer(
    private val scope: CoroutineScope,
) : WearSpeechRecognizerPort {

    private val _isListening = MutableStateFlow(false)
    override val isListening: Flow<Boolean> = _isListening.asStateFlow()

    private val _recognizedText = MutableSharedFlow<String>(extraBufferCapacity = 1)
    override val recognizedText: Flow<String> = _recognizedText.asSharedFlow()

    private val _errors = MutableSharedFlow<String>(extraBufferCapacity = 1)
    override val errors: Flow<String> = _errors.asSharedFlow()

    private val sampleCommands = listOf(
        "Enciende las luces del salón",
        "Apaga la luz de la cocina",
        "Sube la temperatura a 24 grados",
        "Activa la alarma",
        "Abre la persiana del dormitorio",
        "Enciende el ventilador",
    )

    private var commandIndex = 0

    override fun startListening() {
        if (_isListening.value) return
        _isListening.value = true

        scope.launch {
            delay(2_000L)
            if (_isListening.value) {
                val command = sampleCommands[commandIndex % sampleCommands.size]
                commandIndex++
                _isListening.value = false
                _recognizedText.tryEmit(command)
            }
        }
    }

    override fun stopListening() {
        _isListening.value = false
    }

    override fun destroy() {
        // Nothing to clean up
    }
}
