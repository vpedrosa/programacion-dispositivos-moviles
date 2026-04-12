package com.vpedrosa.smarthome.room.domain

import com.vpedrosa.smarthome.room.domain.model.Room
import com.vpedrosa.smarthome.device.domain.model.RoomId
import kotlinx.coroutines.flow.Flow

interface RoomRepository {
    fun observeAllRooms(): Flow<List<Room>>
    fun observeRoom(id: RoomId): Flow<Room?>
    suspend fun save(room: Room)
    suspend fun delete(id: RoomId)
}
