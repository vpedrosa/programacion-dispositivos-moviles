package com.vpedrosa.smarthome.device.application

import com.vpedrosa.smarthome.shared.domain.model.DeviceType
import com.vpedrosa.smarthome.shared.domain.DeviceControlPort
import com.vpedrosa.smarthome.shared.domain.DeviceRepository
import com.vpedrosa.smarthome.shared.domain.toggleDevice
import kotlinx.coroutines.flow.first

class BulkToggleDevicesByTypeUseCase(
    private val deviceRepository: DeviceRepository,
    private val deviceControlPort: DeviceControlPort,
) {
    suspend operator fun invoke(type: DeviceType, turnOn: Boolean): Int {
        val devices = deviceRepository.observeDevicesByType(type).first()

        val updated = devices.mapNotNull { device ->
            toggleDevice(device, turnOn, deviceControlPort)
        }

        if (updated.isNotEmpty()) {
            deviceRepository.saveAll(updated)
        }
        return updated.size
    }
}
