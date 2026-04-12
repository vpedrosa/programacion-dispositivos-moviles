package com.vpedrosa.smarthome.device.application

import com.vpedrosa.smarthome.device.domain.model.DeviceType
import com.vpedrosa.smarthome.device.domain.model.RoomId
import com.vpedrosa.smarthome.device.domain.DeviceControlPort
import com.vpedrosa.smarthome.device.domain.DeviceRepository
import com.vpedrosa.smarthome.room.domain.RoomRepository
import com.vpedrosa.smarthome.device.domain.toggleDevice
import kotlinx.coroutines.flow.first

class BulkToggleDevicesByTypeInRoomUseCase(
    private val deviceRepository: DeviceRepository,
    private val roomRepository: RoomRepository,
    private val deviceControlPort: DeviceControlPort,
) {
    suspend operator fun invoke(roomId: RoomId, type: DeviceType, turnOn: Boolean): Int {
        val room = roomRepository.observeRoom(roomId).first() ?: return 0
        val allDevices = deviceRepository.observeAllDevices().first()
        val deviceMap = allDevices.associateBy { it.id }

        val devices = room.deviceIds
            .mapNotNull { deviceMap[it] }
            .filter { it.type == type }

        val updated = devices.mapNotNull { device ->
            toggleDevice(device, turnOn, deviceControlPort)
        }

        if (updated.isNotEmpty()) {
            deviceRepository.saveAll(updated)
        }
        return updated.size
    }
}
