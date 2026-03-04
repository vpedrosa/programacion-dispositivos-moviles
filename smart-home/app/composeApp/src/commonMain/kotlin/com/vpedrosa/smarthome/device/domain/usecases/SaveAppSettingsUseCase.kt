package com.vpedrosa.smarthome.device.domain.usecases

import com.vpedrosa.smarthome.device.domain.AppSettings
import com.vpedrosa.smarthome.device.domain.ports.AppSettingsRepository

class SaveAppSettingsUseCase(
    private val repository: AppSettingsRepository,
) {
    suspend operator fun invoke(settings: AppSettings) {
        repository.saveSettings(settings)
    }
}
