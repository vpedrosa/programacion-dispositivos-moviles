package com.vpedrosa.smarthome.device.domain.usecases

import com.vpedrosa.smarthome.device.domain.Blind
import com.vpedrosa.smarthome.device.domain.DeviceType
import com.vpedrosa.smarthome.device.domain.Light
import com.vpedrosa.smarthome.device.domain.Lock
import com.vpedrosa.smarthome.device.domain.RoomId
import com.vpedrosa.smarthome.device.domain.SmartTv
import com.vpedrosa.smarthome.device.domain.Switch
import com.vpedrosa.smarthome.device.domain.Thermostat
import com.vpedrosa.smarthome.device.domain.ports.DeviceControlPort
import com.vpedrosa.smarthome.device.domain.ports.DeviceRepository
import com.vpedrosa.smarthome.device.domain.ports.RoomRepository
import kotlinx.coroutines.flow.first

class BulkToggleDevicesByTypeInRoomUseCase(
    private val deviceRepository: DeviceRepository,
    private val roomRepository: RoomRepository,
    private val deviceControlPort: DeviceControlPort,
) {
    suspend operator fun invoke(roomId: RoomId, type: DeviceType, turnOn: Boolean) {
        val room = roomRepository.observeRoom(roomId).first() ?: return
        val allDevices = deviceRepository.observeAllDevices().first()
        val deviceMap = allDevices.associateBy { it.id }

        val devices = room.deviceIds
            .mapNotNull { deviceMap[it] }
            .filter { it.type == type }

        val updated = devices.mapNotNull { device ->
            when (device) {
                is Light -> if (device.isOn != turnOn) {
                    deviceControlPort.toggleOnOff(device.id, turnOn)
                    device.copy(isOn = turnOn)
                } else null
                is Switch -> if (device.isOn != turnOn) {
                    deviceControlPort.toggleOnOff(device.id, turnOn)
                    device.copy(isOn = turnOn)
                } else null
                is SmartTv -> if (device.isOn != turnOn) {
                    deviceControlPort.toggleOnOff(device.id, turnOn)
                    device.copy(isOn = turnOn)
                } else null
                is Lock -> if (device.isLocked != turnOn) {
                    deviceControlPort.lockDoor(device.id, turnOn)
                    device.copy(isLocked = turnOn)
                } else null
                is Thermostat -> if (device.isHeatingOn != turnOn) {
                    deviceControlPort.setThermostatMode(device.id, turnOn)
                    device.copy(isHeatingOn = turnOn)
                } else null
                is Blind -> {
                    val targetLevel = if (turnOn) 100 else 0
                    if (device.openingLevel != targetLevel) {
                        deviceControlPort.setWindowCoveringPosition(device.id, targetLevel)
                        device.copy(openingLevel = targetLevel)
                    } else null
                }
                else -> null
            }
        }

        if (updated.isNotEmpty()) {
            deviceRepository.saveAll(updated)
        }
    }
}
