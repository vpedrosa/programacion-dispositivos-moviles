package com.vpedrosa.smarthome.antisquatter.application

import com.vpedrosa.smarthome.antisquatter.domain.AntiSquatterRepository
import kotlinx.coroutines.flow.first

class ToggleAntiSquatterUseCase(
    private val antiSquatterRepository: AntiSquatterRepository,
) {
    suspend operator fun invoke() {
        val current = antiSquatterRepository.observeConfig().first()
        antiSquatterRepository.saveConfig(current.copy(isEnabled = !current.isEnabled))
    }
}
