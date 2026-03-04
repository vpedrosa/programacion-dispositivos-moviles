package com.vpedrosa.smarthome.device.domain.usecases

import com.vpedrosa.smarthome.device.domain.RoomId
import com.vpedrosa.smarthome.device.domain.ports.RoomRepository

class DeleteRoomUseCase(
    private val roomRepository: RoomRepository,
) {
    suspend operator fun invoke(roomId: RoomId) {
        roomRepository.delete(roomId)
    }
}
