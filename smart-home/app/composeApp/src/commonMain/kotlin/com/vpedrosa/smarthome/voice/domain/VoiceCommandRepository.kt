package com.vpedrosa.smarthome.voice.domain

import com.vpedrosa.smarthome.voice.domain.model.VoiceCommand
import kotlinx.coroutines.flow.Flow

interface VoiceCommandRepository {
    fun observeRecentCommands(): Flow<List<VoiceCommand>>
    fun add(command: VoiceCommand)
}
