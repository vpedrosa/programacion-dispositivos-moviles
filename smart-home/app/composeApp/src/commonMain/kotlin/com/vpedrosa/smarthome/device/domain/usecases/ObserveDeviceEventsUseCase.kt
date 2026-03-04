package com.vpedrosa.smarthome.device.domain.usecases

import com.vpedrosa.smarthome.device.domain.DeviceEvent
import com.vpedrosa.smarthome.device.domain.ports.DeviceEventRepository
import kotlinx.coroutines.flow.Flow

class ObserveDeviceEventsUseCase(
    private val deviceEventRepository: DeviceEventRepository,
) {
    operator fun invoke(): Flow<List<DeviceEvent>> =
        deviceEventRepository.observeAllEvents()
}
