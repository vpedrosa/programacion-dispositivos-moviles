package com.vpedrosa.smarthome.device.domain.usecases

import com.vpedrosa.smarthome.device.domain.Blind
import com.vpedrosa.smarthome.device.domain.ContactSensor
import com.vpedrosa.smarthome.device.domain.DeviceId
import com.vpedrosa.smarthome.device.domain.Light
import com.vpedrosa.smarthome.device.domain.Lock
import com.vpedrosa.smarthome.device.domain.SmartTv
import com.vpedrosa.smarthome.device.domain.SmokeSensor
import com.vpedrosa.smarthome.device.domain.Switch
import com.vpedrosa.smarthome.device.domain.TemperatureSensor
import com.vpedrosa.smarthome.device.domain.Thermostat
import com.vpedrosa.smarthome.device.domain.WaterLeakSensor
import com.vpedrosa.smarthome.device.domain.ports.DeviceControlPort
import com.vpedrosa.smarthome.device.domain.ports.DeviceRepository
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
