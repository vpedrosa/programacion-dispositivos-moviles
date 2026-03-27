package com.vpedrosa.smarthome.device.application

import com.vpedrosa.smarthome.shared.domain.model.Blind
import com.vpedrosa.smarthome.shared.domain.model.ContactSensor
import com.vpedrosa.smarthome.shared.domain.model.DeviceId
import com.vpedrosa.smarthome.shared.domain.model.Light
import com.vpedrosa.smarthome.shared.domain.model.Lock
import com.vpedrosa.smarthome.shared.domain.model.SmartTv
import com.vpedrosa.smarthome.shared.domain.model.SmokeSensor
import com.vpedrosa.smarthome.shared.domain.model.Switch
import com.vpedrosa.smarthome.shared.domain.model.TemperatureSensor
import com.vpedrosa.smarthome.shared.domain.model.Thermostat
import com.vpedrosa.smarthome.shared.domain.model.WaterLeakSensor
import com.vpedrosa.smarthome.shared.domain.DeviceControlPort
import com.vpedrosa.smarthome.shared.domain.DeviceEventRepository
import com.vpedrosa.smarthome.shared.domain.DeviceRepository
import com.vpedrosa.smarthome.shared.domain.model.DeviceEvent
import com.vpedrosa.smarthome.shared.domain.model.DeviceEventType
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.first

class ToggleDeviceUseCase(
    private val deviceRepository: DeviceRepository,
    private val deviceControlPort: DeviceControlPort,
    private val deviceEventRepository: DeviceEventRepository,
) {
    suspend operator fun invoke(id: DeviceId) {
        val device = deviceRepository.observeDevice(id).first() ?: return

        // Send Matter command first — this is the source of truth.
        try {
            when (device) {
                is Light -> deviceControlPort.toggleOnOff(id, !device.isOn)
                is Lock -> deviceControlPort.lockDoor(id, !device.isLocked)
                is Switch -> deviceControlPort.toggleOnOff(id, !device.isOn)
                is SmartTv -> deviceControlPort.toggleOnOff(id, !device.isOn)
                is Thermostat -> deviceControlPort.setThermostatMode(id, !device.isHeatingOn)
                is Blind, is ContactSensor, is SmokeSensor, is WaterLeakSensor, is TemperatureSensor -> return
            }
        } catch (_: Exception) {
            // Matter command failed — do NOT update in-memory state
            // so the UI reflects the real device state.
            return
        }

        // Matter succeeded — update in-memory state to match.
        val toggled = when (device) {
            is Light -> device.toggle()
            is Lock -> device.toggle()
            is Switch -> device.toggle()
            is SmartTv -> device.toggle()
            is Thermostat -> device.toggleHeating()
            is Blind, is ContactSensor, is SmokeSensor, is WaterLeakSensor, is TemperatureSensor -> return
        }
        deviceRepository.save(toggled)

        if (device is Lock) {
            val locked = !device.isLocked
            @OptIn(ExperimentalUuidApi::class)
            deviceEventRepository.add(
                DeviceEvent(
                    id = Uuid.random().toString(),
                    deviceId = id,
                    type = if (locked) DeviceEventType.DOOR_CLOSED else DeviceEventType.DOOR_OPENED,
                    message = if (locked) "${device.name} bloqueada" else "${device.name} desbloqueada",
                    timestamp = Clock.System.now(),
                ),
            )
        }
    }
}
