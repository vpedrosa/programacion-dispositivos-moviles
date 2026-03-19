package com.vpedrosa.smarthome.commissioning.application

import com.vpedrosa.smarthome.shared.domain.model.Device
import com.vpedrosa.smarthome.commissioning.domain.model.DiscoveredDevice
import com.vpedrosa.smarthome.commissioning.domain.CommissioningPort
import com.vpedrosa.smarthome.shared.domain.DeviceControlPort
import com.vpedrosa.smarthome.shared.domain.DeviceRepository

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
