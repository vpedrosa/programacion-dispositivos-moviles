package com.vpedrosa.smarthome.commissioning.application

import com.vpedrosa.smarthome.commissioning.domain.SimulatorDiscoveryPort
import com.vpedrosa.smarthome.commissioning.domain.SimulatorHostRepository

class SearchSimulatorUseCase(
    private val simulatorDiscoveryPort: SimulatorDiscoveryPort,
    private val simulatorHostRepository: SimulatorHostRepository,
) {
    /** Returns true if a simulator was found and saved, false otherwise. */
    suspend operator fun invoke(): Boolean {
        val host = simulatorDiscoveryPort.discoverSimulatorHost()
        return if (host != null) {
            simulatorHostRepository.saveHost(host)
            true
        } else {
            false
        }
    }
}
