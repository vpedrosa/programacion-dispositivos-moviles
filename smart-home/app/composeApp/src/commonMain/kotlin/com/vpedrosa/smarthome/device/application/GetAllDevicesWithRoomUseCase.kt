package com.vpedrosa.smarthome.device.application

import com.vpedrosa.smarthome.shared.domain.model.Device
import com.vpedrosa.smarthome.shared.domain.DeviceRepository
import com.vpedrosa.smarthome.shared.domain.RoomRepository
import kotlinx.coroutines.flow.first

data class DeviceWithRoom(
    val device: Device,
    val roomName: String?,
)

class GetAllDevicesWithRoomUseCase(
    private val deviceRepository: DeviceRepository,
    private val roomRepository: RoomRepository,
) {
    suspend operator fun invoke(): List<DeviceWithRoom> {
        val devices = deviceRepository.observeAllDevices().first()
        val rooms = roomRepository.observeAllRooms().first()
        val roomMap = rooms.associateBy { it.id }

        return devices.map { device ->
            DeviceWithRoom(
                device = device,
                roomName = roomMap[device.roomId]?.name,
            )
        }
    }
}
