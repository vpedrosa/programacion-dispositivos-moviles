package com.vpedrosa.smarthome.voice.domain.model

import com.vpedrosa.smarthome.shared.domain.model.DeviceType

/**
 * Represents the result of parsing and executing a voice command.
 */
data class VoiceCommandResult(
    val success: Boolean,
    val message: String,
    val devicesAffected: Int,
)

/**
 * A recorded voice command with its result and timestamp.
 */
data class VoiceCommand(
    val text: String,
    val result: VoiceCommandResult,
    val timestamp: kotlin.time.Instant,
)

/**
 * Internal representation of a parsed voice command before execution.
 */
sealed interface ParsedVoiceCommand {

    data class ToggleDevices(
        val deviceType: DeviceType,
        val turnOn: Boolean,
        val roomName: String? = null,
    ) : ParsedVoiceCommand

    data class SetBlinds(
        val open: Boolean,
        val roomName: String? = null,
    ) : ParsedVoiceCommand

    data class SetThermostat(
        val targetTemperature: Double,
        val roomName: String? = null,
    ) : ParsedVoiceCommand

    data class ToggleLock(
        val lock: Boolean,
        val doorName: String? = null,
    ) : ParsedVoiceCommand

    data object Unknown : ParsedVoiceCommand
}
