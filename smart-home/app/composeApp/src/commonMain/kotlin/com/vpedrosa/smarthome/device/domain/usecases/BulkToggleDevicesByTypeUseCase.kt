package com.vpedrosa.smarthome.device.domain.usecases

import com.vpedrosa.smarthome.device.domain.DeviceType
import com.vpedrosa.smarthome.device.domain.Light
import com.vpedrosa.smarthome.device.domain.Lock
import com.vpedrosa.smarthome.device.domain.SmartTv
import com.vpedrosa.smarthome.device.domain.Switch
import com.vpedrosa.smarthome.device.domain.Thermostat
import com.vpedrosa.smarthome.device.domain.ports.DeviceRepository
import kotlinx.coroutines.flow.first

class BulkToggleDevicesByTypeUseCase(
    private val deviceRepository: DeviceRepository,
) {
    /**
     * Sets all devices of the given [type] to the specified [turnOn] state.
     * For Lock, [turnOn] = true means locked, false means unlocked.
     * Sensor and Blind types are ignored.
     */
    suspend operator fun invoke(type: DeviceType, turnOn: Boolean) {
        val devices = deviceRepository.observeDevicesByType(type).first()

        val updated = devices.mapNotNull { device ->
            when (device) {
                is Light -> if (device.isOn != turnOn) device.copy(isOn = turnOn) else null
                is Switch -> if (device.isOn != turnOn) device.copy(isOn = turnOn) else null
                is SmartTv -> if (device.isOn != turnOn) device.copy(isOn = turnOn) else null
                is Lock -> if (device.isLocked != turnOn) device.copy(isLocked = turnOn) else null
                is Thermostat -> if (device.isHeatingOn != turnOn) device.copy(isHeatingOn = turnOn) else null
                else -> null
            }
        }

        if (updated.isNotEmpty()) {
            deviceRepository.saveAll(updated)
        }
    }
}
