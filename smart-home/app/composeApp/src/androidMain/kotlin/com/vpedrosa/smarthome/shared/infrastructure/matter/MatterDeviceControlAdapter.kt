package com.vpedrosa.smarthome.shared.infrastructure.matter

import android.util.Log
import chip.devicecontroller.ChipClusters
import chip.devicecontroller.ChipDeviceController
import com.vpedrosa.smarthome.shared.domain.model.DeviceId
import com.vpedrosa.smarthome.commissioning.domain.model.DiscoveredDevice
import com.vpedrosa.smarthome.shared.domain.DeviceControlPort
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Establece una sesión PASE fresca antes de cada comando.
 *
 * El handshake PASE es prácticamente instantáneo (<1ms) y evita
 * completamente el problema de sesiones expiradas, eliminando la
 * necesidad de cache de pointers y lógica de reintentos.
 */
class MatterDeviceControlAdapter(
    private val chipController: ChipDeviceController,
) : DeviceControlPort {

    private val deviceConnections = ConcurrentHashMap<Long, ConnectionInfo>()
    private val controlMutex = Mutex()
    private val paseNodeIdCounter = AtomicLong(PASE_NODE_ID_BASE)

    override fun registerDevice(deviceId: DeviceId, discoveredDevice: DiscoveredDevice) {
        val nodeId = deviceId.value.toLong()
        deviceConnections[nodeId] = ConnectionInfo(
            discoveredDevice.host,
            discoveredDevice.port,
            discoveredDevice.passcode,
        )
        Log.d(TAG, "Registered device $nodeId for control")
    }

    override suspend fun toggleOnOff(deviceId: DeviceId, on: Boolean) {
        executeCommand(deviceId) { pointer ->
            val cluster = ChipClusters.OnOffCluster(pointer, ENDPOINT_ID)
            suspendClusterCommand { cb ->
                if (on) cluster.on(cb) else cluster.off(cb)
            }
            Log.d(TAG, "${deviceId.value}: OnOff -> $on")
        }
    }

    override suspend fun setLevel(deviceId: DeviceId, level: Int) {
        executeCommand(deviceId) { pointer ->
            val cluster = ChipClusters.LevelControlCluster(pointer, ENDPOINT_ID)
            val matterLevel = (level * 254 / 100).coerceIn(0, 254)
            suspendClusterCommand { cb ->
                cluster.moveToLevel(cb, matterLevel, 0, 0, 0)
            }
            Log.d(TAG, "${deviceId.value}: Level -> $level% ($matterLevel)")
        }
    }

    override suspend fun lockDoor(deviceId: DeviceId, lock: Boolean) {
        executeCommand(deviceId) { pointer ->
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
        executeCommand(deviceId) { pointer ->
            val cluster = ChipClusters.ThermostatCluster(pointer, ENDPOINT_ID)
            val matterTemp = (temperatureCelsius * 100).toInt()
            suspendClusterCommand { cb ->
                cluster.writeOccupiedHeatingSetpointAttribute(cb, matterTemp)
            }
            Log.d(TAG, "${deviceId.value}: Setpoint -> ${temperatureCelsius}°C")
        }
    }

    override suspend fun setThermostatMode(deviceId: DeviceId, heating: Boolean) {
        executeCommand(deviceId) { pointer ->
            val cluster = ChipClusters.ThermostatCluster(pointer, ENDPOINT_ID)
            val mode = if (heating) SYSTEM_MODE_HEAT else SYSTEM_MODE_OFF
            suspendClusterCommand { cb ->
                cluster.writeSystemModeAttribute(cb, mode)
            }
            Log.d(TAG, "${deviceId.value}: SystemMode -> $mode (heating=$heating)")
        }
    }

    override suspend fun setWindowCoveringPosition(deviceId: DeviceId, openPercent: Int) {
        executeCommand(deviceId) { pointer ->
            val cluster = ChipClusters.WindowCoveringCluster(pointer, ENDPOINT_ID)
            val percent100ths = (openPercent * 100).coerceIn(0, 10000)
            suspendClusterCommand { cb ->
                cluster.goToLiftPercentage(cb, percent100ths)
            }
            Log.d(TAG, "${deviceId.value}: WindowCovering -> $openPercent% ($percent100ths)")
        }
    }

    /**
     * Establece PASE fresco y ejecuta el comando.
     *
     * Usa un nodeId temporal único para cada llamada, forzando al
     * ChipDeviceController a crear una sesión nueva en lugar de
     * reutilizar una posiblemente expirada.
     */
    private suspend fun executeCommand(
        deviceId: DeviceId,
        block: suspend (Long) -> Unit,
    ) {
        controlMutex.withLock {
            val pointer = freshDevicePointer(deviceId)
            block(pointer)
        }
    }

    private suspend fun freshDevicePointer(deviceId: DeviceId): Long {
        val nodeId = deviceId.value.toLong()
        val conn = deviceConnections[nodeId]
            ?: throw IllegalStateException("Device $nodeId not registered for control")

        val paseNodeId = paseNodeIdCounter.getAndIncrement()
        establishPase(paseNodeId, conn)
        return chipController.getDeviceBeingCommissionedPointer(paseNodeId)
    }

    private suspend fun establishPase(
        nodeId: Long,
        conn: ConnectionInfo,
    ) = suspendCancellableCoroutine { cont ->
        chipController.setCompletionListener(object : ChipDeviceController.CompletionListener {
            override fun onPairingComplete(code: Int) {
                if (code == 0) {
                    Log.d(TAG, "PASE OK: node $nodeId")
                    if (cont.isActive) cont.resume(Unit)
                } else {
                    Log.e(TAG, "PASE failed: node $nodeId, code=$code")
                    if (cont.isActive) {
                        cont.resumeWithException(
                            RuntimeException("PASE failed with code $code for node $nodeId"),
                        )
                    }
                }
            }

            override fun onError(error: Throwable?) {
                Log.e(TAG, "PASE error: node $nodeId", error)
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
    ) = withTimeout(COMMAND_TIMEOUT_MS) {
        suspendCancellableCoroutine { cont ->
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
    }

    private data class ConnectionInfo(val host: String, val port: Int, val passcode: Long)

    private companion object {
        const val TAG = "MatterDeviceControl"
        const val ENDPOINT_ID = 1
        const val TIMED_INVOKE_TIMEOUT = 10_000
        const val SYSTEM_MODE_OFF = 0
        const val SYSTEM_MODE_HEAT = 4
        const val COMMAND_TIMEOUT_MS = 3_000L
        const val PASE_NODE_ID_BASE = 100_000L
    }
}
