package com.vpedrosa.smarthome.antisquatter.infrastructure.persistence

import com.vpedrosa.smarthome.antisquatter.domain.model.AntiSquatterConfig
import com.vpedrosa.smarthome.antisquatter.domain.AntiSquatterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryAntiSquatterRepository : AntiSquatterRepository {

    private val store = MutableStateFlow(AntiSquatterConfig.DEFAULT)

    override fun observeConfig(): Flow<AntiSquatterConfig> = store.asStateFlow()

    override suspend fun saveConfig(config: AntiSquatterConfig) {
        store.value = config
    }
}
