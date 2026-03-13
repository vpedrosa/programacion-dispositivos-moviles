package com.vpedrosa.smarthome.device.domain.usecases

import com.vpedrosa.smarthome.device.domain.Device
import com.vpedrosa.smarthome.device.domain.DiscoveredDevice
import com.vpedrosa.smarthome.device.domain.ports.CommissioningPort
import com.vpedrosa.smarthome.device.domain.ports.DeviceControlPort
import com.vpedrosa.smarthome.device.domain.ports.DeviceRepository

class CommissionDeviceUseCase(
    private val commissioningPort: CommissioningPort,
    private val deviceRepository: DeviceRepository,
    private val deviceControlPort: DeviceControlPort,
) {
    suspend operator fun invoke(device: DiscoveredDevice): Result<Device> {
        return commissioningPort.commission(device).onSuccess { commissioned ->
            deviceControlPort.registerDevice(commissioned.id, device)
            deviceRepository.save(commissioned)
        }
    }
}
