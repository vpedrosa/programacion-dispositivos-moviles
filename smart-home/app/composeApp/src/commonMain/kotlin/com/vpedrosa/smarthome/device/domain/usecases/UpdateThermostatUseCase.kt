package com.vpedrosa.smarthome.device.domain.usecases

import com.vpedrosa.smarthome.device.domain.DeviceId
import com.vpedrosa.smarthome.device.domain.Thermostat
import com.vpedrosa.smarthome.device.domain.ports.DeviceControlPort
import com.vpedrosa.smarthome.device.domain.ports.DeviceRepository
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
        }
        if (isHeatingOn != null) {
            deviceControlPort.toggleOnOff(id, isHeatingOn)
            updated = updated.copy(isHeatingOn = isHeatingOn)
        }

        deviceRepository.save(updated)
    }
}
