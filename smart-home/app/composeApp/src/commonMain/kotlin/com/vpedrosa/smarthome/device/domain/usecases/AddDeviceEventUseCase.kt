package com.vpedrosa.smarthome.device.domain.usecases

import com.vpedrosa.smarthome.device.domain.DeviceEvent
import com.vpedrosa.smarthome.device.domain.ports.DeviceEventRepository

class AddDeviceEventUseCase(
    private val deviceEventRepository: DeviceEventRepository,
) {
    suspend operator fun invoke(event: DeviceEvent) {
        deviceEventRepository.add(event)
    }
}
