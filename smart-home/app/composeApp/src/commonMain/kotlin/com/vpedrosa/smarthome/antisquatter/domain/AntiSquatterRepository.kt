package com.vpedrosa.smarthome.antisquatter.domain

import com.vpedrosa.smarthome.antisquatter.domain.model.AntiSquatterConfig
import kotlinx.coroutines.flow.Flow

interface AntiSquatterRepository {
    fun observeConfig(): Flow<AntiSquatterConfig>
    suspend fun saveConfig(config: AntiSquatterConfig)
}
