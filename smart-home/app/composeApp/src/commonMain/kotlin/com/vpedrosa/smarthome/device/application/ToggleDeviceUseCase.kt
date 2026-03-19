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
        val toggled = when (device) {
            is Light -> {
                deviceControlPort.toggleOnOff(id, !device.isOn)
                device.toggle()
            }
            is Lock -> {
                deviceControlPort.lockDoor(id, !device.isLocked)
                device.toggle()
            }
            is Switch -> {
                deviceControlPort.toggleOnOff(id, !device.isOn)
                device.toggle()
            }
            is SmartTv -> {
                deviceControlPort.toggleOnOff(id, !device.isOn)
                device.toggle()
            }
            is Thermostat -> {
                deviceControlPort.setThermostatMode(id, !device.isHeatingOn)
                device.toggleHeating()
            }
            is Blind,
            is SmokeSensor,
            is WaterLeakSensor,
            is TemperatureSensor,
            is ContactSensor -> return
        }
        deviceRepository.save(toggled)
    }
}
