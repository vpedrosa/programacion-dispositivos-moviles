package com.vpedrosa.smarthome.wear.voice_control

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpedrosa.smarthome.wear.voice_control.domain.ports.VoiceCommandPort
import com.vpedrosa.smarthome.wear.voice_control.domain.ports.VoiceCommandResult
import com.vpedrosa.smarthome.wear.voice_control.domain.ports.WearSpeechRecognizerPort
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class VoiceControlViewModel(
    private val speechRecognizer: WearSpeechRecognizerPort,
    private val voiceCommandPort: VoiceCommandPort,
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceControlUiState())
    val uiState: StateFlow<VoiceControlUiState> = _uiState.asStateFlow()

    init {
        // Observe speech recognizer listening state
        speechRecognizer.isListening
            .onEach { listening ->
                if (listening) {
                    _uiState.value = VoiceControlUiState(status = VoiceStatus.Listening)
                }
            }
            .launchIn(viewModelScope)

        // Observe recognized text and send to the phone app
        speechRecognizer.recognizedText
            .onEach { text -> handleRecognizedText(text) }
            .launchIn(viewModelScope)

        // Observe speech recognition errors
        speechRecognizer.errors
            .onEach { error ->
                _uiState.value = VoiceControlUiState(
                    status = VoiceStatus.Error,
                    resultMessage = error,
                )
                delay(AUTO_RESET_DELAY_MS)
                _uiState.value = VoiceControlUiState()
            }
            .launchIn(viewModelScope)
    }

    /**
     * Starts or stops speech recognition when the mic button is pressed.
     */
    fun onMicPressed() {
        val current = _uiState.value
        if (current.status == VoiceStatus.Processing) return

        if (current.status == VoiceStatus.Listening) {
            speechRecognizer.stopListening()
            _uiState.value = VoiceControlUiState()
            return
        }

        speechRecognizer.startListening()
    }

    private fun handleRecognizedText(text: String) {
        viewModelScope.launch {
            _uiState.value = VoiceControlUiState(
                status = VoiceStatus.Processing,
                transcribedText = text,
            )

            // Send to the phone app via the port
            val result = voiceCommandPort.sendCommand(text)

            _uiState.value = when (result) {
                is VoiceCommandResult.Success -> VoiceControlUiState(
                    status = VoiceStatus.Result,
                    transcribedText = text,
                    resultMessage = result.responseMessage,
                )
                is VoiceCommandResult.Error -> VoiceControlUiState(
                    status = VoiceStatus.Error,
                    transcribedText = text,
                    resultMessage = result.errorMessage,
                )
            }

            // Auto-reset back to idle after showing the result
            delay(AUTO_RESET_DELAY_MS)
            _uiState.value = VoiceControlUiState()
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer.destroy()
    }

    companion object {
        private const val AUTO_RESET_DELAY_MS = 4_000L
    }
}

data class VoiceControlUiState(
    val status: VoiceStatus = VoiceStatus.Idle,
    val transcribedText: String = "",
    val resultMessage: String = "",
)

enum class VoiceStatus {
    Idle,
    Listening,
    Processing,
    Result,
    Error,
}
