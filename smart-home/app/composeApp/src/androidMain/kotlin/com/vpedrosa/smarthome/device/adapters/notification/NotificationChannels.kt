package com.vpedrosa.smarthome.device.adapters.notification

/**
 * Singleton holding notification channel constants for the Android app.
 *
 * Centralizes channel IDs, names, and descriptions so they can be
 * referenced consistently from any adapter or service that needs
 * to post or create notification channels.
 */
object NotificationChannels {
    const val SENSOR_ALERTS_ID = "sensor_alerts"
    const val SENSOR_ALERTS_NAME = "Sensor Alerts"
    const val SENSOR_ALERTS_DESCRIPTION = "Critical alerts from smart home sensors"
}
