package com.vpedrosa.smarthome.antisquatter.application

import com.vpedrosa.smarthome.shared.domain.model.Light
import com.vpedrosa.smarthome.antisquatter.domain.model.LightTimeSlot
import com.vpedrosa.smarthome.shared.domain.model.SmartTv
import com.vpedrosa.smarthome.antisquatter.domain.model.VideoConfig
import com.vpedrosa.smarthome.antisquatter.domain.AntiSquatterRepository
import com.vpedrosa.smarthome.shared.domain.DeviceRepository
import kotlinx.coroutines.flow.first

/**
 * Checks the current anti-squatter configuration against the given time
 * and turns lights on/off in the corresponding rooms.
 * Also controls Smart TVs based on the video configuration.
 */
class SimulatePresenceUseCase(
    private val antiSquatterRepository: AntiSquatterRepository,
    private val deviceRepository: DeviceRepository,
) {
    /**
     * Evaluates time slots for the given [hour] and [minute].
     * Lights in rooms that match an active time slot are turned ON;
     * lights in rooms that were in any configured slot but do NOT match the current time are turned OFF.
     * If the video config is enabled and the current time falls within its range,
     * all Smart TVs are turned on and set to casting; otherwise they are turned off.
     */
    suspend operator fun invoke(hour: Int, minute: Int) {
        val config = antiSquatterRepository.observeConfig().first()
        if (!config.isEnabled) return

        val allDevices = deviceRepository.observeAllDevices().first()

        simulateLights(config.timeSlots, allDevices.filterIsInstance<Light>(), hour, minute)
        simulateSmartTvs(config.videoConfig, allDevices.filterIsInstance<SmartTv>(), hour, minute)
    }

    private suspend fun simulateLights(
        timeSlots: List<LightTimeSlot>,
        lights: List<Light>,
        hour: Int,
        minute: Int,
    ) {
        val activeRoomIds = timeSlots
            .filter { it.containsTime(hour, minute) }
            .flatMap { it.roomIds }
            .toSet()

        val allConfiguredRoomIds = timeSlots
            .flatMap { it.roomIds }
            .toSet()

        val lightsInConfiguredRooms = lights
            .filter { light -> light.roomId in allConfiguredRoomIds }

        lightsInConfiguredRooms.forEach { light ->
            val shouldBeOn = light.roomId in activeRoomIds
            if (light.isOn != shouldBeOn) {
                deviceRepository.save(light.copy(isOn = shouldBeOn))
            }
        }
    }

    private suspend fun simulateSmartTvs(
        videoConfig: VideoConfig,
        smartTvs: List<SmartTv>,
        hour: Int,
        minute: Int,
    ) {
        if (!videoConfig.isEnabled) return

        val shouldBeActive = videoConfig.containsTime(hour, minute)

        smartTvs.forEach { tv ->
            val needsUpdate = tv.isOn != shouldBeActive || tv.isCasting != shouldBeActive
            if (needsUpdate) {
                deviceRepository.save(tv.copy(isOn = shouldBeActive, isCasting = shouldBeActive))
            }
        }
    }
}
