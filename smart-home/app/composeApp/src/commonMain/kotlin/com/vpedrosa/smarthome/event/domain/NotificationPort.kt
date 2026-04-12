package com.vpedrosa.smarthome.event.domain

import com.vpedrosa.smarthome.device.domain.model.DeviceEvent

/**
 * Driven port for showing system-level notifications when critical sensor
 * events occur. The core domain defines what it needs; platform adapters
 * provide the concrete implementation (e.g. Android NotificationManager).
 */
interface NotificationPort {
    fun showSensorAlert(event: DeviceEvent)
}
