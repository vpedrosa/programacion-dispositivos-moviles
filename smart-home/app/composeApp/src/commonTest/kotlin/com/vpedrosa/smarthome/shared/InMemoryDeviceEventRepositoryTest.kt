package com.vpedrosa.smarthome.shared

import com.vpedrosa.smarthome.shared.infrastructure.persistence.InMemoryDeviceEventRepository
import com.vpedrosa.smarthome.shared.domain.model.DeviceEvent
import com.vpedrosa.smarthome.shared.domain.model.DeviceEventType
import com.vpedrosa.smarthome.shared.domain.model.DeviceId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InMemoryDeviceEventRepositoryTest {

    private val now = Instant.parse("2025-01-15T10:30:00Z")

    private val smokeAlert = DeviceEvent(
        id = "event-1",
        deviceId = DeviceId("sensor-cocina"),
        type = DeviceEventType.SMOKE_ALERT,
        message = "Smoke detected in kitchen",
        timestamp = now,
    )
    private val doorOpened = DeviceEvent(
        id = "event-2",
        deviceId = DeviceId("contact-puerta"),
        type = DeviceEventType.DOOR_OPENED,
        message = "Front door opened",
        timestamp = now,
    )
    private val doorClosed = DeviceEvent(
        id = "event-3",
        deviceId = DeviceId("contact-puerta"),
        type = DeviceEventType.DOOR_CLOSED,
        message = "Front door closed",
        timestamp = now,
    )

    @Test
    fun observeAllEvents_emptyByDefault() = runTest {
        val repo = InMemoryDeviceEventRepository()
        val result = repo.observeAllEvents().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun add_insertsEvent() = runTest {
        val repo = InMemoryDeviceEventRepository()
        repo.add(smokeAlert)
        val result = repo.observeAllEvents().first()
        assertEquals(1, result.size)
        assertEquals(smokeAlert, result.first())
    }

    @Test
    fun add_appendsMultipleEvents() = runTest {
        val repo = InMemoryDeviceEventRepository()
        repo.add(smokeAlert)
        repo.add(doorOpened)
        repo.add(doorClosed)
        val result = repo.observeAllEvents().first()
        assertEquals(3, result.size)
    }

    @Test
    fun observeEventsByDevice_filtersCorrectly() = runTest {
        val repo = InMemoryDeviceEventRepository()
        repo.add(smokeAlert)
        repo.add(doorOpened)
        repo.add(doorClosed)

        val doorEvents = repo.observeEventsByDevice(DeviceId("contact-puerta")).first()
        assertEquals(2, doorEvents.size)
        assertTrue(doorEvents.all { it.deviceId == DeviceId("contact-puerta") })

        val smokeEvents = repo.observeEventsByDevice(DeviceId("sensor-cocina")).first()
        assertEquals(1, smokeEvents.size)
    }

    @Test
    fun observeEventsByDevice_returnsEmptyForUnknownDevice() = runTest {
        val repo = InMemoryDeviceEventRepository()
        repo.add(smokeAlert)
        val result = repo.observeEventsByDevice(DeviceId("nonexistent")).first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun clear_removesAllEvents() = runTest {
        val repo = InMemoryDeviceEventRepository()
        repo.add(smokeAlert)
        repo.add(doorOpened)
        repo.clear()
        val result = repo.observeAllEvents().first()
        assertTrue(result.isEmpty())
    }
}
