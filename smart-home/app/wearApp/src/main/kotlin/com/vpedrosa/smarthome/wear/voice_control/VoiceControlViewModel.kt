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
     * Simulates capturing audio and transcribing it.
     * In a real implementation, this would start the microphone,
     * stream audio to the phone, and the phone would do STT.
     */
    fun onMicPressed() {
        val current = _uiState.value
        if (current.status == VoiceStatus.Listening || current.status == VoiceStatus.Processing) return

        viewModelScope.launch {
            _uiState.value = VoiceControlUiState(status = VoiceStatus.Listening)

            // Simulate listening duration
            delay(2_000L)

            // Simulate a transcribed phrase
            val simulatedPhrase = SIMULATED_PHRASES.random()
            _uiState.value = VoiceControlUiState(
                status = VoiceStatus.Processing,
                transcribedText = simulatedPhrase,
            )

            // Send to the "main app" via the port
            val result = voiceCommandPort.sendCommand(simulatedPhrase)

            _uiState.value = when (result) {
                is VoiceCommandResult.Success -> VoiceControlUiState(
                    status = VoiceStatus.Result,
                    transcribedText = simulatedPhrase,
                    resultMessage = result.responseMessage,
                )
                is VoiceCommandResult.Error -> VoiceControlUiState(
                    status = VoiceStatus.Error,
                    transcribedText = simulatedPhrase,
                    resultMessage = result.errorMessage,
                )
            }

            // Auto-reset back to idle after showing the result
            delay(4_000L)
            _uiState.value = VoiceControlUiState()
        }
    }

    companion object {
        private val SIMULATED_PHRASES = listOf(
            "Enciende las luces del salon",
            "Apaga la luz de la cocina",
            "Sube la temperatura a 24 grados",
            "Activa la alarma",
            "Abre la persiana del dormitorio",
            "Enciende el ventilador",
        )
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
