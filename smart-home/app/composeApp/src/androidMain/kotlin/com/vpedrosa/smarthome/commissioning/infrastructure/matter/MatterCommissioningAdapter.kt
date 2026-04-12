package com.vpedrosa.smarthome.commissioning.infrastructure.matter

import android.util.Log
import chip.devicecontroller.ChipDeviceController
import com.vpedrosa.smarthome.device.domain.model.Blind
import com.vpedrosa.smarthome.device.domain.model.ContactSensor
import com.vpedrosa.smarthome.device.domain.model.Device
import com.vpedrosa.smarthome.device.domain.model.DeviceId
import com.vpedrosa.smarthome.device.domain.model.DeviceType
import com.vpedrosa.smarthome.commissioning.domain.model.DiscoveredDevice
import com.vpedrosa.smarthome.device.domain.model.Light
import com.vpedrosa.smarthome.device.domain.model.Lock
import com.vpedrosa.smarthome.device.domain.model.SmartTv
import com.vpedrosa.smarthome.device.domain.model.SmokeSensor
import com.vpedrosa.smarthome.device.domain.model.Switch
import com.vpedrosa.smarthome.device.domain.model.TemperatureSensor
import com.vpedrosa.smarthome.device.domain.model.Thermostat
import com.vpedrosa.smarthome.device.domain.model.WaterLeakSensor
import com.vpedrosa.smarthome.device.domain.model.Color as DeviceColor
import com.vpedrosa.smarthome.commissioning.domain.CommissioningPort
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MatterCommissioningAdapter(
    private val chipController: ChipDeviceController,
) : CommissioningPort {

    /**
     * "Comisiona" un dispositivo estableciendo PASE para verificar conectividad.
     *
     * No llama a commissionDevice() porque FindOperational (mDNS/CASE) no
     * funciona en el emulador Android. El control se hace vía PASE directo
     * en MatterDeviceControlAdapter.
     */
    override suspend fun commission(device: DiscoveredDevice): Result<Device> = runCatching {
        val nodeId = generateNodeId(device)

        establishPaseConnection(nodeId, device)

        createDomainDevice(device, nodeId)
    }

    private suspend fun establishPaseConnection(
        nodeId: Long,
        device: DiscoveredDevice,
    ) = suspendCancellableCoroutine { cont ->
        chipController.setCompletionListener(object : ChipDeviceController.CompletionListener {
            override fun onPairingComplete(code: Int) {
                if (code == 0) {
                    Log.d(TAG, "PASE connection established: ${device.name}")
                    if (cont.isActive) cont.resume(Unit)
                } else {
                    Log.e(TAG, "PASE failed with code $code: ${device.name}")
                    if (cont.isActive) {
                        cont.resumeWithException(
                            RuntimeException("PASE failed with code $code for ${device.name}"),
                        )
                    }
                }
            }

            override fun onError(error: Throwable?) {
                Log.e(TAG, "PASE connection failed: ${device.name}", error)
                if (cont.isActive) {
                    cont.resumeWithException(
                        error ?: RuntimeException("PASE failed for ${device.name}"),
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
                vendorId: Int,
                productId: Int,
                wifiEndpointId: Int,
                threadEndpointId: Int,
            ) {}
            override fun onOpCSRGenerationComplete(csr: ByteArray?) {}
        })

        chipController.establishPaseConnection(
            nodeId,
            device.host,
            device.port,
            device.passcode,
        )
    }

    private fun createDomainDevice(device: DiscoveredDevice, nodeId: Long): Device {
        val id = DeviceId(nodeId.toString())
        return when (device.type) {
            DeviceType.LIGHT -> Light(id, device.name, null, isOn = false, color = DeviceColor.WHITE, brightness = 100)
            DeviceType.SWITCH -> Switch(id, device.name, null, isOn = false)
            DeviceType.LOCK -> Lock(id, device.name, null, isLocked = true)
            DeviceType.BLIND -> Blind(id, device.name, null, openingLevel = 0)
            DeviceType.CONTACT_SENSOR -> ContactSensor(id, device.name, null, isOpen = false)
            DeviceType.SMART_TV -> SmartTv(id, device.name, null, isOn = false)
            DeviceType.SMOKE_SENSOR -> SmokeSensor(id, device.name, null, isSmokeDetected = false)
            DeviceType.WATER_LEAK_SENSOR -> WaterLeakSensor(id, device.name, null, isLeakDetected = false)
            DeviceType.TEMPERATURE_SENSOR -> TemperatureSensor(id, device.name, null, currentTemperature = 21.0)
            DeviceType.THERMOSTAT -> Thermostat(id, device.name, null, currentTemperature = 21.0, targetTemperature = 22.0, isHeatingOn = false)
        }
    }

    private fun generateNodeId(device: DiscoveredDevice): Long =
        device.serialNumber.removePrefix("SIM-").toLongOrNull() ?: device.port.toLong()

    private companion object {
        const val TAG = "MatterCommissioning"
    }
}
