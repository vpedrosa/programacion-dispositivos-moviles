package com.vpedrosa.smarthome.shared.infrastructure.matter

import android.util.Log
import chip.devicecontroller.ChipClusters
import chip.devicecontroller.ChipDeviceController
import com.vpedrosa.smarthome.shared.domain.model.DeviceId
import com.vpedrosa.smarthome.commissioning.domain.model.DiscoveredDevice
import com.vpedrosa.smarthome.shared.domain.DeviceControlPort
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MatterDeviceControlAdapter(
    private val chipController: ChipDeviceController,
) : DeviceControlPort {

    private val deviceConnections = ConcurrentHashMap<Long, ConnectionInfo>()
    private val cachedPointers = ConcurrentHashMap<Long, Long>()
    private val controlMutex = Mutex()

    /**
     * Counter to generate unique temporary nodeIds for PASE re-establishment.
     * The ChipDeviceController reuses internal sessions when called with the
     * same nodeId, so we use a different one each time to force a fresh session.
     */
    private val retryNodeIdCounter = AtomicLong(RETRY_NODE_ID_BASE)

    override fun registerDevice(deviceId: DeviceId, discoveredDevice: DiscoveredDevice) {
        val nodeId = deviceId.value.toLong()
        deviceConnections[nodeId] = ConnectionInfo(discoveredDevice.host, discoveredDevice.port, discoveredDevice.passcode)
        try {
            val pointer = chipController.getDeviceBeingCommissionedPointer(nodeId)
            cachedPointers[nodeId] = pointer
            Log.d(TAG, "Cached pointer for node $nodeId from commissioning session")
        } catch (e: Exception) {
            Log.w(TAG, "Could not cache pointer for node $nodeId at registration", e)
        }
    }

    override suspend fun toggleOnOff(deviceId: DeviceId, on: Boolean) {
        executeWithRetry(deviceId) { pointer ->
            val cluster = ChipClusters.OnOffCluster(pointer, ENDPOINT_ID)
            suspendClusterCommand { cb ->
                if (on) cluster.on(cb) else cluster.off(cb)
            }
            Log.d(TAG, "${deviceId.value}: OnOff -> $on")
        }
    }

    override suspend fun setLevel(deviceId: DeviceId, level: Int) {
        executeWithRetry(deviceId) { pointer ->
            val cluster = ChipClusters.LevelControlCluster(pointer, ENDPOINT_ID)
            val matterLevel = (level * 254 / 100).coerceIn(0, 254)
            suspendClusterCommand { cb ->
                cluster.moveToLevel(cb, matterLevel, 0, 0, 0)
            }
            Log.d(TAG, "${deviceId.value}: Level -> $level% ($matterLevel)")
        }
    }

    override suspend fun lockDoor(deviceId: DeviceId, lock: Boolean) {
        executeWithRetry(deviceId) { pointer ->
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
        executeWithRetry(deviceId) { pointer ->
            val cluster = ChipClusters.ThermostatCluster(pointer, ENDPOINT_ID)
            val matterTemp = (temperatureCelsius * 100).toInt()
            suspendClusterCommand { cb ->
                cluster.writeOccupiedHeatingSetpointAttribute(cb, matterTemp)
            }
            Log.d(TAG, "${deviceId.value}: Setpoint -> ${temperatureCelsius}°C")
        }
    }

    override suspend fun setThermostatMode(deviceId: DeviceId, heating: Boolean) {
        executeWithRetry(deviceId) { pointer ->
            val cluster = ChipClusters.ThermostatCluster(pointer, ENDPOINT_ID)
            val mode = if (heating) SYSTEM_MODE_HEAT else SYSTEM_MODE_OFF
            suspendClusterCommand { cb ->
                cluster.writeSystemModeAttribute(cb, mode)
            }
            Log.d(TAG, "${deviceId.value}: SystemMode -> $mode (heating=$heating)")
        }
    }

    override suspend fun setWindowCoveringPosition(deviceId: DeviceId, openPercent: Int) {
        executeWithRetry(deviceId) { pointer ->
            val cluster = ChipClusters.WindowCoveringCluster(pointer, ENDPOINT_ID)
            val percent100ths = (openPercent * 100).coerceIn(0, 10000)
            suspendClusterCommand { cb ->
                cluster.goToLiftPercentage(cb, percent100ths)
            }
            Log.d(TAG, "${deviceId.value}: WindowCovering -> $openPercent% ($percent100ths)")
        }
    }

    /**
     * Ejecuta un comando con reintentos automáticos.
     *
     * Usa el pointer cacheado de la sesión PASE del comisionamiento.
     * Si el comando falla (sesión expirada), invalida la caché,
     * re-establece PASE con un nodeId temporal (para forzar sesión nueva)
     * y reintenta una vez.
     */
    private suspend fun executeWithRetry(
        deviceId: DeviceId,
        block: suspend (Long) -> Unit,
    ) {
        controlMutex.withLock {
            try {
                block(getDevicePointer(deviceId))
            } catch (e: Exception) {
                Log.w(TAG, "Command failed for ${deviceId.value}, re-establishing session", e)
                invalidateSession(deviceId)
                delay(RETRY_DELAY_MS)
                try {
                    block(getFreshDevicePointer(deviceId))
                } catch (retryError: Exception) {
                    Log.e(TAG, "Retry also failed for ${deviceId.value}", retryError)
                    throw retryError
                }
            }
        }
    }

    /**
     * Obtiene el pointer cacheado, o establece PASE con el nodeId original
     * si no hay caché (primer uso tras comisionamiento).
     */
    private suspend fun getDevicePointer(deviceId: DeviceId): Long {
        val nodeId = deviceId.value.toLong()

        cachedPointers[nodeId]?.let { return it }

        val conn = deviceConnections[nodeId]
            ?: throw IllegalStateException("Device $nodeId not registered for control")

        establishPaseForControl(nodeId, conn)
        val pointer = chipController.getDeviceBeingCommissionedPointer(nodeId)
        cachedPointers[nodeId] = pointer
        return pointer
    }

    /**
     * Fuerza una sesión PASE completamente nueva usando un nodeId temporal.
     *
     * El ChipDeviceController reutiliza sesiones internas cuando se llama
     * a establishPaseConnection con el mismo nodeId. Usando un nodeId
     * diferente cada vez, forzamos la creación de una sesión nueva con
     * un LSID nuevo que el simulador reconoce.
     */
    private suspend fun getFreshDevicePointer(deviceId: DeviceId): Long {
        val nodeId = deviceId.value.toLong()
        val conn = deviceConnections[nodeId]
            ?: throw IllegalStateException("Device $nodeId not registered for control")

        val tempNodeId = retryNodeIdCounter.getAndIncrement()
        Log.d(TAG, "Re-establishing PASE with temp nodeId $tempNodeId for device ${deviceId.value}")

        establishPaseForControl(tempNodeId, conn)
        val pointer = chipController.getDeviceBeingCommissionedPointer(tempNodeId)
        cachedPointers[nodeId] = pointer
        return pointer
    }

    private fun invalidateSession(deviceId: DeviceId) {
        val nodeId = deviceId.value.toLong()
        cachedPointers.remove(nodeId)
        Log.d(TAG, "Invalidated cached session for node $nodeId")
    }

    private suspend fun establishPaseForControl(
        nodeId: Long,
        conn: ConnectionInfo,
    ) = suspendCancellableCoroutine { cont ->
        chipController.setCompletionListener(object : ChipDeviceController.CompletionListener {
            override fun onPairingComplete(code: Int) {
                if (code == 0) {
                    Log.d(TAG, "PASE for control: node $nodeId OK")
                    if (cont.isActive) cont.resume(Unit)
                } else {
                    Log.e(TAG, "PASE for control failed: node $nodeId, code=$code")
                    if (cont.isActive) {
                        cont.resumeWithException(
                            RuntimeException("PASE failed with code $code for node $nodeId"),
                        )
                    }
                }
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
        const val RETRY_DELAY_MS = 1_000L
        const val COMMAND_TIMEOUT_MS = 3_000L
        const val RETRY_NODE_ID_BASE = 100_000L
    }
}
