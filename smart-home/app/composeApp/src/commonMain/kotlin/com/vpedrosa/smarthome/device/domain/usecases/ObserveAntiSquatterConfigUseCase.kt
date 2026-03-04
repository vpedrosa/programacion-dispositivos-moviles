package com.vpedrosa.smarthome.device.domain.usecases

import com.vpedrosa.smarthome.device.domain.AntiSquatterConfig
import com.vpedrosa.smarthome.device.domain.ports.AntiSquatterRepository
import kotlinx.coroutines.flow.Flow

class ObserveAntiSquatterConfigUseCase(
    private val repository: AntiSquatterRepository,
) {
    operator fun invoke(): Flow<AntiSquatterConfig> =
        repository.observeConfig()
}
