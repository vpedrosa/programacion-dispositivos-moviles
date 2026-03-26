package com.vpedrosa.smarthome.device.application

import com.vpedrosa.smarthome.shared.domain.DeviceControlPort
import com.vpedrosa.smarthome.shared.domain.DeviceRepository
import com.vpedrosa.smarthome.shared.domain.model.DeviceId

class DeregisterDeviceUseCase(
    private val deviceRepository: DeviceRepository,
    private val deviceControlPort: DeviceControlPort,
) {
    suspend operator fun invoke(id: DeviceId) {
        deviceControlPort.deregisterDevice(id)
        deviceRepository.delete(id)
    }
}
