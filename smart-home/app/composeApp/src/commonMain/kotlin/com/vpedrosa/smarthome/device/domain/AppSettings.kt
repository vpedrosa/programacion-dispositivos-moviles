package com.vpedrosa.smarthome.device.domain

data class AppSettings(
    val sensorAlertsEnabled: Boolean,
    val doorAlertEnabled: Boolean,
    val thermostatEventsEnabled: Boolean,
) {
    companion object {
        val DEFAULT = AppSettings(
            sensorAlertsEnabled = true,
            doorAlertEnabled = true,
            thermostatEventsEnabled = false,
        )
    }
}
