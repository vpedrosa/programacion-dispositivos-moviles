package com.vpedrosa.smarthome.device.domain.usecases

import com.vpedrosa.smarthome.device.domain.Room
import com.vpedrosa.smarthome.device.domain.ports.RoomRepository

class SaveRoomUseCase(
    private val roomRepository: RoomRepository,
) {
    suspend operator fun invoke(room: Room) {
        roomRepository.save(room)
    }
}
