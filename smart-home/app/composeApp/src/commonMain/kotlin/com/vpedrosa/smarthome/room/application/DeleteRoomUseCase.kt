package com.vpedrosa.smarthome.room.application

import com.vpedrosa.smarthome.device.domain.model.RoomId
import com.vpedrosa.smarthome.room.domain.RoomRepository

class DeleteRoomUseCase(
    private val roomRepository: RoomRepository,
) {
    suspend operator fun invoke(roomId: RoomId) {
        roomRepository.delete(roomId)
    }
}
