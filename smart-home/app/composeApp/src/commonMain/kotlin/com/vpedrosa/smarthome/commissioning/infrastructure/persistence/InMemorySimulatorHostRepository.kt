package com.vpedrosa.smarthome.commissioning.infrastructure.persistence

import com.vpedrosa.smarthome.commissioning.domain.SimulatorHostRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemorySimulatorHostRepository : SimulatorHostRepository {

    private val store = MutableStateFlow<String?>(null)

    override fun observeHost(): Flow<String?> = store.asStateFlow()

    override suspend fun saveHost(host: String) {
        store.value = host
    }

    override suspend fun clearHost() {
        store.value = null
    }
}
