package com.vpedrosa.smarthome.device.adapters.persistence

import com.vpedrosa.smarthome.device.domain.Room
import com.vpedrosa.smarthome.device.domain.RoomId
import com.vpedrosa.smarthome.device.domain.ports.RoomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class InMemoryRoomRepository(
    initialRooms: List<Room> = DefaultDeviceData.rooms,
) : RoomRepository {

    private val store = MutableStateFlow(
        initialRooms.associateBy { it.id }
    )

    override fun observeAllRooms(): Flow<List<Room>> =
        store.map { it.values.toList() }

    override fun observeRoom(id: RoomId): Flow<Room?> =
        store.map { it[id] }

    override suspend fun save(room: Room) {
        store.update { it + (room.id to room) }
    }

    override suspend fun delete(id: RoomId) {
        store.update { it - id }
    }
}
