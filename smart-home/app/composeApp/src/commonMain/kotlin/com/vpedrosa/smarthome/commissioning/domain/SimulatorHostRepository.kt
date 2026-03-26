package com.vpedrosa.smarthome.commissioning.domain

import kotlinx.coroutines.flow.Flow

interface SimulatorHostRepository {
    fun observeHost(): Flow<String?>
    suspend fun saveHost(host: String)
    suspend fun clearHost()
}
