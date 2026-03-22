package com.vpedrosa.smarthome.commissioning.infrastructure.discovery

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.vpedrosa.smarthome.shared.domain.model.DeviceType
import com.vpedrosa.smarthome.commissioning.domain.model.DiscoveredDevice
import com.vpedrosa.smarthome.commissioning.domain.DeviceDiscoveryPort
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Device discovery adapter for production using mDNS (NsdManager).
 *
 * Discovers Matter devices advertising the `_matter._tcp` service type
 * on the local network. Each discovered service is resolved to obtain
 * host, port, and TXT record attributes.
 */
class MdnsDeviceDiscoveryAdapter(
    private val context: Context,
) : DeviceDiscoveryPort {

    override fun discoverDevices(): Flow<List<DiscoveredDevice>> = callbackFlow {
        val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
        val discovered = mutableMapOf<String, DiscoveredDevice>()

        val discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(serviceType: String) {
                Log.d(TAG, "mDNS discovery started for $serviceType")
            }

            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                Log.d(TAG, "Service found: ${serviceInfo.serviceName}")
                nsdManager.resolveService(
                    serviceInfo,
                    object : NsdManager.ResolveListener {
                        override fun onResolveFailed(si: NsdServiceInfo, errorCode: Int) {
                            Log.w(TAG, "Resolve failed: ${si.serviceName}, error=$errorCode")
                        }

                        override fun onServiceResolved(si: NsdServiceInfo) {
                            Log.d(TAG, "Resolved: ${si.serviceName} at ${si.host}:${si.port}")
                            val device = si.toDiscoveredDevice() ?: return
                            discovered[device.serialNumber] = device
                            trySend(discovered.values.toList())
                        }
                    },
                )
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                Log.d(TAG, "Service lost: ${serviceInfo.serviceName}")
                discovered.remove(serviceInfo.serviceName)
                trySend(discovered.values.toList())
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Log.d(TAG, "mDNS discovery stopped")
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Discovery start failed: error=$errorCode")
                close()
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Discovery stop failed: error=$errorCode")
            }
        }

        nsdManager.discoverServices(MATTER_SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)

        awaitClose {
            try {
                nsdManager.stopServiceDiscovery(discoveryListener)
            } catch (_: Exception) {
                // Already stopped
            }
        }
    }

    private fun NsdServiceInfo.toDiscoveredDevice(): DiscoveredDevice? {
        val hostAddress = host?.hostAddress ?: return null
        val deviceName = serviceName ?: "Matter Device"
        val serialNumber = serviceName ?: "MDNS-${port}"

        return DiscoveredDevice(
            name = deviceName,
            type = DeviceType.LIGHT, // Default; actual type determined during commissioning
            host = hostAddress,
            port = port,
            discriminator = extractDiscriminator(),
            passcode = DEFAULT_PASSCODE,
            serialNumber = serialNumber,
        )
    }

    private fun NsdServiceInfo.extractDiscriminator(): Int {
        // Matter devices advertise discriminator in TXT record attribute "D"
        val txtRecord = attributes
        val discriminatorBytes = txtRecord?.get("D")
        return discriminatorBytes?.let { String(it).toIntOrNull() } ?: DEFAULT_DISCRIMINATOR
    }

    private companion object {
        const val TAG = "MdnsDeviceDiscovery"
        const val MATTER_SERVICE_TYPE = "_matter._tcp"
        const val DEFAULT_DISCRIMINATOR = 3840
        const val DEFAULT_PASSCODE = 20202021L
    }
}
