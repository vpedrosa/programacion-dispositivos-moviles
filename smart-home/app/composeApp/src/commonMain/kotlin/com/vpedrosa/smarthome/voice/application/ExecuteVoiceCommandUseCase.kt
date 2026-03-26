package com.vpedrosa.smarthome.voice.application

import com.vpedrosa.smarthome.shared.domain.model.Blind
import com.vpedrosa.smarthome.shared.domain.model.Device
import com.vpedrosa.smarthome.shared.domain.model.DeviceType
import com.vpedrosa.smarthome.shared.domain.model.Lock
import com.vpedrosa.smarthome.shared.domain.model.RoomId
import com.vpedrosa.smarthome.voice.domain.model.ParsedVoiceCommand
import com.vpedrosa.smarthome.shared.domain.model.Thermostat
import com.vpedrosa.smarthome.voice.domain.model.VoiceCommandResult
import com.vpedrosa.smarthome.device.application.BulkToggleDevicesByTypeUseCase
import com.vpedrosa.smarthome.device.application.BulkToggleDevicesByTypeInRoomUseCase
import com.vpedrosa.smarthome.shared.domain.DeviceControlPort
import com.vpedrosa.smarthome.shared.domain.DeviceRepository
import com.vpedrosa.smarthome.shared.domain.RoomRepository
import kotlinx.coroutines.flow.first

/**
 * Executes a [ParsedVoiceCommand] by applying the corresponding actions
 * to the matching devices. Returns a [VoiceCommandResult] describing what happened.
 */
class ExecuteVoiceCommandUseCase(
    private val deviceRepository: DeviceRepository,
    private val roomRepository: RoomRepository,
    private val deviceControlPort: DeviceControlPort,
    private val bulkToggle: BulkToggleDevicesByTypeUseCase,
    private val bulkToggleInRoom: BulkToggleDevicesByTypeInRoomUseCase,
) {
    suspend operator fun invoke(command: ParsedVoiceCommand, rawText: String = ""): VoiceCommandResult {
        return when (command) {
            is ParsedVoiceCommand.ToggleDevices -> executeToggle(command, rawText)
            is ParsedVoiceCommand.SetBlinds -> executeBlinds(command, rawText)
            is ParsedVoiceCommand.SetThermostat -> executeThermostat(command, rawText)
            is ParsedVoiceCommand.ToggleLock -> executeLock(command)
            is ParsedVoiceCommand.Unknown -> VoiceCommandResult(
                success = false,
                message = "Command not recognized",
                devicesAffected = 0,
            )
        }
    }

    private suspend fun executeToggle(cmd: ParsedVoiceCommand.ToggleDevices, rawText: String): VoiceCommandResult {
        val room = findRoomInText(rawText)
        val affected = if (room != null) {
            bulkToggleInRoom(room.first, cmd.deviceType, cmd.turnOn)
        } else {
            bulkToggle(cmd.deviceType, cmd.turnOn)
        }

        val action = if (cmd.turnOn) "turned on" else "turned off"
        val roomLabel = room?.second
        return VoiceCommandResult(
            success = affected > 0,
            message = "${cmd.deviceType.name.lowercase().replaceFirstChar { it.uppercase() }} $action${roomSuffix(roomLabel)} ($affected)",
            devicesAffected = affected,
        )
    }

    private suspend fun executeBlinds(cmd: ParsedVoiceCommand.SetBlinds, rawText: String): VoiceCommandResult {
        val room = findRoomInText(rawText)
        val devices = getDevicesByTypeAndRoom(DeviceType.BLIND, room?.first)
        if (devices.isEmpty()) {
            return VoiceCommandResult(
                success = false,
                message = "No blinds found${roomSuffix(room?.second)}",
                devicesAffected = 0,
            )
        }

        val targetLevel = if (cmd.open) 100 else 0
        val updated = devices.filterIsInstance<Blind>()
            .filter { it.openingLevel != targetLevel }
            .mapNotNull { blind ->
                try {
                    deviceControlPort.setWindowCoveringPosition(blind.id, targetLevel)
                    blind.changeOpeningLevel(targetLevel)
                } catch (_: Exception) {
                    null
                }
            }

        if (updated.isNotEmpty()) {
            deviceRepository.saveAll(updated)
        }

        val action = if (cmd.open) "opened" else "closed"
        return VoiceCommandResult(
            success = updated.isNotEmpty(),
            message = "Blinds $action${roomSuffix(room?.second)} (${updated.size})",
            devicesAffected = updated.size,
        )
    }

    private suspend fun executeThermostat(cmd: ParsedVoiceCommand.SetThermostat, rawText: String): VoiceCommandResult {
        val room = findRoomInText(rawText)
        val devices = getDevicesByTypeAndRoom(DeviceType.THERMOSTAT, room?.first)
        if (devices.isEmpty()) {
            return VoiceCommandResult(
                success = false,
                message = "No thermostats found${roomSuffix(room?.second)}",
                devicesAffected = 0,
            )
        }

        val updated = devices.filterIsInstance<Thermostat>()
            .mapNotNull { thermostat ->
                try {
                    deviceControlPort.setThermostatSetpoint(thermostat.id, cmd.targetTemperature)
                    thermostat.adjustTarget(cmd.targetTemperature)
                } catch (_: Exception) {
                    null
                }
            }

        if (updated.isNotEmpty()) {
            deviceRepository.saveAll(updated)
        }

        return VoiceCommandResult(
            success = updated.isNotEmpty(),
            message = "Temperature set to ${cmd.targetTemperature.toInt()}\u00B0${roomSuffix(room?.second)} (${updated.size})",
            devicesAffected = updated.size,
        )
    }

    private suspend fun executeLock(cmd: ParsedVoiceCommand.ToggleLock): VoiceCommandResult {
        val allLocks = deviceRepository.observeDevicesByType(DeviceType.LOCK).first()
            .filterIsInstance<Lock>()

        // Filter by door name if specified
        val locks = if (cmd.doorName != null) {
            allLocks.filter { lock ->
                lock.name.lowercase().contains(cmd.doorName.lowercase())
            }
        } else {
            allLocks
        }

        if (locks.isEmpty()) {
            val suffix = cmd.doorName?.let { " ($it)" } ?: ""
            return VoiceCommandResult(
                success = false,
                message = "No locks found$suffix",
                devicesAffected = 0,
            )
        }

        val updated = locks
            .filter { it.isLocked != cmd.lock }
            .mapNotNull { lock ->
                try {
                    deviceControlPort.lockDoor(lock.id, cmd.lock)
                    lock.toggle()
                } catch (_: Exception) {
                    null
                }
            }

        if (updated.isNotEmpty()) {
            deviceRepository.saveAll(updated)
        }

        val action = if (cmd.lock) "locked" else "unlocked"
        return VoiceCommandResult(
            success = updated.isNotEmpty(),
            message = "Door $action (${updated.size})",
            devicesAffected = updated.size,
        )
    }

    /**
     * Returns devices of [type], optionally filtered by room ID.
     */
    private suspend fun getDevicesByTypeAndRoom(
        type: DeviceType,
        roomId: RoomId?,
    ): List<Device> {
        val allOfType = deviceRepository.observeDevicesByType(type).first()
        if (roomId == null) return allOfType

        val room = roomRepository.observeRoom(roomId).first() ?: return allOfType
        val roomDeviceIds = room.deviceIds.map { it.value }.toSet()
        return allOfType.filter { it.id.value in roomDeviceIds }
    }

    /**
     * Searches for any known room name in the raw command text.
     * Returns (RoomId, displayName) if found, null otherwise.
     */
    private suspend fun findRoomInText(text: String): Pair<RoomId, String>? {
        if (text.isBlank()) return null
        val normalizedText = text.normalizeForComparison()
        val rooms = roomRepository.observeAllRooms().first()
        return rooms
            .sortedByDescending { it.name.length }
            .firstOrNull { room ->
                val normalizedName = room.name.normalizeForComparison()
                normalizedText.contains(normalizedName)
            }
            ?.let { it.id to it.name }
    }

    private fun roomSuffix(roomName: String?): String =
        if (roomName != null) " in $roomName" else ""

    private fun String.normalizeForComparison(): String =
        this.lowercase()
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
}
