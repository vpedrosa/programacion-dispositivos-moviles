package com.vpedrosa.smarthome.device.domain.usecases

import com.vpedrosa.smarthome.device.domain.Device
import com.vpedrosa.smarthome.device.domain.ports.DeviceRepository
import kotlinx.coroutines.flow.Flow

class ObserveAllDevicesUseCase(
    private val deviceRepository: DeviceRepository,
) {
    operator fun invoke(): Flow<List<Device>> =
        deviceRepository.observeAllDevices()
}
