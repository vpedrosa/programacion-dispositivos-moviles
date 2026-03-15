package com.vpedrosa.smarthome.device.domain.usecases

import com.vpedrosa.smarthome.device.domain.DeviceEvent
import com.vpedrosa.smarthome.device.domain.DeviceEventType
import com.vpedrosa.smarthome.device.domain.ports.DeviceEventRepository
import com.vpedrosa.smarthome.device.domain.ports.NotificationPort

class AddDeviceEventUseCase(
    private val deviceEventRepository: DeviceEventRepository,
    private val notificationPort: NotificationPort,
) {

    private val criticalEventTypes = setOf(
        DeviceEventType.SMOKE_ALERT,
        DeviceEventType.WATER_LEAK_ALERT,
        DeviceEventType.DOOR_OPEN_TOO_LONG,
    )

    suspend operator fun invoke(event: DeviceEvent) {
        deviceEventRepository.add(event)

        if (event.type in criticalEventTypes) {
            notificationPort.showSensorAlert(event)
        }
    }
}
