package com.vpedrosa.smarthome.ui.voice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpedrosa.smarthome.voice.domain.model.ParsedVoiceCommand
import com.vpedrosa.smarthome.voice.domain.model.VoiceCommand
import com.vpedrosa.smarthome.voice.domain.model.VoiceCommandResult
import com.vpedrosa.smarthome.voice.domain.SpeechRecognizerPort
import com.vpedrosa.smarthome.voice.application.ExecuteVoiceCommandUseCase
import com.vpedrosa.smarthome.voice.application.ParseVoiceCommandUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock

data class VoiceControlUiState(
    val isListening: Boolean = false,
    val recognizedText: String = "",
    val lastResult: VoiceCommandResult? = null,
    val recentCommands: List<VoiceCommand> = emptyList(),
    val error: String? = null,
)

class VoiceControlViewModel(
    private val speechRecognizer: SpeechRecognizerPort,
    private val parseVoiceCommand: ParseVoiceCommandUseCase,
    private val executeVoiceCommand: ExecuteVoiceCommandUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceControlUiState())
    val uiState: StateFlow<VoiceControlUiState> = _uiState.asStateFlow()

    init {
        speechRecognizer.isListening
            .onEach { listening ->
                _uiState.update { it.copy(isListening = listening) }
            }
            .launchIn(viewModelScope)

        speechRecognizer.recognizedText
            .onEach { text ->
                _uiState.update { it.copy(recognizedText = text) }
                processCommand(text)
            }
            .launchIn(viewModelScope)

        speechRecognizer.errors
            .onEach { error ->
                _uiState.update { it.copy(error = error, isListening = false) }
            }
            .launchIn(viewModelScope)
    }

    fun toggleListening() {
        if (_uiState.value.isListening) {
            speechRecognizer.stopListening()
        } else {
            _uiState.update { it.copy(lastResult = null, recognizedText = "", error = null) }
            speechRecognizer.startListening()
        }
    }

    /**
     * Process a text command directly (useful for testing without speech recognizer).
     */
    fun submitTextCommand(text: String) {
        _uiState.update { it.copy(recognizedText = text) }
        processCommand(text)
    }

    private fun processCommand(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val result = try {
                val parsed = parseVoiceCommand(text)
                executeVoiceCommand(parsed)
            } catch (e: Exception) {
                VoiceCommandResult(
                    success = false,
                    message = e.message ?: "Error executing command",
                    devicesAffected = 0,
                )
            }

            val command = VoiceCommand(
                text = text,
                result = result,
                timestamp = Clock.System.now(),
            )

            _uiState.update { state ->
                state.copy(
                    lastResult = result,
                    recentCommands = listOf(command) + state.recentCommands.take(MAX_RECENT_COMMANDS - 1),
                    isListening = false,
                )
            }
        }
    }

    companion object {
        private const val MAX_RECENT_COMMANDS = 10
    }
}
