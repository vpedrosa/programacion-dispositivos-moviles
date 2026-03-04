package com.vpedrosa.smarthome.wear.voice_control.domain.ports

/**
 * Driven port: sends a voice command (as text) to the main app
 * and receives the result of processing that command.
 *
 * The real implementation would use the Wearable Data Layer API
 * to communicate with the phone app. The phone app performs the
 * speech-to-text processing and command execution.
 */
interface VoiceCommandPort {

    /**
     * Sends audio/text captured on the watch to the main app for processing.
     * Returns a human-readable result string describing what was executed.
     */
    suspend fun sendCommand(transcribedText: String): VoiceCommandResult
}

sealed interface VoiceCommandResult {
    data class Success(val responseMessage: String) : VoiceCommandResult
    data class Error(val errorMessage: String) : VoiceCommandResult
}
