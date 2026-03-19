package com.vpedrosa.smarthome.settings.domain

import com.vpedrosa.smarthome.settings.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {
    fun observeSettings(): Flow<AppSettings>
    suspend fun saveSettings(settings: AppSettings)
}
