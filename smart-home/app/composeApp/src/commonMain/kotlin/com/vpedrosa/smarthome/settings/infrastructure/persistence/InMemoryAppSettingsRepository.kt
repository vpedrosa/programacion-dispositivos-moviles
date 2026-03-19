package com.vpedrosa.smarthome.settings.infrastructure.persistence

import com.vpedrosa.smarthome.settings.domain.model.AppSettings
import com.vpedrosa.smarthome.settings.domain.AppSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryAppSettingsRepository : AppSettingsRepository {

    private val store = MutableStateFlow(AppSettings.DEFAULT)

    override fun observeSettings(): Flow<AppSettings> = store.asStateFlow()

    override suspend fun saveSettings(settings: AppSettings) {
        store.value = settings
    }
}
