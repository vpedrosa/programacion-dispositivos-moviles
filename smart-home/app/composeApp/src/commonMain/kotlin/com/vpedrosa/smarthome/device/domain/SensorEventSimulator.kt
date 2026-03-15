package com.vpedrosa.smarthome.device.domain

import com.vpedrosa.smarthome.device.domain.ports.DeviceRepository
import com.vpedrosa.smarthome.device.domain.usecases.AddDeviceEventUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ObserveAllDevicesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Clock

class SensorEventSimulator(
    private val addDeviceEvent: AddDeviceEventUseCase,
    private val observeAllDevices: ObserveAllDevicesUseCase,
    private val deviceRepository: DeviceRepository,
    private val scope: CoroutineScope,
) {

    private var started = false

    fun start() {
        if (started) return
        started = true

        scope.launch { emitTemperatureReadings() }
        scope.launch { emitContactSensorEvents() }
        scope.launch { emitSmokeAlerts() }
        scope.launch { emitWaterLeakAlerts() }
        scope.launch { emitThermostatAdjustments() }
    }

    private suspend fun emitTemperatureReadings() {
        while (true) {
            delay(60_000L)
            val sensors = findDevicesOfType<TemperatureSensor>()
            for (sensor in sensors) {
                val temp = 18.0 + Random.nextDouble() * 10.0
                val rounded = (temp * 10).toLong() / 10.0
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
            delay(90_000L)
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
            delay(180_000L)
            val sensors = findDevicesOfType<SmokeSensor>()
            for (sensor in sensors) {
                if (Random.nextFloat() < 0.15f) {
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
            delay(240_000L)
            val sensors = findDevicesOfType<WaterLeakSensor>()
            for (sensor in sensors) {
                if (Random.nextFloat() < 0.1f) {
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

    private suspend inline fun <reified T : Device> findDevicesOfType(): List<T> =
        observeAllDevices().first().filterIsInstance<T>()

    private fun randomId(): String =
        "evt-${Clock.System.now().toEpochMilliseconds()}-${Random.nextInt(100_000)}"
}
