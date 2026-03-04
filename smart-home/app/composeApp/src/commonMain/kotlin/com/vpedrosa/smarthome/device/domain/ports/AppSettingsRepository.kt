package com.vpedrosa.smarthome.device.domain.ports

import com.vpedrosa.smarthome.device.domain.AppSettings
import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {
    fun observeSettings(): Flow<AppSettings>
    suspend fun saveSettings(settings: AppSettings)
}
