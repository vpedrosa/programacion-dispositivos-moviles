package com.vpedrosa.smarthome.voice.application

import com.vpedrosa.smarthome.shared.domain.model.Blind
import com.vpedrosa.smarthome.shared.domain.model.Device
import com.vpedrosa.smarthome.shared.domain.model.DeviceType
import com.vpedrosa.smarthome.shared.domain.model.Lock
import com.vpedrosa.smarthome.voice.domain.model.ParsedVoiceCommand
import com.vpedrosa.smarthome.shared.domain.model.Thermostat
import com.vpedrosa.smarthome.voice.domain.model.VoiceCommandResult
import com.vpedrosa.smarthome.device.application.toggleDevice
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
) {
    suspend operator fun invoke(command: ParsedVoiceCommand): VoiceCommandResult {
        return when (command) {
            is ParsedVoiceCommand.ToggleDevices -> executeToggle(command)
            is ParsedVoiceCommand.SetBlinds -> executeBlinds(command)
            is ParsedVoiceCommand.SetThermostat -> executeThermostat(command)
            is ParsedVoiceCommand.ToggleLock -> executeLock(command)
            is ParsedVoiceCommand.Unknown -> VoiceCommandResult(
                success = false,
                message = "Command not recognized",
                devicesAffected = 0,
            )
        }
    }

    private suspend fun executeToggle(cmd: ParsedVoiceCommand.ToggleDevices): VoiceCommandResult {
        val devices = getDevicesByTypeAndRoom(cmd.deviceType, cmd.roomName)
        if (devices.isEmpty()) {
            return VoiceCommandResult(
                success = false,
                message = "No ${cmd.deviceType.name.lowercase()} devices found${roomSuffix(cmd.roomName)}",
                devicesAffected = 0,
            )
        }

        val toggled = devices.mapNotNull { device ->
            try {
                toggleDevice(device, cmd.turnOn, deviceControlPort)
            } catch (_: Exception) {
                null
            }
        }

        if (toggled.isNotEmpty()) {
            deviceRepository.saveAll(toggled)
        }

        val action = if (cmd.turnOn) "turned on" else "turned off"
        return VoiceCommandResult(
            success = toggled.isNotEmpty(),
            message = "${cmd.deviceType.name.lowercase().replaceFirstChar { it.uppercase() }} $action${roomSuffix(cmd.roomName)} (${toggled.size})",
            devicesAffected = toggled.size,
        )
    }

    private suspend fun executeBlinds(cmd: ParsedVoiceCommand.SetBlinds): VoiceCommandResult {
        val devices = getDevicesByTypeAndRoom(DeviceType.BLIND, cmd.roomName)
        if (devices.isEmpty()) {
            return VoiceCommandResult(
                success = false,
                message = "No blinds found${roomSuffix(cmd.roomName)}",
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
            message = "Blinds $action${roomSuffix(cmd.roomName)} (${updated.size})",
            devicesAffected = updated.size,
        )
    }

    private suspend fun executeThermostat(cmd: ParsedVoiceCommand.SetThermostat): VoiceCommandResult {
        val devices = getDevicesByTypeAndRoom(DeviceType.THERMOSTAT, cmd.roomName)
        if (devices.isEmpty()) {
            return VoiceCommandResult(
                success = false,
                message = "No thermostats found${roomSuffix(cmd.roomName)}",
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
            message = "Temperature set to ${cmd.targetTemperature.toInt()}\u00B0${roomSuffix(cmd.roomName)} (${updated.size})",
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
     * Returns devices of [type], optionally filtered by room name.
     * Room matching is case-insensitive and accent-insensitive.
     */
    private suspend fun getDevicesByTypeAndRoom(
        type: DeviceType,
        roomName: String?,
    ): List<Device> {
        val allOfType = deviceRepository.observeDevicesByType(type).first()
        if (roomName == null) return allOfType

        val rooms = roomRepository.observeAllRooms().first()
        val normalizedRoomName = roomName.normalizeForComparison()

        val matchingRoom = rooms.find { room ->
            room.name.lowercase().normalizeForComparison().contains(normalizedRoomName)
        } ?: return emptyList()

        val roomDeviceIds = matchingRoom.deviceIds.map { it.value }.toSet()
        return allOfType.filter { it.id.value in roomDeviceIds }
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
