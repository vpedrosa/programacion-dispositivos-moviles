package com.vpedrosa.smarthome.settings.domain.model

data class AppSettings(
    val notificationsEnabled: Boolean,
) {
    companion object {
        val DEFAULT = AppSettings(
            notificationsEnabled = true,
        )
    }
}
