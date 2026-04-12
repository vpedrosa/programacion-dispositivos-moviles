package com.vpedrosa.smarthome.antisquatter.application

import com.vpedrosa.smarthome.device.domain.model.Blind
import com.vpedrosa.smarthome.device.domain.model.DeviceType
import com.vpedrosa.smarthome.device.domain.model.Light
import com.vpedrosa.smarthome.antisquatter.domain.AntiSquatterRepository
import com.vpedrosa.smarthome.device.application.BulkToggleDevicesByTypeUseCase
import com.vpedrosa.smarthome.device.domain.DeviceRepository
import com.vpedrosa.smarthome.event.application.AddDeviceEventUseCase
import com.vpedrosa.smarthome.device.domain.model.DeviceEvent
import com.vpedrosa.smarthome.device.domain.model.DeviceEventType
import kotlinx.coroutines.flow.first
import kotlin.random.Random
import kotlin.time.Clock

/**
 * Simulates presence by toggling lights and blinds at each action-duration
 * boundary within the configured time window.
 *
 * Example: start=21:00, end=23:00, duration=30min → toggles at 21:00, 21:30, 22:00, 22:30.
 */
class SimulatePresenceUseCase(
    private val antiSquatterRepository: AntiSquatterRepository,
    private val deviceRepository: DeviceRepository,
    private val bulkToggle: BulkToggleDevicesByTypeUseCase,
    private val addDeviceEvent: AddDeviceEventUseCase,
) {
    /**
     * Called periodically (e.g. every minute). Checks if the current time
     * falls within one of the scheduled action slots and executes if so.
     */
    suspend operator fun invoke(hour: Int, minute: Int) {
        val config = antiSquatterRepository.observeConfig().first()
        if (!config.isEnabled || !config.isValid) return

        val currentMinutes = hour * 60 + minute
        val startMinutes = config.startHour * 60 + config.startMinute
        val endMinutes = config.endHour * 60 + config.endMinute

        if (currentMinutes !in startMinutes until endMinutes) return
        if (!isActionSlot(config, currentMinutes)) return

        // Determine target state from current majority state
        val allDevices = deviceRepository.observeAllDevices().first()
        val lights = allDevices.filterIsInstance<Light>()
        val blinds = allDevices.filterIsInstance<Blind>()

        val turnOnLights = lights.count { it.isOn } <= lights.size / 2
        val openBlinds = blinds.count { it.openingLevel > 0 } <= blinds.size / 2

        bulkToggle(DeviceType.LIGHT, turnOnLights)
        bulkToggle(DeviceType.BLIND, openBlinds)

        // Log events
        for (light in lights) {
            val action = if (turnOnLights) "encendida" else "apagada"
            addDeviceEvent(
                DeviceEvent(
                    id = "asq-${Clock.System.now().toEpochMilliseconds()}-${Random.nextInt(100_000)}",
                    deviceId = light.id,
                    type = if (turnOnLights) DeviceEventType.DEVICE_TURNED_ON else DeviceEventType.DEVICE_TURNED_OFF,
                    message = "Antiokupas: ${light.name} $action",
                    timestamp = Clock.System.now(),
                ),
            )
        }
        for (blind in blinds) {
            val action = if (openBlinds) "abierta" else "cerrada"
            addDeviceEvent(
                DeviceEvent(
                    id = "asq-${Clock.System.now().toEpochMilliseconds()}-${Random.nextInt(100_000)}",
                    deviceId = blind.id,
                    type = if (openBlinds) DeviceEventType.DEVICE_TURNED_ON else DeviceEventType.DEVICE_TURNED_OFF,
                    message = "Antiokupas: ${blind.name} $action",
                    timestamp = Clock.System.now(),
                ),
            )
        }
    }

    private fun isActionSlot(
        config: com.vpedrosa.smarthome.antisquatter.domain.model.AntiSquatterConfig,
        currentMinutes: Int,
    ): Boolean {
        val startMinutes = config.startHour * 60 + config.startMinute
        val offset = currentMinutes - startMinutes
        return offset >= 0 && offset % config.actionDurationMinutes == 0
    }
}
