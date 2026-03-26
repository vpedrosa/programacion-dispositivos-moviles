package com.vpedrosa.smarthome.event.infrastructure.simulation

import com.vpedrosa.smarthome.shared.domain.model.ContactSensor
import com.vpedrosa.smarthome.shared.domain.model.Device
import com.vpedrosa.smarthome.shared.domain.model.DeviceEvent
import com.vpedrosa.smarthome.shared.domain.model.DeviceEventType
import com.vpedrosa.smarthome.shared.domain.model.DeviceId
import com.vpedrosa.smarthome.shared.domain.model.SmokeSensor
import com.vpedrosa.smarthome.shared.domain.model.TemperatureSensor
import com.vpedrosa.smarthome.shared.domain.model.Thermostat
import com.vpedrosa.smarthome.shared.domain.model.WaterLeakSensor
import com.vpedrosa.smarthome.shared.domain.DeviceRepository
import com.vpedrosa.smarthome.event.application.AddDeviceEventUseCase
import com.vpedrosa.smarthome.event.domain.BackgroundSimulatorPort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.time.Duration.Companion.minutes

class SensorEventSimulator(
    private val addDeviceEvent: AddDeviceEventUseCase,
    private val deviceRepository: DeviceRepository,
    private val scope: CoroutineScope,
) : BackgroundSimulatorPort {

    private var started = false

    /** Tracks when each ContactSensor was first detected as open. */
    private val doorOpenSince = mutableMapOf<DeviceId, Instant>()

    /** Tracks last notified temperature per sensor to filter small variations. */
    private val lastNotifiedTemp = mutableMapOf<DeviceId, Double>()

    override fun start() {
        if (started) return
        started = true

        scope.launch { emitTemperatureReadings() }
        scope.launch { emitContactSensorEvents() }
        scope.launch { emitSmokeAlerts() }
        scope.launch { emitWaterLeakAlerts() }
        scope.launch { emitThermostatAdjustments() }
        scope.launch { monitorDoorOpenDuration() }
    }

    private suspend fun emitTemperatureReadings() {
        while (true) {
            delay(3_000L)
            val sensors = findDevicesOfType<TemperatureSensor>()
            for (sensor in sensors) {
                val temp = 18.0 + Random.nextDouble() * 10.0
                val rounded = (temp * 10).toLong() / 10.0
                val previous = lastNotifiedTemp[sensor.id]
                if (previous != null && kotlin.math.abs(rounded - previous) <= 1.0) continue
                lastNotifiedTemp[sensor.id] = rounded
                addDeviceEvent(
                    DeviceEvent(
                        id = randomId(),
                        deviceId = sensor.id,
                        type = DeviceEventType.TEMPERATURE_READING,
                        message = "Temperatura: ${rounded}\u00B0C",
                        timestamp = Clock.System.now(),
                    ),
                )
            }
        }
    }

    private suspend fun emitContactSensorEvents() {
        while (true) {
            delay(10_000L)
            val sensors = findDevicesOfType<ContactSensor>()
            for (sensor in sensors) {
                val toggled = sensor.toggle()
                deviceRepository.save(toggled)
                addDeviceEvent(
                    DeviceEvent(
                        id = randomId(),
                        deviceId = sensor.id,
                        type = if (toggled.isOpen) DeviceEventType.DOOR_OPENED else DeviceEventType.DOOR_CLOSED,
                        message = if (toggled.isOpen) "${sensor.name} abierta" else "${sensor.name} cerrada",
                        timestamp = Clock.System.now(),
                    ),
                )
            }
        }
    }

    private suspend fun emitSmokeAlerts() {
        while (true) {
            delay(5_000L)
            val sensors = findDevicesOfType<SmokeSensor>()
            for (sensor in sensors) {
                if (Random.nextFloat() < 0.5f) {
                    addDeviceEvent(
                        DeviceEvent(
                            id = randomId(),
                            deviceId = sensor.id,
                            type = DeviceEventType.SMOKE_ALERT,
                            message = "Alerta de humo detectada",
                            timestamp = Clock.System.now(),
                        ),
                    )
                }
            }
        }
    }

    private suspend fun emitWaterLeakAlerts() {
        while (true) {
            delay(5_000L)
            val sensors = findDevicesOfType<WaterLeakSensor>()
            for (sensor in sensors) {
                if (Random.nextFloat() < 0.5f) {
                    addDeviceEvent(
                        DeviceEvent(
                            id = randomId(),
                            deviceId = sensor.id,
                            type = DeviceEventType.WATER_LEAK_ALERT,
                            message = "Posible fuga de agua detectada",
                            timestamp = Clock.System.now(),
                        ),
                    )
                }
            }
        }
    }

    private suspend fun emitThermostatAdjustments() {
        while (true) {
            delay(120_000L)
            val thermostats = findDevicesOfType<Thermostat>()
            for (thermostat in thermostats) {
                if (Random.nextFloat() < 0.3f) {
                    val target = 19 + Random.nextInt(6)
                    addDeviceEvent(
                        DeviceEvent(
                            id = randomId(),
                            deviceId = thermostat.id,
                            type = DeviceEventType.THERMOSTAT_ADJUSTED,
                            message = "Termostato ajustado a ${target}\u00B0C",
                            timestamp = Clock.System.now(),
                        ),
                    )
                }
            }
        }
    }

    /**
     * Monitors all ContactSensor devices every 30 seconds. If a door has been
     * continuously open for more than 2 minutes, emits a DOOR_OPEN_TOO_LONG
     * event. The alert repeats every 2 minutes while the door remains open.
     */
    private suspend fun monitorDoorOpenDuration() {
        val alertThreshold = 2.minutes

        while (true) {
            delay(30_000L)

            val now = Clock.System.now()
            val sensors = findDevicesOfType<ContactSensor>()

            for (sensor in sensors) {
                if (sensor.isOpen) {
                    val openedAt = doorOpenSince.getOrPut(sensor.id) { now }
                    val elapsed = now - openedAt

                    if (elapsed >= alertThreshold) {
                        val minutes = elapsed.inWholeMinutes
                        addDeviceEvent(
                            DeviceEvent(
                                id = randomId(),
                                deviceId = sensor.id,
                                type = DeviceEventType.DOOR_OPEN_TOO_LONG,
                                message = "${sensor.name} lleva abierta ${minutes} min",
                                timestamp = now,
                            ),
                        )
                        // Reset the timer so the next alert fires 2 minutes later
                        doorOpenSince[sensor.id] = now
                    }
                } else {
                    doorOpenSince.remove(sensor.id)
                }
            }
        }
    }

    private suspend inline fun <reified T : Device> findDevicesOfType(): List<T> =
        deviceRepository.observeAllDevices().first().filterIsInstance<T>()

    private fun randomId(): String =
        "evt-${Clock.System.now().toEpochMilliseconds()}-${Random.nextInt(100_000)}"
}
