package com.vpedrosa.smarthome.event

import com.vpedrosa.smarthome.shared.infrastructure.persistence.InMemoryDeviceEventRepository
import com.vpedrosa.smarthome.shared.domain.model.DeviceEvent
import com.vpedrosa.smarthome.shared.domain.model.DeviceEventType
import com.vpedrosa.smarthome.shared.domain.model.DeviceId
import com.vpedrosa.smarthome.event.domain.NotificationPort
import com.vpedrosa.smarthome.event.application.AddDeviceEventUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AddDeviceEventUseCaseTest {

    private val eventRepository = InMemoryDeviceEventRepository()
    private val fakeNotification = FakeNotificationPort()
    private val useCase = AddDeviceEventUseCase(eventRepository, fakeNotification)

    // region Critical events trigger notification

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

    // endregion

    // region Non-critical events do NOT trigger notification

    @Test
    fun temperatureReading_persistsButNoNotification() = runTest {
        val event = createEvent(DeviceEventType.TEMPERATURE_READING, "22.5C")

        useCase(event)

        assertEquals(1, eventRepository.observeAllEvents().first().size)
        assertTrue(fakeNotification.shownEvents.isEmpty())
    }

    @Test
    fun doorOpened_persistsButNoNotification() = runTest {
        val event = createEvent(DeviceEventType.DOOR_OPENED, "Front door opened")

        useCase(event)

        assertEquals(1, eventRepository.observeAllEvents().first().size)
        assertTrue(fakeNotification.shownEvents.isEmpty())
    }

    @Test
    fun doorClosed_persistsButNoNotification() = runTest {
        val event = createEvent(DeviceEventType.DOOR_CLOSED, "Front door closed")

        useCase(event)

        assertEquals(1, eventRepository.observeAllEvents().first().size)
        assertTrue(fakeNotification.shownEvents.isEmpty())
    }

    @Test
    fun thermostatAdjusted_persistsButNoNotification() = runTest {
        val event = createEvent(DeviceEventType.THERMOSTAT_ADJUSTED, "Set to 23C")

        useCase(event)

        assertEquals(1, eventRepository.observeAllEvents().first().size)
        assertTrue(fakeNotification.shownEvents.isEmpty())
    }

    @Test
    fun deviceTurnedOn_persistsButNoNotification() = runTest {
        val event = createEvent(DeviceEventType.DEVICE_TURNED_ON, "Light on")

        useCase(event)

        assertEquals(1, eventRepository.observeAllEvents().first().size)
        assertTrue(fakeNotification.shownEvents.isEmpty())
    }

    @Test
    fun deviceTurnedOff_persistsButNoNotification() = runTest {
        val event = createEvent(DeviceEventType.DEVICE_TURNED_OFF, "Light off")

        useCase(event)

        assertEquals(1, eventRepository.observeAllEvents().first().size)
        assertTrue(fakeNotification.shownEvents.isEmpty())
    }

    // endregion

    // region Multiple events

    @Test
    fun multipleCriticalEvents_notifiesForEach() = runTest {
        val smoke = createEvent(DeviceEventType.SMOKE_ALERT, "Smoke", id = "evt-1")
        val water = createEvent(DeviceEventType.WATER_LEAK_ALERT, "Water", id = "evt-2")
        val temp = createEvent(DeviceEventType.TEMPERATURE_READING, "22C", id = "evt-3")

        useCase(smoke)
        useCase(water)
        useCase(temp)

        assertEquals(3, eventRepository.observeAllEvents().first().size)
        assertEquals(2, fakeNotification.shownEvents.size)
        assertEquals(smoke, fakeNotification.shownEvents[0])
        assertEquals(water, fakeNotification.shownEvents[1])
    }

    // endregion

    // region Helpers

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

    // endregion
}

/** Fake implementation of [NotificationPort] that records all calls for assertion. */
private class FakeNotificationPort : NotificationPort {
    val shownEvents = mutableListOf<DeviceEvent>()

    override fun showSensorAlert(event: DeviceEvent) {
        shownEvents.add(event)
    }
}
