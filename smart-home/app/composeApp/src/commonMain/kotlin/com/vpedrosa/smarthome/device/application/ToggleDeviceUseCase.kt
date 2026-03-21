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
import com.vpedrosa.smarthome.shared.domain.DeviceRepository
import kotlinx.coroutines.flow.first

class ToggleDeviceUseCase(
    private val deviceRepository: DeviceRepository,
    private val deviceControlPort: DeviceControlPort,
) {
    suspend operator fun invoke(id: DeviceId) {
        val device = deviceRepository.observeDevice(id).first() ?: return

        // Optimistic update: toggle in-memory state first so the UI always reflects the change.
        // The Matter command is a best-effort side effect.
        val toggled = when (device) {
            is Light -> device.toggle()
            is Lock -> device.toggle()
            is Switch -> device.toggle()
            is SmartTv -> device.toggle()
            is Thermostat -> device.toggleHeating()
            is Blind,
            is SmokeSensor,
            is WaterLeakSensor,
            is TemperatureSensor,
            is ContactSensor -> return
        }
        deviceRepository.save(toggled)

        // Fire Matter command — errors are caught and logged, not propagated.
        try {
            when (device) {
                is Light -> deviceControlPort.toggleOnOff(id, !device.isOn)
                is Lock -> deviceControlPort.lockDoor(id, !device.isLocked)
                is Switch -> deviceControlPort.toggleOnOff(id, !device.isOn)
                is SmartTv -> deviceControlPort.toggleOnOff(id, !device.isOn)
                is Thermostat -> deviceControlPort.setThermostatMode(id, !device.isHeatingOn)
                else -> {}
            }
        } catch (_: Exception) {
            // Session expired or simulator unavailable — state already updated above.
        }
    }
}
