package com.vpedrosa.smarthome.device.adapters.matter

import android.util.Log
import chip.devicecontroller.ChipClusters
import chip.devicecontroller.ChipDeviceController
import com.vpedrosa.smarthome.device.domain.DeviceId
import com.vpedrosa.smarthome.device.domain.DiscoveredDevice
import com.vpedrosa.smarthome.device.domain.ports.DeviceControlPort
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MatterDeviceControlAdapter(
    private val chipController: ChipDeviceController,
) : DeviceControlPort {

    private val deviceConnections = ConcurrentHashMap<Long, ConnectionInfo>()
    private val controlMutex = Mutex()

    override fun registerDevice(deviceId: DeviceId, discoveredDevice: DiscoveredDevice) {
        val nodeId = deviceId.value.toLong()
        deviceConnections[nodeId] = ConnectionInfo(discoveredDevice.host, discoveredDevice.port, discoveredDevice.passcode)
    }

    override suspend fun toggleOnOff(deviceId: DeviceId, on: Boolean) {
        controlMutex.withLock {
            val pointer = getDevicePointer(deviceId)
            val cluster = ChipClusters.OnOffCluster(pointer, ENDPOINT_ID)
            suspendClusterCommand { cb ->
                if (on) cluster.on(cb) else cluster.off(cb)
            }
            Log.d(TAG, "${deviceId.value}: OnOff -> $on")
        }
    }

    override suspend fun setLevel(deviceId: DeviceId, level: Int) {
        controlMutex.withLock {
            val pointer = getDevicePointer(deviceId)
            val cluster = ChipClusters.LevelControlCluster(pointer, ENDPOINT_ID)
            val matterLevel = (level * 254 / 100).coerceIn(0, 254)
            suspendClusterCommand { cb ->
                cluster.moveToLevel(cb, matterLevel, 0, 0, 0)
            }
            Log.d(TAG, "${deviceId.value}: Level -> $level% ($matterLevel)")
        }
    }

    override suspend fun lockDoor(deviceId: DeviceId, lock: Boolean) {
        controlMutex.withLock {
            val pointer = getDevicePointer(deviceId)
            val cluster = ChipClusters.DoorLockCluster(pointer, ENDPOINT_ID)
            val noPin = Optional.empty<ByteArray>()
            suspendClusterCommand { cb ->
                if (lock) cluster.lockDoor(cb, noPin, TIMED_INVOKE_TIMEOUT)
                else cluster.unlockDoor(cb, noPin, TIMED_INVOKE_TIMEOUT)
            }
            Log.d(TAG, "${deviceId.value}: Lock -> $lock")
        }
    }

    override suspend fun setThermostatSetpoint(deviceId: DeviceId, temperatureCelsius: Double) {
        controlMutex.withLock {
            val pointer = getDevicePointer(deviceId)
            val cluster = ChipClusters.ThermostatCluster(pointer, ENDPOINT_ID)
            val matterTemp = (temperatureCelsius * 100).toInt()
            suspendClusterCommand { cb ->
                cluster.writeOccupiedHeatingSetpointAttribute(cb, matterTemp)
            }
            Log.d(TAG, "${deviceId.value}: Setpoint -> ${temperatureCelsius}°C")
        }
    }

    override suspend fun setThermostatMode(deviceId: DeviceId, heating: Boolean) {
        controlMutex.withLock {
            val pointer = getDevicePointer(deviceId)
            val cluster = ChipClusters.ThermostatCluster(pointer, ENDPOINT_ID)
            val mode = if (heating) SYSTEM_MODE_HEAT else SYSTEM_MODE_OFF
            suspendClusterCommand { cb ->
                cluster.writeSystemModeAttribute(cb, mode)
            }
            Log.d(TAG, "${deviceId.value}: SystemMode -> $mode (heating=$heating)")
        }
    }

    override suspend fun setWindowCoveringPosition(deviceId: DeviceId, openPercent: Int) {
        controlMutex.withLock {
            val pointer = getDevicePointer(deviceId)
            val cluster = ChipClusters.WindowCoveringCluster(pointer, ENDPOINT_ID)
            val percent100ths = (openPercent * 100).coerceIn(0, 10000)
            suspendClusterCommand { cb ->
                cluster.goToLiftPercentage(cb, percent100ths)
            }
            Log.d(TAG, "${deviceId.value}: WindowCovering -> $openPercent% ($percent100ths)")
        }
    }

    /**
     * Obtiene un device pointer estableciendo una conexión PASE directa.
     *
     * No usa getConnectedDevicePointer (que necesita mDNS/CASE) porque mDNS
     * no funciona en el emulador Android. En su lugar, re-establece PASE
     * con la dirección conocida y usa getDeviceBeingCommissioned.
     */
    private suspend fun getDevicePointer(deviceId: DeviceId): Long {
        val nodeId = deviceId.value.toLong()
        val conn = deviceConnections[nodeId]
            ?: throw IllegalStateException("Device $nodeId not registered for control")

        establishPaseForControl(nodeId, conn)
        return chipController.getDeviceBeingCommissionedPointer(nodeId)
    }

    private suspend fun establishPaseForControl(
        nodeId: Long,
        conn: ConnectionInfo,
    ) = suspendCancellableCoroutine { cont ->
        chipController.setCompletionListener(object : ChipDeviceController.CompletionListener {
            override fun onPairingComplete(code: Int) {
                Log.d(TAG, "PASE for control: node $nodeId, code=$code")
                if (cont.isActive) cont.resume(Unit)
            }

            override fun onError(error: Throwable?) {
                Log.e(TAG, "PASE for control failed: node $nodeId", error)
                if (cont.isActive) {
                    cont.resumeWithException(
                        error ?: RuntimeException("PASE failed for node $nodeId"),
                    )
                }
            }

            override fun onConnectDeviceComplete() {}
            override fun onStatusUpdate(status: Int) {}
            override fun onPairingDeleted(code: Int) {}
            override fun onCommissioningComplete(nodeId: Long, errorCode: Int) {}
            override fun onCommissioningStatusUpdate(nodeId: Long, stage: String?, errorCode: Int) {}
            override fun onNotifyChipConnectionClosed() {}
            override fun onCloseBleComplete() {}
            override fun onReadCommissioningInfo(
                vendorId: Int, productId: Int, wifiEndpointId: Int, threadEndpointId: Int,
            ) {}
            override fun onOpCSRGenerationComplete(csr: ByteArray?) {}
        })

        chipController.establishPaseConnection(nodeId, conn.host, conn.port, conn.passcode)
    }

    private suspend fun suspendClusterCommand(
        block: (ChipClusters.DefaultClusterCallback) -> Unit,
    ) = suspendCancellableCoroutine { cont ->
        block(object : ChipClusters.DefaultClusterCallback {
            override fun onSuccess() {
                if (cont.isActive) cont.resume(Unit)
            }

            override fun onError(error: Exception) {
                Log.e(TAG, "Cluster command error", error)
                if (cont.isActive) cont.resumeWithException(error)
            }
        })
    }

    private data class ConnectionInfo(val host: String, val port: Int, val passcode: Long)

    private companion object {
        const val TAG = "MatterDeviceControl"
        const val ENDPOINT_ID = 1
        const val TIMED_INVOKE_TIMEOUT = 10_000
        const val SYSTEM_MODE_OFF = 0
        const val SYSTEM_MODE_HEAT = 4
    }
}
