package com.vpedrosa.smarthome.wear.voice_control

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpedrosa.smarthome.wear.voice_control.domain.ports.VoiceCommandPort
import com.vpedrosa.smarthome.wear.voice_control.domain.ports.VoiceCommandResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VoiceControlViewModel(
    private val voiceCommandPort: VoiceCommandPort,
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceControlUiState())
    val uiState: StateFlow<VoiceControlUiState> = _uiState.asStateFlow()

    /**
     * Called when the system RecognizerIntent returns recognized text.
     */
    fun onSpeechResult(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            _uiState.value = VoiceControlUiState(
                status = VoiceStatus.Processing,
                transcribedText = text,
            )

            val result = try {
                voiceCommandPort.sendCommand(text)
            } catch (e: Exception) {
                VoiceCommandResult.Error(e.message ?: "Unknown error")
            }

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

            delay(AUTO_RESET_DELAY_MS)
            _uiState.value = VoiceControlUiState()
        }
    }

    /**
     * Called when speech recognition fails or is unavailable.
     */
    fun onSpeechError(message: String) {
        viewModelScope.launch {
            _uiState.value = VoiceControlUiState(
                status = VoiceStatus.Error,
                resultMessage = message,
            )
            delay(AUTO_RESET_DELAY_MS)
            _uiState.value = VoiceControlUiState()
        }
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
    Processing,
    Result,
    Error,
}
