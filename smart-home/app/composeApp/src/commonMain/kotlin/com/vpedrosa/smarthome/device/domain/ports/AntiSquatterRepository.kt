package com.vpedrosa.smarthome.device.domain.ports

import com.vpedrosa.smarthome.device.domain.AntiSquatterConfig
import kotlinx.coroutines.flow.Flow

interface AntiSquatterRepository {
    fun observeConfig(): Flow<AntiSquatterConfig>
    suspend fun saveConfig(config: AntiSquatterConfig)
}
