package com.vpedrosa.smarthome.event

import com.vpedrosa.smarthome.shared.infrastructure.persistence.InMemoryDeviceEventRepository
import com.vpedrosa.smarthome.shared.domain.model.DeviceEvent
import com.vpedrosa.smarthome.shared.domain.model.DeviceEventType
import com.vpedrosa.smarthome.shared.domain.model.DeviceId
import com.vpedrosa.smarthome.event.domain.NotificationPort
import com.vpedrosa.smarthome.event.application.AddDeviceEventUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class AddDeviceEventUseCaseTest {

    private val eventRepository = InMemoryDeviceEventRepository()
    private val fakeNotification = FakeNotificationPort()
    private val useCase = AddDeviceEventUseCase(eventRepository, fakeNotification)

    @Test
    fun smokeAlert_persistsEventAndShowsNotification() = runTest {
        val event = createEvent(DeviceEventType.SMOKE_ALERT, "Smoke detected")

        useCase(event)

        val stored = eventRepository.observeAllEvents().first()
        assertEquals(1, stored.size)
        assertEquals(event, stored.first())
        assertEquals(listOf(event), fakeNotification.shownEvents)
    }

    @Test
    fun waterLeakAlert_persistsEventAndShowsNotification() = runTest {
        val event = createEvent(DeviceEventType.WATER_LEAK_ALERT, "Water leak detected")

        useCase(event)

        assertEquals(1, eventRepository.observeAllEvents().first().size)
        assertEquals(listOf(event), fakeNotification.shownEvents)
    }

    @Test
    fun doorOpenTooLong_persistsEventAndShowsNotification() = runTest {
        val event = createEvent(DeviceEventType.DOOR_OPEN_TOO_LONG, "Door open 5 min")

        useCase(event)

        assertEquals(1, eventRepository.observeAllEvents().first().size)
        assertEquals(listOf(event), fakeNotification.shownEvents)
    }

    @Test
    fun temperatureReading_persistsAndShowsNotification() = runTest {
        val event = createEvent(DeviceEventType.TEMPERATURE_READING, "22.5C")

        useCase(event)

        assertEquals(1, eventRepository.observeAllEvents().first().size)
        assertEquals(listOf(event), fakeNotification.shownEvents)
    }

    @Test
    fun doorOpened_persistsAndShowsNotification() = runTest {
        val event = createEvent(DeviceEventType.DOOR_OPENED, "Front door opened")

        useCase(event)

        assertEquals(1, eventRepository.observeAllEvents().first().size)
        assertEquals(listOf(event), fakeNotification.shownEvents)
    }

    @Test
    fun multipleEvents_notifiesForEach() = runTest {
        val smoke = createEvent(DeviceEventType.SMOKE_ALERT, "Smoke", id = "evt-1")
        val water = createEvent(DeviceEventType.WATER_LEAK_ALERT, "Water", id = "evt-2")
        val temp = createEvent(DeviceEventType.TEMPERATURE_READING, "22C", id = "evt-3")

        useCase(smoke)
        useCase(water)
        useCase(temp)

        assertEquals(3, eventRepository.observeAllEvents().first().size)
        assertEquals(3, fakeNotification.shownEvents.size)
    }

    private fun createEvent(
        type: DeviceEventType,
        message: String,
        id: String = "evt-test",
    ) = DeviceEvent(
        id = id,
        deviceId = DeviceId("device-1"),
        type = type,
        message = message,
        timestamp = Instant.fromEpochMilliseconds(1_000_000L),
    )
}

/** Fake implementation of [NotificationPort] that records all calls for assertion. */
private class FakeNotificationPort : NotificationPort {
    val shownEvents = mutableListOf<DeviceEvent>()

    override fun showSensorAlert(event: DeviceEvent) {
        shownEvents.add(event)
    }
}
