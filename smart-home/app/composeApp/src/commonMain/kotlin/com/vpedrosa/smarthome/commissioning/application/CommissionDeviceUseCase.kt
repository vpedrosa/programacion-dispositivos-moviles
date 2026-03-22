package com.vpedrosa.smarthome.commissioning.application

import com.vpedrosa.smarthome.shared.domain.model.Device
import com.vpedrosa.smarthome.shared.domain.model.DeviceConnectionInfo
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
            val connectionInfo = DeviceConnectionInfo(
                host = device.host,
                port = device.port,
                passcode = device.passcode,
            )
            deviceControlPort.registerDevice(commissioned.id, connectionInfo)
            deviceRepository.save(commissioned)
        }
    }
}
