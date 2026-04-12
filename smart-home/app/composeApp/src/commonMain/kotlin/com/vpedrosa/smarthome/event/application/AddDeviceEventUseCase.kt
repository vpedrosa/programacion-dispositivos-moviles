package com.vpedrosa.smarthome.event.application

import com.vpedrosa.smarthome.device.domain.model.DeviceEvent
import com.vpedrosa.smarthome.device.domain.DeviceEventRepository
import com.vpedrosa.smarthome.event.domain.NotificationPort
import com.vpedrosa.smarthome.settings.domain.AppSettingsRepository
import kotlinx.coroutines.flow.first

class AddDeviceEventUseCase(
    private val deviceEventRepository: DeviceEventRepository,
    private val notificationPort: NotificationPort,
    private val appSettingsRepository: AppSettingsRepository,
) {

    suspend operator fun invoke(event: DeviceEvent) {
        deviceEventRepository.add(event)
        val settings = appSettingsRepository.observeSettings().first()
        if (settings.notificationsEnabled) {
            notificationPort.showSensorAlert(event)
        }
    }
}
