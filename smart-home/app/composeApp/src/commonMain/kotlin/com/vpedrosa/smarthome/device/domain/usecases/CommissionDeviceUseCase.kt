package com.vpedrosa.smarthome.device.domain.usecases

import com.vpedrosa.smarthome.device.domain.Device
import com.vpedrosa.smarthome.device.domain.DiscoveredDevice
import com.vpedrosa.smarthome.device.domain.ports.CommissioningPort
import com.vpedrosa.smarthome.device.domain.ports.DeviceRepository

class CommissionDeviceUseCase(
    private val commissioningPort: CommissioningPort,
    private val deviceRepository: DeviceRepository,
) {
    suspend operator fun invoke(device: DiscoveredDevice): Result<Device> {
        return commissioningPort.commission(device).onSuccess { commissioned ->
            deviceRepository.save(commissioned)
        }
    }
}
