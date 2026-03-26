package com.vpedrosa.smarthome.shared.infrastructure.matter

import android.util.Log
import chip.devicecontroller.ChipClusters
import chip.devicecontroller.ChipDeviceController
import com.vpedrosa.smarthome.shared.domain.model.DeviceConnectionInfo
import com.vpedrosa.smarthome.shared.domain.model.DeviceId
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

/**
 * Controla dispositivos Matter usando la sesión PASE del comisionamiento.
 *
 * Cachea el pointer obtenido durante el comisionamiento y lo reutiliza.
 * Si la sesión expira, intenta re-establecer PASE con un nodeId temporal
 * para forzar una sesión nueva. Si el simulador ha cerrado la ventana de
 * comisionamiento, el retry también fallará.
 */
class MatterDeviceControlAdapter(
    private val chipController: ChipDeviceController,
) : DeviceControlPort {

    private val deviceConnections = ConcurrentHashMap<Long, ConnectionInfo>()
    private val cachedPointers = ConcurrentHashMap<Long, Long>()
    private val controlMutex = Mutex()
    private val retryNodeIdCounter = AtomicLong(RETRY_NODE_ID_BASE)

    override fun registerDevice(deviceId: DeviceId, connectionInfo: DeviceConnectionInfo) {
        val nodeId = deviceId.value.toLong()
        deviceConnections[nodeId] = ConnectionInfo(
            connectionInfo.host,
            connectionInfo.port,
            connectionInfo.passcode,
        )
        try {
            val pointer = chipController.getDeviceBeingCommissionedPointer(nodeId)
            cachedPointers[nodeId] = pointer
            Log.d(TAG, "Cached pointer for node $nodeId (port ${connectionInfo.port})")
        } catch (e: Exception) {
            Log.w(TAG, "Could not cache pointer for node $nodeId", e)
        }
    }

    override fun deregisterDevice(deviceId: DeviceId) {
        val nodeId = deviceId.value.toLong()
        cachedPointers.remove(nodeId)
        deviceConnections.remove(nodeId)
        Log.d(TAG, "Deregistered device $nodeId")
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

    override suspend fun launchContent(deviceId: DeviceId, url: String) {
        executeWithRetry(deviceId) { pointer ->
            suspendContentLaunch(pointer, url)
            Log.d(TAG, "${deviceId.value}: LaunchURL -> $url")
        }
    }

    private suspend fun suspendContentLaunch(
        devicePtr: Long,
        url: String,
    ) = withTimeout(COMMAND_TIMEOUT_MS) {
        suspendCancellableCoroutine { cont ->
            val tlvWriter = chip.tlv.TlvWriter()
            tlvWriter.startStructure(chip.tlv.AnonymousTag)
            tlvWriter.put(chip.tlv.ContextSpecificTag(0), url) // contentUrl (field 0)
            tlvWriter.endStructure()

            chipController.invoke(
                object : chip.devicecontroller.InvokeCallback {
                    override fun onResponse(
                        invokeElement: chip.devicecontroller.model.InvokeElement?,
                        successCode: Long,
                    ) {
                        Log.d(TAG, "LaunchURL invoke success: $successCode")
                        if (cont.isActive) cont.resume(Unit)
                    }

                    override fun onError(error: Exception) {
                        Log.e(TAG, "LaunchURL invoke error", error)
                        if (cont.isActive) cont.resumeWithException(error)
                    }
                },
                devicePtr,
                chip.devicecontroller.model.InvokeElement.newInstance(
                    ENDPOINT_ID.toLong(),
                    CONTENT_LAUNCHER_CLUSTER_ID,
                    LAUNCH_URL_COMMAND_ID,
                    tlvWriter.getEncoded(),
                    null,
                ),
                0, // timedRequestTimeoutMs (0 = no timed invoke)
                0, // imTimeoutMs
            )
        }
    }

    /**
     * Ejecuta un comando usando el pointer cacheado del comisionamiento.
     * Si falla (sesión expirada), intenta re-establecer PASE con nodeId
     * temporal para forzar sesión nueva.
     */
    private suspend fun executeWithRetry(
        deviceId: DeviceId,
        block: suspend (Long) -> Unit,
    ) {
        controlMutex.withLock {
            try {
                block(getDevicePointer(deviceId))
            } catch (e: Exception) {
                Log.w(TAG, "Command failed for ${deviceId.value}, trying fresh PASE", e)
                cachedPointers.remove(deviceId.value.toLong())
                delay(RETRY_DELAY_MS)
                block(getFreshPointer(deviceId))
            }
        }
    }

    private fun getDevicePointer(deviceId: DeviceId): Long {
        val nodeId = deviceId.value.toLong()
        return cachedPointers[nodeId]
            ?: throw IllegalStateException("No cached pointer for device $nodeId")
    }

    /**
     * Fuerza sesión PASE nueva con nodeId temporal.
     * Solo funciona si el simulador tiene la ventana de comisionamiento abierta.
     */
    private suspend fun getFreshPointer(deviceId: DeviceId): Long {
        val nodeId = deviceId.value.toLong()
        val conn = deviceConnections[nodeId]
            ?: throw IllegalStateException("Device $nodeId not registered")

        val tempNodeId = retryNodeIdCounter.getAndIncrement()
        Log.d(TAG, "Fresh PASE: tempNode=$tempNodeId for device ${deviceId.value} (port ${conn.port})")

        establishPase(tempNodeId, conn)
        val pointer = chipController.getDeviceBeingCommissionedPointer(tempNodeId)
        cachedPointers[nodeId] = pointer
        return pointer
    }

    private suspend fun establishPase(
        nodeId: Long,
        conn: ConnectionInfo,
    ) = withTimeout(PASE_TIMEOUT_MS) {
        suspendCancellableCoroutine { cont ->
            chipController.setCompletionListener(object : ChipDeviceController.CompletionListener {
                override fun onPairingComplete(code: Int) {
                    if (code == 0) {
                        Log.d(TAG, "PASE OK: node $nodeId")
                        if (cont.isActive) cont.resume(Unit)
                    } else {
                        Log.e(TAG, "PASE failed: node $nodeId, code=$code")
                        if (cont.isActive) {
                            cont.resumeWithException(
                                RuntimeException("PASE failed code=$code node=$nodeId"),
                            )
                        }
                    }
                }

                override fun onError(error: Throwable?) {
                    Log.e(TAG, "PASE error: node $nodeId", error)
                    if (cont.isActive) {
                        cont.resumeWithException(
                            error ?: RuntimeException("PASE failed node=$nodeId"),
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
        const val PASE_TIMEOUT_MS = 5_000L
        const val RETRY_DELAY_MS = 500L
        const val RETRY_NODE_ID_BASE = 100_000L
        const val CONTENT_LAUNCHER_CLUSTER_ID = 0x050AL
        const val LAUNCH_URL_COMMAND_ID = 0x01L
    }
}
