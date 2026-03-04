package com.vpedrosa.smarthome.device.domain.usecases

import com.vpedrosa.smarthome.device.domain.Device
import com.vpedrosa.smarthome.device.domain.RoomId
import com.vpedrosa.smarthome.device.domain.ports.DeviceRepository
import kotlinx.coroutines.flow.Flow

class ObserveDevicesByRoomUseCase(
    private val deviceRepository: DeviceRepository,
) {
    operator fun invoke(roomId: RoomId): Flow<List<Device>> =
        deviceRepository.observeDevicesByRoom(roomId)
}
