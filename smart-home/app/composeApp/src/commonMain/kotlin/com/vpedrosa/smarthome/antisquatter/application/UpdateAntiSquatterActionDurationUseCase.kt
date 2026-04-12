package com.vpedrosa.smarthome.antisquatter.application

import com.vpedrosa.smarthome.antisquatter.domain.AntiSquatterRepository
import kotlinx.coroutines.flow.first

class UpdateAntiSquatterActionDurationUseCase(
    private val antiSquatterRepository: AntiSquatterRepository,
) {
    /** [minutes] must be > 0, as enforced by [AntiSquatterConfig]. No-op otherwise. */
    suspend operator fun invoke(minutes: Int) {
        if (minutes <= 0) return
        val current = antiSquatterRepository.observeConfig().first()
        antiSquatterRepository.saveConfig(current.copy(actionDurationMinutes = minutes))
    }
}
