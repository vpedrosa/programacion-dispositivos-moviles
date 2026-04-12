package com.vpedrosa.smarthome.commissioning.infrastructure.discovery

import android.util.Log
import com.vpedrosa.smarthome.commissioning.domain.DeviceDiscoveryPort
import com.vpedrosa.smarthome.commissioning.domain.model.DiscoveredDevice
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Adaptador de descubrimiento con fallback automático.
 *
 * Estrategia:
 *   1. Lanza el descubrimiento primario (mDNS) en background.
 *   2. Espera [fallbackDelayMs] milisegundos.
 *   3. Si el primario no emitió ningún dispositivo, lanza el fallback (localhost).
 *   4. Si el primario encontró aunque sea uno, el fallback no se lanza.
 *
 * El flujo permanece abierto mientras el consumidor lo observe; el job
 * mDNS se cancela automáticamente cuando el consumidor cancela.
 */
class FallbackDeviceDiscoveryAdapter(
    private val primary: DeviceDiscoveryPort,
    private val fallback: DeviceDiscoveryPort,
    private val fallbackDelayMs: Long,
) : DeviceDiscoveryPort {

    override fun discoverDevices(): Flow<List<DiscoveredDevice>> = channelFlow {
        val primaryFoundDevices = AtomicBoolean(false)

        launch {
            primary.discoverDevices().collect { devices ->
                if (devices.isNotEmpty()) primaryFoundDevices.set(true)
                send(devices)
            }
        }

        delay(fallbackDelayMs)

        if (!primaryFoundDevices.get()) {
            Log.d(TAG, "mDNS no encontró dispositivos tras ${fallbackDelayMs}ms — lanzando fallback")
            fallback.discoverDevices().collect { devices ->
                send(devices)
            }
        }
    }

    private companion object {
        const val TAG = "FallbackDeviceDiscovery"
    }
}
