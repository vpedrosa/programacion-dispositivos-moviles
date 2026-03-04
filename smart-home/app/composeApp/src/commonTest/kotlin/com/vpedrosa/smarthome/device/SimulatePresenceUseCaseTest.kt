package com.vpedrosa.smarthome.device

import com.vpedrosa.smarthome.device.adapters.persistence.InMemoryAntiSquatterRepository
import com.vpedrosa.smarthome.device.adapters.persistence.InMemoryDeviceRepository
import com.vpedrosa.smarthome.device.domain.AntiSquatterConfig
import com.vpedrosa.smarthome.device.domain.Color
import com.vpedrosa.smarthome.device.domain.DeviceId
import com.vpedrosa.smarthome.device.domain.Light
import com.vpedrosa.smarthome.device.domain.LightTimeSlot
import com.vpedrosa.smarthome.device.domain.RoomId
import com.vpedrosa.smarthome.device.domain.VideoConfig
import com.vpedrosa.smarthome.device.domain.usecases.SimulatePresenceUseCase
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
        roomId = salonId.value,
        isOn = false,
        color = Color.WHITE,
        brightness = 80,
    )

    private val lightDormitorio = Light(
        id = DeviceId("light-dorm"),
        name = "Dorm Light",
        roomId = dormitorioId.value,
        isOn = false,
        color = Color.WHITE,
        brightness = 60,
    )

    private val defaultVideoConfig = VideoConfig(
        isEnabled = false,
        startHour = 20,
        startMinute = 0,
        endHour = 22,
        endMinute = 0,
        videoUrl = "",
    )

    @Test
    fun turnsOnLightsInActiveSlotRooms() = runTest {
        val deviceRepo = InMemoryDeviceRepository(listOf(lightSalon, lightDormitorio))
        val config = AntiSquatterConfig(
            isEnabled = true,
            timeSlots = listOf(
                LightTimeSlot(
                    id = "slot-1",
                    startHour = 18,
                    startMinute = 0,
                    endHour = 20,
                    endMinute = 0,
                    roomIds = listOf(salonId),
                ),
            ),
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
            timeSlots = listOf(
                LightTimeSlot(
                    id = "slot-1",
                    startHour = 18,
                    startMinute = 0,
                    endHour = 20,
                    endMinute = 0,
                    roomIds = listOf(salonId),
                ),
            ),
            videoConfig = defaultVideoConfig,
        )
        val antiSquatterRepo = InMemoryAntiSquatterRepository()
        antiSquatterRepo.saveConfig(config)

        val useCase = SimulatePresenceUseCase(antiSquatterRepo, deviceRepo)
        // 21:00 is outside the 18:00-20:00 slot
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
            timeSlots = listOf(
                LightTimeSlot(
                    id = "slot-1",
                    startHour = 18,
                    startMinute = 0,
                    endHour = 20,
                    endMinute = 0,
                    roomIds = listOf(salonId),
                ),
            ),
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
                LightTimeSlot(
                    id = "slot-1",
                    startHour = 18,
                    startMinute = 0,
                    endHour = 20,
                    endMinute = 0,
                    roomIds = listOf(salonId),
                ),
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
}
