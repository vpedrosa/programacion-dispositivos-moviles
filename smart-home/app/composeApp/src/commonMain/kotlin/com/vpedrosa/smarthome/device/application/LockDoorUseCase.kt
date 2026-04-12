package com.vpedrosa.smarthome.device.application

import com.vpedrosa.smarthome.device.domain.model.DeviceId
import com.vpedrosa.smarthome.device.domain.model.Lock
import com.vpedrosa.smarthome.device.domain.DeviceControlPort
import com.vpedrosa.smarthome.device.domain.DeviceRepository
import kotlinx.coroutines.flow.first

class LockDoorUseCase(
    private val deviceRepository: DeviceRepository,
    private val deviceControlPort: DeviceControlPort,
) {
    suspend operator fun invoke(deviceId: DeviceId, lock: Boolean) {
        val device = deviceRepository.observeDevice(deviceId).first() ?: return
        if (device !is Lock) return
        deviceControlPort.lockDoor(deviceId, lock)
        deviceRepository.save(device.copy(isLocked = lock))
    }
}
