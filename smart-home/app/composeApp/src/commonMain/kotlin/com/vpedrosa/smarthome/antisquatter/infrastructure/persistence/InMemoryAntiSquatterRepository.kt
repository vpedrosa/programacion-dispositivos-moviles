package com.vpedrosa.smarthome.antisquatter.infrastructure.persistence

import com.vpedrosa.smarthome.antisquatter.domain.model.AntiSquatterConfig
import com.vpedrosa.smarthome.antisquatter.domain.model.VideoConfig
import com.vpedrosa.smarthome.antisquatter.domain.AntiSquatterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryAntiSquatterRepository : AntiSquatterRepository {

    private val store = MutableStateFlow(defaultConfig())

    override fun observeConfig(): Flow<AntiSquatterConfig> = store.asStateFlow()

    override suspend fun saveConfig(config: AntiSquatterConfig) {
        store.value = config
    }

    companion object {
        fun defaultConfig(): AntiSquatterConfig = AntiSquatterConfig(
            isEnabled = false,
            timeSlots = emptyList(),
            videoConfig = VideoConfig(
                isEnabled = false,
                startHour = 20,
                startMinute = 0,
                endHour = 22,
                endMinute = 0,
                videoUrl = "",
            ),
        )
    }
}
