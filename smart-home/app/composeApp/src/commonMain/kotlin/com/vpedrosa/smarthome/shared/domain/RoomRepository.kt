package com.vpedrosa.smarthome.shared.domain

import com.vpedrosa.smarthome.shared.domain.model.Room
import com.vpedrosa.smarthome.shared.domain.model.RoomId
import kotlinx.coroutines.flow.Flow

interface RoomRepository {
    fun observeAllRooms(): Flow<List<Room>>
    fun observeRoom(id: RoomId): Flow<Room?>
    suspend fun save(room: Room)
    suspend fun delete(id: RoomId)
}
