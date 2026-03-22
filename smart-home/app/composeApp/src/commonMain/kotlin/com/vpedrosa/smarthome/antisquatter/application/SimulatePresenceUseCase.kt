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
 * Also controls Smart TVs using the same light time slots.
 */
class SimulatePresenceUseCase(
    private val antiSquatterRepository: AntiSquatterRepository,
    private val deviceRepository: DeviceRepository,
) {
    suspend operator fun invoke(hour: Int, minute: Int) {
        val config = antiSquatterRepository.observeConfig().first()
        if (!config.isEnabled) return

        val allDevices = deviceRepository.observeAllDevices().first()
        val anySlotActive = config.timeSlots.any { it.containsTime(hour, minute) }

        simulateLights(config.timeSlots, allDevices.filterIsInstance<Light>(), hour, minute)
        simulateSmartTvs(config.videoConfig, allDevices.filterIsInstance<SmartTv>(), anySlotActive)
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

    /**
     * Smart TVs are activated during the same time slots as the lights.
     * The video config only controls whether TV simulation is enabled and the video URL.
     */
    private suspend fun simulateSmartTvs(
        videoConfig: VideoConfig,
        smartTvs: List<SmartTv>,
        anySlotActive: Boolean,
    ) {
        if (!videoConfig.isEnabled) return

        val simulatedUrl = videoConfig.videoUrl.ifBlank { null }
        smartTvs.forEach { tv ->
            val targetUrl = if (anySlotActive) simulatedUrl else null
            val needsUpdate = tv.isOn != anySlotActive || tv.contentUrl != targetUrl
            if (needsUpdate) {
                deviceRepository.save(tv.copy(isOn = anySlotActive, contentUrl = targetUrl))
            }
        }
    }
}
