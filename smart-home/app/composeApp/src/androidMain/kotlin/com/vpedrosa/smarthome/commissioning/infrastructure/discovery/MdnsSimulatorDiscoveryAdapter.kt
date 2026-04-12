package com.vpedrosa.smarthome.commissioning.infrastructure.discovery

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.vpedrosa.smarthome.commissioning.domain.SimulatorDiscoveryPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.coroutines.resume

class MdnsSimulatorDiscoveryAdapter(
    private val context: Context,
) : SimulatorDiscoveryPort {

    override suspend fun discoverSimulatorHost(): String? {
        val mdnsResult = withTimeoutOrNull(DISCOVERY_TIMEOUT_MS) {
            discoverMatter()
        }
        if (mdnsResult != null) return mdnsResult

        Log.d(TAG, "mDNS discovery timed out, trying localhost fallback")
        return probeLocalhostFallback()
    }

    /**
     * Fallback para emulador Android: el localhost de la máquina host
     * es accesible en 10.0.2.2, y en 127.0.0.1 si se ejecuta en dispositivo físico
     * con port-forward. Prueba cada candidato con una conexión TCP al puerto del hub.
     */
    private suspend fun probeLocalhostFallback(): String? =
        EMULATOR_FALLBACK_HOSTS.firstOrNull { host -> probeHost(host) }

    private suspend fun probeHost(host: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, HUB_PORT), PROBE_TIMEOUT_MS.toInt())
                Log.d(TAG, "Hub reachable at $host:$HUB_PORT")
            }
            true
        } catch (e: Exception) {
            Log.d(TAG, "Hub not reachable at $host:$HUB_PORT (${e.message})")
            false
        }
    }

    private suspend fun discoverMatter(): String? = suspendCancellableCoroutine { cont ->
        val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
        var resolved = false

        var discoveryListener: NsdManager.DiscoveryListener? = null
        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(serviceType: String) {
                Log.d(TAG, "mDNS discovery started for $serviceType")
            }

            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                Log.d(TAG, "Service found: ${serviceInfo.serviceName} type=${serviceInfo.serviceType}")
                if (resolved) return
                nsdManager.registerServiceInfoCallback(
                    serviceInfo,
                    { it.run() },
                    object : NsdManager.ServiceInfoCallback {
                        override fun onServiceInfoCallbackRegistrationFailed(errorCode: Int) {
                            Log.w(TAG, "ServiceInfo registration failed: error=$errorCode")
                        }

                        override fun onServiceUpdated(si: NsdServiceInfo) {
                            if (resolved) return
                            val host = si.hostAddresses.firstOrNull()?.hostAddress
                            Log.d(TAG, "Resolved: ${si.serviceName} -> $host:${si.port}")
                            if (host != null) {
                                resolved = true
                                nsdManager.unregisterServiceInfoCallback(this)
                                try { nsdManager.stopServiceDiscovery(discoveryListener!!) } catch (_: Exception) {}
                                if (cont.isActive) cont.resume(host)
                            }
                        }

                        override fun onServiceLost() {
                            Log.d(TAG, "Service lost during resolve")
                        }

                        override fun onServiceInfoCallbackUnregistered() {}
                    },
                )
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                Log.d(TAG, "Service lost: ${serviceInfo.serviceName}")
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Log.d(TAG, "mDNS discovery stopped")
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Discovery start failed: error=$errorCode")
                if (cont.isActive) cont.resume(null)
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.w(TAG, "Discovery stop failed: error=$errorCode")
            }
        }

        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)

        cont.invokeOnCancellation {
            try { nsdManager.stopServiceDiscovery(discoveryListener) } catch (_: Exception) {}
        }
    }

    private companion object {
        const val TAG = "MdnsSimDiscovery"
        const val SERVICE_TYPE = "_smarthome-hub._tcp"
        const val DISCOVERY_TIMEOUT_MS = 10_000L
        const val HUB_PORT = 8085    // WebSocket del simulador (ws-server.mjs) — el único puerto TCP
        const val PROBE_TIMEOUT_MS = 2_000L
        val EMULATOR_FALLBACK_HOSTS = listOf(
            "10.0.2.2",   // host machine desde el emulador Android
            "127.0.0.1",  // loopback (útil con adb port-forward en dispositivo físico)
        )
    }
}
