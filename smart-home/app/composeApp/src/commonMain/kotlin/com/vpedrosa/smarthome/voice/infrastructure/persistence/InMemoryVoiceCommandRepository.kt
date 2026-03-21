package com.vpedrosa.smarthome.voice.infrastructure.persistence

import com.vpedrosa.smarthome.voice.domain.VoiceCommandRepository
import com.vpedrosa.smarthome.voice.domain.model.VoiceCommand
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InMemoryVoiceCommandRepository : VoiceCommandRepository {

    private val _commands = MutableStateFlow<List<VoiceCommand>>(emptyList())

    override fun observeRecentCommands(): Flow<List<VoiceCommand>> = _commands.asStateFlow()

    override fun add(command: VoiceCommand) {
        _commands.update { current ->
            listOf(command) + current.take(MAX_COMMANDS - 1)
        }
    }

    private companion object {
        const val MAX_COMMANDS = 10
    }
}
