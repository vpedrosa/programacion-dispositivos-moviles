package com.vpedrosa.smarthome.commissioning.infrastructure.discovery

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.vpedrosa.smarthome.commissioning.domain.SimulatorDiscoveryPort
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

class MdnsSimulatorDiscoveryAdapter(
    private val context: Context,
) : SimulatorDiscoveryPort {

    override suspend fun discoverSimulatorHost(): String? {
        return withTimeoutOrNull(DISCOVERY_TIMEOUT_MS) {
            discoverMatter()
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
    }
}
