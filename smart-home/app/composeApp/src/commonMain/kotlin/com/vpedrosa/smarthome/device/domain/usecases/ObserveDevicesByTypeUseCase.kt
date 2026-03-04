package com.vpedrosa.smarthome.device.domain.usecases

import com.vpedrosa.smarthome.device.domain.Device
import com.vpedrosa.smarthome.device.domain.DeviceType
import com.vpedrosa.smarthome.device.domain.ports.DeviceRepository
import kotlinx.coroutines.flow.Flow

class ObserveDevicesByTypeUseCase(
    private val deviceRepository: DeviceRepository,
) {
    operator fun invoke(type: DeviceType): Flow<List<Device>> =
        deviceRepository.observeDevicesByType(type)
}
