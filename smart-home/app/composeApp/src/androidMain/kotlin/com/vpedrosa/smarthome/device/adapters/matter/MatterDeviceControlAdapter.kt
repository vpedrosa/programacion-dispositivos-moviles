package com.vpedrosa.smarthome.device.adapters.matter

import android.util.Log
import chip.devicecontroller.ChipClusters
import chip.devicecontroller.ChipDeviceController
import chip.devicecontroller.GetConnectedDeviceCallbackJni
import java.util.Optional
import com.vpedrosa.smarthome.device.domain.DeviceId
import com.vpedrosa.smarthome.device.domain.ports.DeviceControlPort
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MatterDeviceControlAdapter(
    private val chipController: ChipDeviceController,
) : DeviceControlPort {

    override suspend fun toggleOnOff(deviceId: DeviceId, on: Boolean) {
        val pointer = getDevicePointer(deviceId)
        val cluster = ChipClusters.OnOffCluster(pointer, ENDPOINT_ID)
        suspendClusterCommand { cb ->
            if (on) cluster.on(cb) else cluster.off(cb)
        }
        Log.d(TAG, "${deviceId.value}: OnOff -> $on")
    }

    override suspend fun setLevel(deviceId: DeviceId, level: Int) {
        val pointer = getDevicePointer(deviceId)
        val cluster = ChipClusters.LevelControlCluster(pointer, ENDPOINT_ID)
        val matterLevel = (level * 254 / 100).coerceIn(0, 254)
        suspendClusterCommand { cb ->
            cluster.moveToLevel(cb, matterLevel, 0, 0, 0)
        }
        Log.d(TAG, "${deviceId.value}: Level -> $level% ($matterLevel)")
    }

    override suspend fun lockDoor(deviceId: DeviceId, lock: Boolean) {
        val pointer = getDevicePointer(deviceId)
        val cluster = ChipClusters.DoorLockCluster(pointer, ENDPOINT_ID)
        val noPin = Optional.empty<ByteArray>()
        suspendClusterCommand { cb ->
            if (lock) cluster.lockDoor(cb, noPin, TIMED_INVOKE_TIMEOUT)
            else cluster.unlockDoor(cb, noPin, TIMED_INVOKE_TIMEOUT)
        }
        Log.d(TAG, "${deviceId.value}: Lock -> $lock")
    }

    override suspend fun setThermostatSetpoint(deviceId: DeviceId, temperatureCelsius: Double) {
        val pointer = getDevicePointer(deviceId)
        val cluster = ChipClusters.ThermostatCluster(pointer, ENDPOINT_ID)
        val matterTemp = (temperatureCelsius * 100).toInt()
        suspendClusterCommand { cb ->
            cluster.writeOccupiedHeatingSetpointAttribute(cb, matterTemp)
        }
        Log.d(TAG, "${deviceId.value}: Setpoint -> ${temperatureCelsius}°C")
    }

    private suspend fun getDevicePointer(deviceId: DeviceId): Long {
        val nodeId = deviceId.value.toLong()
        return suspendCancellableCoroutine { cont ->
            chipController.getConnectedDevicePointer(
                nodeId,
                object : GetConnectedDeviceCallbackJni.GetConnectedDeviceCallback {
                    override fun onDeviceConnected(devicePointer: Long) {
                        cont.resume(devicePointer)
                    }

                    override fun onConnectionFailure(nodeId: Long, error: Exception) {
                        Log.e(TAG, "Connection failed for node $nodeId", error)
                        cont.resumeWithException(error)
                    }
                },
            )
        }
    }

    private suspend fun suspendClusterCommand(
        block: (ChipClusters.DefaultClusterCallback) -> Unit,
    ) = suspendCancellableCoroutine { cont ->
        block(object : ChipClusters.DefaultClusterCallback {
            override fun onSuccess() {
                cont.resume(Unit)
            }

            override fun onError(error: Exception) {
                Log.e(TAG, "Cluster command error", error)
                cont.resumeWithException(error)
            }
        })
    }

    private companion object {
        const val TAG = "MatterDeviceControl"
        const val ENDPOINT_ID = 1
        const val TIMED_INVOKE_TIMEOUT = 10_000
    }
}
