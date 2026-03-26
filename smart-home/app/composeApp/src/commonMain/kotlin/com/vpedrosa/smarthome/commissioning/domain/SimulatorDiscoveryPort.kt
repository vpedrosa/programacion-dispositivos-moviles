package com.vpedrosa.smarthome.commissioning.domain

/**
 * Searches the local network for Matter devices via mDNS (_matter._tcp).
 * Returns the host IP if found, null otherwise.
 */
interface SimulatorDiscoveryPort {
    suspend fun discoverSimulatorHost(): String?
}
