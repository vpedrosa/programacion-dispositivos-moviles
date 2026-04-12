package com.vpedrosa.smarthome.voice.application

import com.vpedrosa.smarthome.voice.domain.VoiceCommandRepository
import com.vpedrosa.smarthome.voice.domain.model.VoiceCommand
import com.vpedrosa.smarthome.voice.domain.model.VoiceCommandResult
import kotlin.time.Clock

class RecordVoiceCommandUseCase(
    private val voiceCommandRepository: VoiceCommandRepository,
) {
    suspend operator fun invoke(text: String, result: VoiceCommandResult) {
        voiceCommandRepository.add(
            VoiceCommand(
                text = text,
                result = result,
                timestamp = Clock.System.now(),
            ),
        )
    }
}
