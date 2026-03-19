package com.vpedrosa.smarthome.device.application

import com.vpedrosa.smarthome.shared.domain.model.DeviceType
import com.vpedrosa.smarthome.shared.domain.model.RoomId
import com.vpedrosa.smarthome.shared.domain.DeviceControlPort
import com.vpedrosa.smarthome.shared.domain.DeviceRepository
import com.vpedrosa.smarthome.shared.domain.RoomRepository
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
            toggleDevice(device, turnOn, deviceControlPort)
        }

        if (updated.isNotEmpty()) {
            deviceRepository.saveAll(updated)
        }
    }
}
