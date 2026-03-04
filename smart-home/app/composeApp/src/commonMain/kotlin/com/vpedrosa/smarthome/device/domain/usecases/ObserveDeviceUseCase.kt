package com.vpedrosa.smarthome.device.domain.usecases

import com.vpedrosa.smarthome.device.domain.Device
import com.vpedrosa.smarthome.device.domain.DeviceId
import com.vpedrosa.smarthome.device.domain.ports.DeviceRepository
import kotlinx.coroutines.flow.Flow

class ObserveDeviceUseCase(
    private val deviceRepository: DeviceRepository,
) {
    operator fun invoke(id: DeviceId): Flow<Device?> =
        deviceRepository.observeDevice(id)
}
