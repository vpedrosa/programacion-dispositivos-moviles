package com.vpedrosa.smarthome.antisquatter

import com.vpedrosa.smarthome.antisquatter.infrastructure.persistence.InMemoryAntiSquatterRepository
import com.vpedrosa.smarthome.shared.infrastructure.persistence.InMemoryDeviceRepository
import com.vpedrosa.smarthome.antisquatter.domain.model.AntiSquatterConfig
import com.vpedrosa.smarthome.shared.domain.model.Color
import com.vpedrosa.smarthome.shared.domain.model.DeviceId
import com.vpedrosa.smarthome.shared.domain.model.Light
import com.vpedrosa.smarthome.antisquatter.domain.model.LightTimeSlot
import com.vpedrosa.smarthome.shared.domain.model.RoomId
import com.vpedrosa.smarthome.shared.domain.model.SmartTv
import com.vpedrosa.smarthome.antisquatter.domain.model.VideoConfig
import com.vpedrosa.smarthome.antisquatter.application.SimulatePresenceUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SimulatePresenceUseCaseTest {

    private val salonId = RoomId("room-salon")
    private val dormitorioId = RoomId("room-dormitorio")

    private val lightSalon = Light(
        id = DeviceId("light-salon"),
        name = "Salon Light",
        roomId = salonId,
        isOn = false,
        color = Color.WHITE,
        brightness = 80,
    )

    private val lightDormitorio = Light(
        id = DeviceId("light-dorm"),
        name = "Dorm Light",
        roomId = dormitorioId,
        isOn = false,
        color = Color.WHITE,
        brightness = 60,
    )

    private val smartTv = SmartTv(
        id = DeviceId("tv-salon"),
        name = "Salon TV",
        roomId = salonId,
        isOn = false,
    )

    private val defaultVideoConfig = VideoConfig(
        isEnabled = false,
        startHour = 20,
        startMinute = 0,
        endHour = 22,
        endMinute = 0,
        videoUrl = "",
    )

    private val enabledVideoConfig = VideoConfig(
        isEnabled = true,
        startHour = 20,
        startMinute = 0,
        endHour = 22,
        endMinute = 0,
        videoUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
    )

    private val defaultSlot = LightTimeSlot(
        id = "slot-1",
        startHour = 18,
        startMinute = 0,
        endHour = 20,
        endMinute = 0,
        roomIds = listOf(salonId),
    )

    @Test
    fun turnsOnLightsInActiveSlotRooms() = runTest {
        val deviceRepo = InMemoryDeviceRepository(listOf(lightSalon, lightDormitorio))
        val config = AntiSquatterConfig(
            isEnabled = true,
            timeSlots = listOf(defaultSlot),
            videoConfig = defaultVideoConfig,
        )
        val antiSquatterRepo = InMemoryAntiSquatterRepository()
        antiSquatterRepo.saveConfig(config)

        val useCase = SimulatePresenceUseCase(antiSquatterRepo, deviceRepo)
        useCase(hour = 19, minute = 0)

        val devices = deviceRepo.observeAllDevices().first()
        val salon = devices.first { it.id == lightSalon.id } as Light
        val dorm = devices.first { it.id == lightDormitorio.id } as Light

        assertTrue(salon.isOn, "Salon light should be ON during active slot")
        assertFalse(dorm.isOn, "Dormitorio light should remain OFF (not in any configured slot)")
    }

    @Test
    fun turnsOffLightsOutsideActiveSlot() = runTest {
        val salonOn = lightSalon.copy(isOn = true)
        val deviceRepo = InMemoryDeviceRepository(listOf(salonOn, lightDormitorio))
        val config = AntiSquatterConfig(
            isEnabled = true,
            timeSlots = listOf(defaultSlot),
            videoConfig = defaultVideoConfig,
        )
        val antiSquatterRepo = InMemoryAntiSquatterRepository()
        antiSquatterRepo.saveConfig(config)

        val useCase = SimulatePresenceUseCase(antiSquatterRepo, deviceRepo)
        useCase(hour = 21, minute = 0)

        val devices = deviceRepo.observeAllDevices().first()
        val salon = devices.first { it.id == lightSalon.id } as Light
        assertFalse(salon.isOn, "Salon light should be OFF outside active slot")
    }

    @Test
    fun doesNothingWhenDisabled() = runTest {
        val deviceRepo = InMemoryDeviceRepository(listOf(lightSalon))
        val config = AntiSquatterConfig(
            isEnabled = false,
            timeSlots = listOf(defaultSlot),
            videoConfig = defaultVideoConfig,
        )
        val antiSquatterRepo = InMemoryAntiSquatterRepository()
        antiSquatterRepo.saveConfig(config)

        val useCase = SimulatePresenceUseCase(antiSquatterRepo, deviceRepo)
        useCase(hour = 19, minute = 0)

        val devices = deviceRepo.observeAllDevices().first()
        val salon = devices.first { it.id == lightSalon.id } as Light
        assertFalse(salon.isOn, "Light should stay OFF when anti-squatter is disabled")
    }

    @Test
    fun handlesMultipleSlotsWithOverlappingRooms() = runTest {
        val deviceRepo = InMemoryDeviceRepository(listOf(lightSalon, lightDormitorio))
        val config = AntiSquatterConfig(
            isEnabled = true,
            timeSlots = listOf(
                defaultSlot,
                LightTimeSlot(
                    id = "slot-2",
                    startHour = 19,
                    startMinute = 0,
                    endHour = 22,
                    endMinute = 0,
                    roomIds = listOf(salonId, dormitorioId),
                ),
            ),
            videoConfig = defaultVideoConfig,
        )
        val antiSquatterRepo = InMemoryAntiSquatterRepository()
        antiSquatterRepo.saveConfig(config)

        val useCase = SimulatePresenceUseCase(antiSquatterRepo, deviceRepo)
        useCase(hour = 19, minute = 30)

        val devices = deviceRepo.observeAllDevices().first()
        val salon = devices.first { it.id == lightSalon.id } as Light
        val dorm = devices.first { it.id == lightDormitorio.id } as Light

        assertTrue(salon.isOn, "Salon light should be ON (in both slots)")
        assertTrue(dorm.isOn, "Dormitorio light should be ON (in slot-2)")
    }

    // --- Smart TV tests (TV now uses same time slots as lights) ---

    @Test
    fun turnsOnTvDuringActiveLightSlot() = runTest {
        val deviceRepo = InMemoryDeviceRepository(listOf(smartTv))
        val config = AntiSquatterConfig(
            isEnabled = true,
            timeSlots = listOf(defaultSlot), // 18:00-20:00
            videoConfig = enabledVideoConfig,
        )
        val antiSquatterRepo = InMemoryAntiSquatterRepository()
        antiSquatterRepo.saveConfig(config)

        val useCase = SimulatePresenceUseCase(antiSquatterRepo, deviceRepo)
        useCase(hour = 19, minute = 0)

        val devices = deviceRepo.observeAllDevices().first()
        val tv = devices.first { it.id == smartTv.id } as SmartTv

        assertTrue(tv.isOn, "TV should be ON during active light slot")
        assertTrue(tv.isCasting, "TV should be casting during active light slot")
    }

    @Test
    fun turnsOffTvOutsideLightSlots() = runTest {
        val tvOn = smartTv.copy(isOn = true, contentUrl = "https://example.com/video")
        val deviceRepo = InMemoryDeviceRepository(listOf(tvOn))
        val config = AntiSquatterConfig(
            isEnabled = true,
            timeSlots = listOf(defaultSlot), // 18:00-20:00
            videoConfig = enabledVideoConfig,
        )
        val antiSquatterRepo = InMemoryAntiSquatterRepository()
        antiSquatterRepo.saveConfig(config)

        val useCase = SimulatePresenceUseCase(antiSquatterRepo, deviceRepo)
        useCase(hour = 21, minute = 0)

        val devices = deviceRepo.observeAllDevices().first()
        val tv = devices.first { it.id == smartTv.id } as SmartTv

        assertFalse(tv.isOn, "TV should be OFF outside light slots")
        assertFalse(tv.isCasting, "TV should not be casting outside light slots")
    }

    @Test
    fun doesNotTouchTvWhenVideoConfigIsDisabled() = runTest {
        val deviceRepo = InMemoryDeviceRepository(listOf(smartTv))
        val config = AntiSquatterConfig(
            isEnabled = true,
            timeSlots = listOf(defaultSlot),
            videoConfig = defaultVideoConfig, // isEnabled = false
        )
        val antiSquatterRepo = InMemoryAntiSquatterRepository()
        antiSquatterRepo.saveConfig(config)

        val useCase = SimulatePresenceUseCase(antiSquatterRepo, deviceRepo)
        useCase(hour = 19, minute = 0)

        val devices = deviceRepo.observeAllDevices().first()
        val tv = devices.first { it.id == smartTv.id } as SmartTv

        assertFalse(tv.isOn, "TV should stay OFF when video config is disabled")
        assertFalse(tv.isCasting, "TV should not be casting when video config is disabled")
    }

    @Test
    fun handlesLightsAndTvSimultaneously() = runTest {
        val slot = LightTimeSlot(
            id = "slot-1",
            startHour = 20,
            startMinute = 0,
            endHour = 23,
            endMinute = 0,
            roomIds = listOf(salonId),
        )
        val deviceRepo = InMemoryDeviceRepository(listOf(lightSalon, smartTv))
        val config = AntiSquatterConfig(
            isEnabled = true,
            timeSlots = listOf(slot),
            videoConfig = enabledVideoConfig,
        )
        val antiSquatterRepo = InMemoryAntiSquatterRepository()
        antiSquatterRepo.saveConfig(config)

        val useCase = SimulatePresenceUseCase(antiSquatterRepo, deviceRepo)
        useCase(hour = 21, minute = 0)

        val devices = deviceRepo.observeAllDevices().first()
        val salon = devices.first { it.id == lightSalon.id } as Light
        val tv = devices.first { it.id == smartTv.id } as SmartTv

        assertTrue(salon.isOn, "Salon light should be ON during active slot")
        assertTrue(tv.isOn, "TV should be ON during active light slot")
        assertTrue(tv.isCasting, "TV should be casting during active light slot")
    }
}
