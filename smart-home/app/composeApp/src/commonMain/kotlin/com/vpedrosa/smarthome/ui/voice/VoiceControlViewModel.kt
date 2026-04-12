package com.vpedrosa.smarthome.ui.voice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpedrosa.smarthome.voice.domain.VoiceCommandRepository
import com.vpedrosa.smarthome.voice.domain.model.VoiceCommand
import com.vpedrosa.smarthome.voice.domain.model.VoiceCommandResult
import com.vpedrosa.smarthome.voice.domain.SpeechRecognizerPort
import com.vpedrosa.smarthome.voice.application.ExecuteVoiceCommandUseCase
import com.vpedrosa.smarthome.voice.application.ParseVoiceCommandUseCase
import com.vpedrosa.smarthome.voice.application.RecordVoiceCommandUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VoiceControlUiState(
    val isListening: Boolean = false,
    val isProcessing: Boolean = false,
    val recognizedText: String = "",
    val lastResult: VoiceCommandResult? = null,
    val recentCommands: List<VoiceCommand> = emptyList(),
    val error: String? = null,
)

class VoiceControlViewModel(
    private val speechRecognizer: SpeechRecognizerPort,
    private val parseVoiceCommand: ParseVoiceCommandUseCase,
    private val executeVoiceCommand: ExecuteVoiceCommandUseCase,
    private val voiceCommandRepository: VoiceCommandRepository,
    private val recordVoiceCommand: RecordVoiceCommandUseCase,
) : ViewModel() {

    private val _localState = MutableStateFlow(LocalState())

    val uiState: StateFlow<VoiceControlUiState> = combine(
        _localState,
        voiceCommandRepository.observeRecentCommands(),
    ) { local, commands ->
        VoiceControlUiState(
            isListening = local.isListening,
            isProcessing = local.isProcessing,
            recognizedText = local.recognizedText,
            lastResult = local.lastResult,
            recentCommands = commands,
            error = local.error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = VoiceControlUiState(),
    )

    init {
        speechRecognizer.isListening
            .onEach { listening ->
                _localState.update { it.copy(isListening = listening) }
            }
            .launchIn(viewModelScope)

        speechRecognizer.recognizedText
            .onEach { text ->
                _localState.update { it.copy(recognizedText = text) }
                processCommand(text)
            }
            .launchIn(viewModelScope)

        speechRecognizer.errors
            .onEach { error ->
                _localState.update { it.copy(error = error, isListening = false) }
            }
            .launchIn(viewModelScope)
    }

    fun toggleListening() {
        if (_localState.value.isListening) {
            speechRecognizer.stopListening()
        } else {
            _localState.update { it.copy(lastResult = null, recognizedText = "", error = null) }
            speechRecognizer.startListening()
        }
    }

    fun submitTextCommand(text: String) {
        _localState.update { it.copy(recognizedText = text) }
        processCommand(text)
    }

    private fun processCommand(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _localState.update { it.copy(isProcessing = true) }

            val result = try {
                val parsed = parseVoiceCommand(text)
                executeVoiceCommand(parsed, text)
            } catch (e: Exception) {
                VoiceCommandResult(
                    success = false,
                    message = e.message ?: "Error executing command",
                    devicesAffected = 0,
                )
            }

            recordVoiceCommand(text, result)

            _localState.update { it.copy(lastResult = result, isListening = false, isProcessing = false) }
        }
    }

    private data class LocalState(
        val isListening: Boolean = false,
        val isProcessing: Boolean = false,
        val recognizedText: String = "",
        val lastResult: VoiceCommandResult? = null,
        val error: String? = null,
    )
}
