package com.vpedrosa.smarthome.commissioning.infrastructure.discovery

import android.util.Log
import com.vpedrosa.smarthome.commissioning.domain.DeviceDiscoveryPort
import com.vpedrosa.smarthome.commissioning.domain.model.DiscoveredDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Adaptador de descubrimiento para emulador Android.
 *
 * En lugar de mDNS (que no funciona en el emulador), prueba una conexión
 * TCP al primer puerto del simulador. Si responde, emite la lista completa
 * de dispositivos generada a partir de la configuración estática del simulador.
 *
 * Candidatos probados en orden:
 *   - 10.0.2.2 → localhost de la máquina host desde el emulador Android
 *   - 127.0.0.1 → loopback, útil con `adb forward` en dispositivo físico
 */
class LocalhostDeviceDiscoveryAdapter : DeviceDiscoveryPort {

    override fun discoverDevices(): Flow<List<DiscoveredDevice>> = flow {
        for (host in FALLBACK_HOSTS) {
            if (probeHost(host)) {
                val devices = SimulatorDeviceList.forHost(host)
                Log.d(TAG, "Simulador accesible en $host — emitiendo ${devices.size} dispositivos")
                emit(devices)
                return@flow
            }
        }
        Log.d(TAG, "Simulador no accesible en ningún host de fallback")
    }

    private suspend fun probeHost(host: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Socket().use { it.connect(InetSocketAddress(host, PROBE_PORT), PROBE_TIMEOUT_MS) }
            Log.d(TAG, "Probe OK: $host:$PROBE_PORT")
            true
        } catch (e: Exception) {
            Log.d(TAG, "Probe fallido: $host:$PROBE_PORT (${e.message})")
            false
        }
    }

    private companion object {
        const val TAG = "LocalhostDeviceDiscovery"
        const val PROBE_PORT = 8085   // WebSocket del simulador (ws-server.mjs) — el único puerto TCP
        const val PROBE_TIMEOUT_MS = 2_000
        val FALLBACK_HOSTS = listOf("10.0.2.2", "127.0.0.1")
    }
}
