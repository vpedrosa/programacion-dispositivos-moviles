package com.vpedrosa.smarthome.settings.application

import com.vpedrosa.smarthome.settings.domain.AppSettingsRepository
import com.vpedrosa.smarthome.settings.domain.model.AppSettings
import kotlinx.coroutines.flow.first

class ToggleNotificationsUseCase(
    private val appSettingsRepository: AppSettingsRepository,
) {
    suspend operator fun invoke() {
        val current = appSettingsRepository.observeSettings().first()
        appSettingsRepository.saveSettings(
            AppSettings(notificationsEnabled = !current.notificationsEnabled),
        )
    }
}
