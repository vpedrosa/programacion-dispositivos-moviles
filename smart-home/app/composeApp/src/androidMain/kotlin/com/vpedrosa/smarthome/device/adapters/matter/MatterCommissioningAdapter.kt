package com.vpedrosa.smarthome.device.adapters.matter

import android.content.Context
import android.util.Log
import chip.devicecontroller.ChipDeviceController
import chip.devicecontroller.ControllerParams
import chip.platform.AndroidBleManager
import chip.platform.AndroidChipPlatform
import chip.platform.ChipMdnsCallbackImpl
import chip.platform.DiagnosticDataProviderImpl
import chip.platform.NsdManagerServiceBrowser
import chip.platform.NsdManagerServiceResolver
import chip.platform.PreferencesConfigurationManager
import chip.platform.PreferencesKeyValueStoreManager
import com.vpedrosa.smarthome.device.domain.Blind
import com.vpedrosa.smarthome.device.domain.ContactSensor
import com.vpedrosa.smarthome.device.domain.Device
import com.vpedrosa.smarthome.device.domain.DeviceId
import com.vpedrosa.smarthome.device.domain.DeviceType
import com.vpedrosa.smarthome.device.domain.DiscoveredDevice
import com.vpedrosa.smarthome.device.domain.Light
import com.vpedrosa.smarthome.device.domain.Lock
import com.vpedrosa.smarthome.device.domain.SmartTv
import com.vpedrosa.smarthome.device.domain.SmokeSensor
import com.vpedrosa.smarthome.device.domain.Switch
import com.vpedrosa.smarthome.device.domain.TemperatureSensor
import com.vpedrosa.smarthome.device.domain.Thermostat
import com.vpedrosa.smarthome.device.domain.WaterLeakSensor
import com.vpedrosa.smarthome.device.domain.Color as DeviceColor
import com.vpedrosa.smarthome.device.domain.ports.CommissioningPort
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Adaptador de commissioning que usa CHIPDeviceController del Matter SDK.
 *
 * Realiza PASE over IP (sin BLE) contra dispositivos Matter en la red local.
 * Tras el commissioning, crea el modelo de dominio Device correspondiente.
 */
class MatterCommissioningAdapter(
    context: Context,
) : CommissioningPort {

    private val chipController: ChipDeviceController

    init {
        System.loadLibrary("CHIPController")

        AndroidChipPlatform(
            AndroidBleManager(),
            PreferencesKeyValueStoreManager(context),
            PreferencesConfigurationManager(context),
            NsdManagerServiceResolver(context),
            NsdManagerServiceBrowser(context),
            ChipMdnsCallbackImpl(),
            DiagnosticDataProviderImpl(context),
        )

        chipController = ChipDeviceController(
            ControllerParams.newBuilder()
                .setUdpListenPort(0)
                .setControllerVendorId(VENDOR_ID)
                .build(),
        )
    }

    override suspend fun commission(device: DiscoveredDevice): Result<Device> = runCatching {
        val nodeId = generateNodeId(device)

        establishPaseConnection(nodeId, device)
        commissionDevice(nodeId)

        createDomainDevice(device, nodeId)
    }

    private suspend fun establishPaseConnection(
        nodeId: Long,
        device: DiscoveredDevice,
    ) = suspendCancellableCoroutine { cont ->
        chipController.setCompletionListener(object : ChipDeviceController.CompletionListener {
            override fun onConnectDeviceComplete() {
                Log.d(TAG, "PASE connection established: ${device.name}")
                cont.resume(Unit)
            }

            override fun onError(error: Throwable?) {
                Log.e(TAG, "PASE connection failed: ${device.name}", error)
                cont.resume(Unit) // Will fail on commission step
            }

            override fun onStatusUpdate(status: Int) {}
            override fun onPairingComplete(code: Int) {}
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

    private suspend fun commissionDevice(nodeId: Long) = suspendCancellableCoroutine { cont ->
        chipController.setCompletionListener(object : ChipDeviceController.CompletionListener {
            override fun onCommissioningComplete(completedNodeId: Long, errorCode: Int) {
                if (errorCode == 0) {
                    Log.d(TAG, "Commissioning complete: node $completedNodeId")
                } else {
                    Log.e(TAG, "Commissioning error: $errorCode for node $completedNodeId")
                }
                cont.resume(errorCode)
            }

            override fun onError(error: Throwable?) {
                Log.e(TAG, "Commissioning error", error)
                cont.resume(-1)
            }

            override fun onConnectDeviceComplete() {}
            override fun onStatusUpdate(status: Int) {}
            override fun onPairingComplete(code: Int) {}
            override fun onPairingDeleted(code: Int) {}
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

        chipController.commissionDevice(nodeId, null)
    }

    private fun createDomainDevice(device: DiscoveredDevice, nodeId: Long): Device {
        val id = DeviceId(nodeId.toString())
        return when (device.type) {
            DeviceType.LIGHT -> Light(id, device.name, null, isOn = false, color = DeviceColor.WHITE, brightness = 100)
            DeviceType.SWITCH -> Switch(id, device.name, null, isOn = false)
            DeviceType.LOCK -> Lock(id, device.name, null, isLocked = true)
            DeviceType.BLIND -> Blind(id, device.name, null, openingLevel = 0)
            DeviceType.CONTACT_SENSOR -> ContactSensor(id, device.name, null, isOpen = false)
            DeviceType.SMART_TV -> SmartTv(id, device.name, null, isOn = false, isCasting = false)
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
        const val VENDOR_ID = 0xFFF1
    }
}
