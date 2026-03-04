package com.vpedrosa.smarthome.device.domain.usecases

import com.vpedrosa.smarthome.device.domain.Room
import com.vpedrosa.smarthome.device.domain.RoomId
import com.vpedrosa.smarthome.device.domain.ports.RoomRepository
import kotlinx.coroutines.flow.Flow

class ObserveRoomUseCase(
    private val roomRepository: RoomRepository,
) {
    operator fun invoke(id: RoomId): Flow<Room?> =
        roomRepository.observeRoom(id)
}
