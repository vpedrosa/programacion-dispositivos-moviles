package com.vpedrosa.smarthome.device.application

import com.vpedrosa.smarthome.device.domain.model.DeviceId
import com.vpedrosa.smarthome.device.domain.model.Thermostat
import com.vpedrosa.smarthome.device.domain.DeviceControlPort
import com.vpedrosa.smarthome.device.domain.DeviceRepository
import kotlinx.coroutines.flow.first

class UpdateThermostatUseCase(
    private val deviceRepository: DeviceRepository,
    private val deviceControlPort: DeviceControlPort,
) {
    suspend operator fun invoke(
        id: DeviceId,
        targetTemperature: Double? = null,
        isHeatingOn: Boolean? = null,
    ) {
        val device = deviceRepository.observeDevice(id).first() ?: return
        if (device !is Thermostat) return

        var updated = device
        if (targetTemperature != null) {
            deviceControlPort.setThermostatSetpoint(id, targetTemperature)
            updated = updated.adjustTarget(targetTemperature)
            if (!device.isHeatingOn && isHeatingOn != false) {
                deviceControlPort.setThermostatMode(id, true)
                updated = updated.copy(isHeatingOn = true)
            }
        }
        if (isHeatingOn != null) {
            deviceControlPort.setThermostatMode(id, isHeatingOn)
            updated = updated.copy(isHeatingOn = isHeatingOn)
        }

        deviceRepository.save(updated)
    }
}
