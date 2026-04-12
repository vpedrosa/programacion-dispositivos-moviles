package com.vpedrosa.smarthome.device.application

import com.vpedrosa.smarthome.device.domain.DeviceControlPort
import com.vpedrosa.smarthome.device.domain.DeviceRepository
import com.vpedrosa.smarthome.device.domain.model.DeviceId

class DeregisterDeviceUseCase(
    private val deviceRepository: DeviceRepository,
    private val deviceControlPort: DeviceControlPort,
) {
    suspend operator fun invoke(id: DeviceId) {
        deviceControlPort.deregisterDevice(id)
        deviceRepository.delete(id)
    }
}
