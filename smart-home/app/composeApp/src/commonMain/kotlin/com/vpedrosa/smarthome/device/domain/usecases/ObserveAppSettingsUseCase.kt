package com.vpedrosa.smarthome.device.domain.usecases

import com.vpedrosa.smarthome.device.domain.AppSettings
import com.vpedrosa.smarthome.device.domain.ports.AppSettingsRepository
import kotlinx.coroutines.flow.Flow

class ObserveAppSettingsUseCase(
    private val repository: AppSettingsRepository,
) {
    operator fun invoke(): Flow<AppSettings> =
        repository.observeSettings()
}
