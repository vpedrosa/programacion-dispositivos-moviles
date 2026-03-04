package com.vpedrosa.smarthome.device.domain.usecases

import com.vpedrosa.smarthome.device.domain.AntiSquatterConfig
import com.vpedrosa.smarthome.device.domain.ports.AntiSquatterRepository

class SaveAntiSquatterConfigUseCase(
    private val repository: AntiSquatterRepository,
) {
    suspend operator fun invoke(config: AntiSquatterConfig) {
        repository.saveConfig(config)
    }
}
