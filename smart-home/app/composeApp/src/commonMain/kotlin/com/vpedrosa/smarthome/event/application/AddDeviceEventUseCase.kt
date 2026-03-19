package com.vpedrosa.smarthome.event.application

import com.vpedrosa.smarthome.shared.domain.model.DeviceEvent
import com.vpedrosa.smarthome.shared.domain.model.DeviceEventType
import com.vpedrosa.smarthome.shared.domain.DeviceEventRepository
import com.vpedrosa.smarthome.event.domain.NotificationPort

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
