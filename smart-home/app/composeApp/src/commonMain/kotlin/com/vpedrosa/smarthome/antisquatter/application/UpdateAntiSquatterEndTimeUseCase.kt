package com.vpedrosa.smarthome.antisquatter.application

import com.vpedrosa.smarthome.antisquatter.domain.AntiSquatterRepository
import kotlinx.coroutines.flow.first

class UpdateAntiSquatterEndTimeUseCase(
    private val antiSquatterRepository: AntiSquatterRepository,
) {
    suspend operator fun invoke(hour: Int, minute: Int) {
        val current = antiSquatterRepository.observeConfig().first()
        antiSquatterRepository.saveConfig(current.copy(endHour = hour, endMinute = minute))
    }
}
