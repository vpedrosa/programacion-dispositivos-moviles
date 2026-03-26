package com.vpedrosa.smarthome.antisquatter

import com.vpedrosa.smarthome.antisquatter.infrastructure.persistence.InMemoryAntiSquatterRepository
import com.vpedrosa.smarthome.shared.infrastructure.persistence.InMemoryDeviceRepository
import com.vpedrosa.smarthome.shared.infrastructure.persistence.InMemoryDeviceEventRepository
import com.vpedrosa.smarthome.antisquatter.domain.model.AntiSquatterConfig
import com.vpedrosa.smarthome.shared.domain.model.Blind
import com.vpedrosa.smarthome.shared.domain.model.Color
import com.vpedrosa.smarthome.shared.domain.model.DeviceEvent
import com.vpedrosa.smarthome.shared.domain.model.DeviceId
import com.vpedrosa.smarthome.shared.domain.model.Light
import com.vpedrosa.smarthome.shared.domain.model.RoomId
import com.vpedrosa.smarthome.antisquatter.application.SimulatePresenceUseCase
import com.vpedrosa.smarthome.event.application.AddDeviceEventUseCase
import com.vpedrosa.smarthome.event.domain.NotificationPort
import com.vpedrosa.smarthome.settings.infrastructure.persistence.InMemoryAppSettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SimulatePresenceUseCaseTest {

    private val light1 = Light(
        id = DeviceId("light-1"),
        name = "Light 1",
        roomId = RoomId("room-1"),
        isOn = false,
        color = Color.WHITE,
        brightness = 80,
    )

    private val blind1 = Blind(
        id = DeviceId("blind-1"),
        name = "Blind 1",
        roomId = RoomId("room-1"),
        openingLevel = 0,
    )

    private val defaultConfig = AntiSquatterConfig(
        isEnabled = true,
        startHour = 21,
        startMinute = 0,
        endHour = 23,
        endMinute = 0,
        actionDurationMinutes = 30,
    )

    private val fakeNotificationPort = object : NotificationPort {
        override fun showSensorAlert(event: DeviceEvent) {}
    }

    private fun createUseCase(
        config: AntiSquatterConfig,
        devices: List<com.vpedrosa.smarthome.shared.domain.model.Device>,
    ): Triple<SimulatePresenceUseCase, InMemoryDeviceRepository, InMemoryAntiSquatterRepository> {
        val deviceRepo = InMemoryDeviceRepository(devices)
        val eventRepo = InMemoryDeviceEventRepository()
        val addEvent = AddDeviceEventUseCase(eventRepo, fakeNotificationPort, InMemoryAppSettingsRepository())
        val antiSquatterRepo = InMemoryAntiSquatterRepository()
        kotlinx.coroutines.runBlocking { antiSquatterRepo.saveConfig(config) }
        return Triple(SimulatePresenceUseCase(antiSquatterRepo, deviceRepo, addEvent), deviceRepo, antiSquatterRepo)
    }

    @Test
    fun doesNothingWhenDisabled() = runTest {
        val (useCase, deviceRepo, _) = createUseCase(
            defaultConfig.copy(isEnabled = false),
            listOf(light1),
        )
        useCase(hour = 21, minute = 0)

        val light = deviceRepo.observeAllDevices().first().first() as Light
        assertFalse(light.isOn, "Light should stay OFF when disabled")
    }

    @Test
    fun doesNothingOutsideTimeWindow() = runTest {
        val (useCase, deviceRepo, _) = createUseCase(
            defaultConfig,
            listOf(light1),
        )
        useCase(hour = 20, minute = 0) // Before start

        val light = deviceRepo.observeAllDevices().first().first() as Light
        assertFalse(light.isOn, "Light should stay OFF outside time window")
    }

    @Test
    fun togglesLightsDuringActionSlot() = runTest {
        // With 120min interval / 30min duration = 4 max actions
        // At minute 0 of interval (21:00), this is one of the possible action slots
        val (useCase, deviceRepo, _) = createUseCase(
            defaultConfig,
            listOf(light1),
        )
        // Run many times to ensure at least one toggle happens statistically
        var toggled = false
        repeat(20) {
            useCase(hour = 21, minute = 0)
            val light = deviceRepo.observeAllDevices().first().first() as Light
            if (light.isOn) toggled = true
        }
        // The probability of never hitting minute 0 in 20 attempts is very low
        assertTrue(toggled, "Light should have been toggled at least once")
    }

    @Test
    fun togglesBlindsDuringActionSlot() = runTest {
        val (useCase, deviceRepo, _) = createUseCase(
            defaultConfig,
            listOf(blind1),
        )
        var toggled = false
        repeat(20) {
            useCase(hour = 21, minute = 0)
            val blind = deviceRepo.observeAllDevices().first().first() as Blind
            if (blind.openingLevel > 0) toggled = true
        }
        assertTrue(toggled, "Blind should have been toggled at least once")
    }

    @Test
    fun validationRejectsDurationExceedingInterval() {
        val config = AntiSquatterConfig(
            isEnabled = true,
            startHour = 21,
            startMinute = 0,
            endHour = 21,
            endMinute = 15,
            actionDurationMinutes = 30,
        )
        assertFalse(config.isValid, "Config should be invalid when duration > interval")
    }

    @Test
    fun maxActionsCalculatedCorrectly() {
        val config = AntiSquatterConfig(
            isEnabled = true,
            startHour = 21,
            startMinute = 0,
            endHour = 23,
            endMinute = 0,
            actionDurationMinutes = 30,
        )
        assertTrue(config.maxActions == 4, "2h / 30min = 4 max actions")
    }
}
