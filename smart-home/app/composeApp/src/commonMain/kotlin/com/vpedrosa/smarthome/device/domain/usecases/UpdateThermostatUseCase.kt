package com.vpedrosa.smarthome.device.domain.usecases

import com.vpedrosa.smarthome.device.domain.DeviceId
import com.vpedrosa.smarthome.device.domain.Thermostat
import com.vpedrosa.smarthome.device.domain.ports.DeviceRepository
import kotlinx.coroutines.flow.first

class UpdateThermostatUseCase(
    private val deviceRepository: DeviceRepository,
) {
    suspend operator fun invoke(
        id: DeviceId,
        targetTemperature: Double? = null,
        isHeatingOn: Boolean? = null,
    ) {
        val device = deviceRepository.observeDevice(id).first() ?: return
        if (device !is Thermostat) return

        var updated = device
        if (targetTemperature != null) updated = updated.adjustTarget(targetTemperature)
        if (isHeatingOn != null) updated = updated.copy(isHeatingOn = isHeatingOn)

        deviceRepository.save(updated)
    }
}
