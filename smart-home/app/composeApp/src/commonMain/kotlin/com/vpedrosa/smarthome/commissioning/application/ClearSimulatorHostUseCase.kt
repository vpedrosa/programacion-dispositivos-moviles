package com.vpedrosa.smarthome.commissioning.application

import com.vpedrosa.smarthome.commissioning.domain.SimulatorHostRepository

class ClearSimulatorHostUseCase(
    private val simulatorHostRepository: SimulatorHostRepository,
) {
    suspend operator fun invoke() {
        simulatorHostRepository.clearHost()
    }
}
