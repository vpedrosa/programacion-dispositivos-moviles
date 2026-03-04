package com.vpedrosa.smarthome.device.domain.usecases

import com.vpedrosa.smarthome.device.domain.Room
import com.vpedrosa.smarthome.device.domain.ports.RoomRepository
import kotlinx.coroutines.flow.Flow

class ObserveAllRoomsUseCase(
    private val roomRepository: RoomRepository,
) {
    operator fun invoke(): Flow<List<Room>> =
        roomRepository.observeAllRooms()
}
