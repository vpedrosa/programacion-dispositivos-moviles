package com.vpedrosa.smarthome.room

import com.vpedrosa.smarthome.room.infrastructure.InMemoryRoomRepository
import com.vpedrosa.smarthome.device.domain.model.DeviceId
import com.vpedrosa.smarthome.room.domain.model.Room
import com.vpedrosa.smarthome.device.domain.model.RoomId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class InMemoryRoomRepositoryTest {

    private val room = Room(
        id = RoomId("room-1"),
        name = "Living Room",
        photoUri = null,
        deviceIds = listOf(DeviceId("d-1"), DeviceId("d-2")),
    )

    @Test
    fun observeAllRooms_returnsInitialRooms() = runTest {
        val repo = InMemoryRoomRepository(initialRooms = listOf(room))
        val result = repo.observeAllRooms().first()
        assertEquals(1, result.size)
        assertEquals("Living Room", result.first().name)
    }

    @Test
    fun observeRoom_returnsCorrectRoom() = runTest {
        val repo = InMemoryRoomRepository(initialRooms = listOf(room))
        val result = repo.observeRoom(RoomId("room-1")).first()
        assertNotNull(result)
        assertEquals(2, result.deviceIds.size)
    }

    @Test
    fun observeRoom_returnsNullForMissing() = runTest {
        val repo = InMemoryRoomRepository(initialRooms = emptyList())
        val result = repo.observeRoom(RoomId("nonexistent")).first()
        assertNull(result)
    }

    @Test
    fun save_addsNewRoom() = runTest {
        val repo = InMemoryRoomRepository(initialRooms = emptyList())
        repo.save(room)
        val result = repo.observeAllRooms().first()
        assertEquals(1, result.size)
    }

    @Test
    fun save_updatesExistingRoom() = runTest {
        val repo = InMemoryRoomRepository(initialRooms = listOf(room))
        val updated = room.addDevice(DeviceId("d-3"))
        repo.save(updated)
        val result = repo.observeRoom(room.id).first()
        assertNotNull(result)
        assertEquals(3, result.deviceIds.size)
    }

    @Test
    fun delete_removesRoom() = runTest {
        val repo = InMemoryRoomRepository(initialRooms = listOf(room))
        repo.delete(room.id)
        val result = repo.observeAllRooms().first()
        assertEquals(0, result.size)
    }
}
