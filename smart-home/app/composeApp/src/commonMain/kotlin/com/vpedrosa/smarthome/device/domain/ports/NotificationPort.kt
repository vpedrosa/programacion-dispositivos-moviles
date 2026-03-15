package com.vpedrosa.smarthome.device.domain.ports

import com.vpedrosa.smarthome.device.domain.DeviceEvent

/**
 * Driven port for showing system-level notifications when critical sensor
 * events occur. The core domain defines what it needs; platform adapters
 * provide the concrete implementation (e.g. Android NotificationManager).
 */
interface NotificationPort {
    fun showSensorAlert(event: DeviceEvent)
}
