package com.vpedrosa.smarthome.antisquatter.application

import com.vpedrosa.smarthome.shared.domain.model.Blind
import com.vpedrosa.smarthome.shared.domain.model.DeviceType
import com.vpedrosa.smarthome.shared.domain.model.Light
import com.vpedrosa.smarthome.antisquatter.domain.AntiSquatterRepository
import com.vpedrosa.smarthome.device.application.BulkToggleDevicesByTypeUseCase
import com.vpedrosa.smarthome.shared.domain.DeviceRepository
import com.vpedrosa.smarthome.event.application.AddDeviceEventUseCase
import com.vpedrosa.smarthome.shared.domain.model.DeviceEvent
import com.vpedrosa.smarthome.shared.domain.model.DeviceEventType
import kotlinx.coroutines.flow.first
import kotlin.random.Random
import kotlin.time.Clock

/**
 * Simulates presence by toggling lights and blinds randomly within
 * the configured time window.
 *
 * Formula: n_actions = maxActions - X, where X ∈ [0, maxActions - 1],
 * ensuring at least 1 action is always executed.
 *
 * Actions are distributed at random timestamps within the interval.
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

        val actionSlots = computeActionSlots(config)
        if (currentMinutes !in actionSlots) return

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

    /**
     * Computes which minute-offsets within the interval have scheduled actions.
     *
     * n_actions = maxActions - X, X ∈ [0, maxActions - 1] → at least 1
     * The actions are distributed randomly within the interval.
     */
    private fun computeActionSlots(
        config: com.vpedrosa.smarthome.antisquatter.domain.model.AntiSquatterConfig,
    ): Set<Int> {
        val maxActions = config.maxActions
        if (maxActions <= 0) return emptySet()

        val x = Random.nextInt(0, maxActions) // 0..maxActions-1
        val nActions = maxActions - x // 1..maxActions

        val startMinutes = config.startHour * 60 + config.startMinute
        val possibleSlots = (0 until maxActions).map { i ->
            startMinutes + i * config.actionDurationMinutes
        }.shuffled()

        return possibleSlots.take(nActions).toSet()
    }
}
