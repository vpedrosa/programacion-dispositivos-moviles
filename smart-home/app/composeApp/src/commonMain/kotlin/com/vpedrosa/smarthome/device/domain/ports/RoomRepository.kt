package com.vpedrosa.smarthome.device.domain.ports

import com.vpedrosa.smarthome.device.domain.Room
import com.vpedrosa.smarthome.device.domain.RoomId
import kotlinx.coroutines.flow.Flow

interface RoomRepository {
    fun observeAllRooms(): Flow<List<Room>>
    fun observeRoom(id: RoomId): Flow<Room?>
    suspend fun save(room: Room)
    suspend fun delete(id: RoomId)
}
