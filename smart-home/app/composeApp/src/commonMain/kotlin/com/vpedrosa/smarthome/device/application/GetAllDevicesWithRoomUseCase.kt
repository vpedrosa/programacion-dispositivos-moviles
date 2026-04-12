package com.vpedrosa.smarthome.device.application

import com.vpedrosa.smarthome.device.domain.DeviceRepository
import com.vpedrosa.smarthome.device.domain.model.Device
import com.vpedrosa.smarthome.room.domain.RoomRepository
import kotlinx.coroutines.flow.first

data class DeviceWithRoom(val device: Device, val roomName: String?)

class GetAllDevicesWithRoomUseCase(
    private val deviceRepository: DeviceRepository,
    private val roomRepository: RoomRepository,
) {
    suspend operator fun invoke(): List<DeviceWithRoom> {
        val devices = deviceRepository.observeAllDevices().first()
        val rooms = roomRepository.observeAllRooms().first()
        val roomNameById = rooms.associate { it.id to it.name }
        return devices.map { device ->
            DeviceWithRoom(
                device = device,
                roomName = device.roomId?.let { roomNameById[it] },
            )
        }
    }
}
