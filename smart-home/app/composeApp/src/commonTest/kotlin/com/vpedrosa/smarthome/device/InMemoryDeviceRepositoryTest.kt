package com.vpedrosa.smarthome.device

import com.vpedrosa.smarthome.device.infrastructure.persistence.InMemoryDeviceRepository
import com.vpedrosa.smarthome.device.domain.model.Blind
import com.vpedrosa.smarthome.device.domain.model.Color
import com.vpedrosa.smarthome.device.domain.model.DeviceId
import com.vpedrosa.smarthome.device.domain.model.DeviceType
import com.vpedrosa.smarthome.device.domain.model.Light
import com.vpedrosa.smarthome.device.domain.model.Lock
import com.vpedrosa.smarthome.device.domain.model.RoomId
import com.vpedrosa.smarthome.device.domain.model.Switch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InMemoryDeviceRepositoryTest {

    private val light = Light(
        id = DeviceId("light-1"),
        name = "Test Light",
        roomId = RoomId("room-a"),
        isOn = false,
        color = Color.WHITE,
        brightness = 50,
    )
    private val lock = Lock(
        id = DeviceId("lock-1"),
        name = "Test Lock",
        roomId = RoomId("room-a"),
        isLocked = true,
    )
    private val blind = Blind(
        id = DeviceId("blind-1"),
        name = "Test Blind",
        roomId = RoomId("room-b"),
        openingLevel = 75,
    )

    private fun createRepository(vararg devices: com.vpedrosa.smarthome.device.domain.model.Device) =
        InMemoryDeviceRepository(initialDevices = devices.toList())

    @Test
    fun observeAllDevices_returnsInitialDevices() = runTest {
        val repo = createRepository(light, lock, blind)
        val result = repo.observeAllDevices().first()
        assertEquals(3, result.size)
    }

    @Test
    fun observeDevice_returnsCorrectDevice() = runTest {
        val repo = createRepository(light, lock)
        val result = repo.observeDevice(DeviceId("light-1")).first()
        assertNotNull(result)
        assertEquals("Test Light", result.name)
    }

    @Test
    fun observeDevice_returnsNullForMissing() = runTest {
        val repo = createRepository(light)
        val result = repo.observeDevice(DeviceId("nonexistent")).first()
        assertNull(result)
    }

    @Test
    fun observeDevicesByRoom_filtersCorrectly() = runTest {
        val repo = createRepository(light, lock, blind)
        val roomA = repo.observeDevicesByRoom(RoomId("room-a")).first()
        assertEquals(2, roomA.size)
        assertTrue(roomA.all { it.roomId == RoomId("room-a") })

        val roomB = repo.observeDevicesByRoom(RoomId("room-b")).first()
        assertEquals(1, roomB.size)
    }

    @Test
    fun observeDevicesByType_filtersCorrectly() = runTest {
        val repo = createRepository(light, lock, blind)
        val lights = repo.observeDevicesByType(DeviceType.LIGHT).first()
        assertEquals(1, lights.size)
        assertEquals(DeviceType.LIGHT, lights.first().type)
    }

    @Test
    fun save_addsNewDevice() = runTest {
        val repo = createRepository(light)
        val newSwitch = Switch(
            id = DeviceId("switch-1"),
            name = "New Switch",
            roomId = RoomId("room-a"),
            isOn = true,
        )
        repo.save(newSwitch)
        val all = repo.observeAllDevices().first()
        assertEquals(2, all.size)
    }

    @Test
    fun save_updatesExistingDevice() = runTest {
        val repo = createRepository(light)
        val toggled = light.toggle()
        repo.save(toggled)
        val result = repo.observeDevice(light.id).first()
        assertNotNull(result)
        assertTrue((result as Light).isOn)
    }

    @Test
    fun saveAll_addsBatch() = runTest {
        val repo = createRepository()
        repo.saveAll(listOf(light, lock, blind))
        val all = repo.observeAllDevices().first()
        assertEquals(3, all.size)
    }

    @Test
    fun delete_removesDevice() = runTest {
        val repo = createRepository(light, lock)
        repo.delete(light.id)
        val all = repo.observeAllDevices().first()
        assertEquals(1, all.size)
        assertNull(repo.observeDevice(light.id).first())
    }
}
