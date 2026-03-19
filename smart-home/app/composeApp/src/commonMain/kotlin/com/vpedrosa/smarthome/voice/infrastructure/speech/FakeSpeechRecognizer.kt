package com.vpedrosa.smarthome.voice.infrastructure.speech

import com.vpedrosa.smarthome.voice.domain.SpeechRecognizerPort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Fake speech recognizer for development and testing.
 * Simulates a 2-second listening period then emits a sample command.
 */
class FakeSpeechRecognizer(
    private val scope: CoroutineScope,
) : SpeechRecognizerPort {

    private val _isListening = MutableStateFlow(false)
    override val isListening: Flow<Boolean> = _isListening.asStateFlow()

    private val _recognizedText = MutableSharedFlow<String>()
    override val recognizedText: Flow<String> = _recognizedText.asSharedFlow()

    private val _errors = MutableSharedFlow<String>()
    override val errors: Flow<String> = _errors.asSharedFlow()

    private val sampleCommands = listOf(
        "Enciende las luces del salón",
        "Apaga la tele",
        "Cierra las persianas",
        "Pon la temperatura a 22 grados",
        "Abre la puerta de entrada",
        "Enciende los interruptores",
    )

    private var commandIndex = 0

    override fun startListening() {
        if (_isListening.value) return
        _isListening.value = true

        scope.launch {
            delay(2000)
            if (_isListening.value) {
                val command = sampleCommands[commandIndex % sampleCommands.size]
                commandIndex++
                _recognizedText.emit(command)
                _isListening.value = false
            }
        }
    }

    override fun stopListening() {
        _isListening.value = false
    }
}
