package com.vpedrosa.smarthome.device.domain.usecases

import com.vpedrosa.smarthome.device.domain.Light
import com.vpedrosa.smarthome.device.domain.RoomId
import com.vpedrosa.smarthome.device.domain.ports.AntiSquatterRepository
import com.vpedrosa.smarthome.device.domain.ports.DeviceRepository
import kotlinx.coroutines.flow.first

/**
 * Checks the current anti-squatter configuration against the given time
 * and turns lights on/off in the corresponding rooms.
 */
class SimulatePresenceUseCase(
    private val antiSquatterRepository: AntiSquatterRepository,
    private val deviceRepository: DeviceRepository,
) {
    /**
     * Evaluates time slots for the given [hour] and [minute].
     * Lights in rooms that match an active time slot are turned ON;
     * lights in rooms that were in any configured slot but do NOT match the current time are turned OFF.
     */
    suspend operator fun invoke(hour: Int, minute: Int) {
        val config = antiSquatterRepository.observeConfig().first()
        if (!config.isEnabled) return

        val activeRoomIds = config.timeSlots
            .filter { it.containsTime(hour, minute) }
            .flatMap { it.roomIds }
            .toSet()

        val allConfiguredRoomIds = config.timeSlots
            .flatMap { it.roomIds }
            .toSet()

        val allDevices = deviceRepository.observeAllDevices().first()
        val lightsInConfiguredRooms = allDevices.filterIsInstance<Light>()
            .filter { light -> allConfiguredRoomIds.any { it.value == light.roomId } }

        lightsInConfiguredRooms.forEach { light ->
            val shouldBeOn = activeRoomIds.any { it.value == light.roomId }
            if (light.isOn != shouldBeOn) {
                deviceRepository.save(light.copy(isOn = shouldBeOn))
            }
        }
    }
}
