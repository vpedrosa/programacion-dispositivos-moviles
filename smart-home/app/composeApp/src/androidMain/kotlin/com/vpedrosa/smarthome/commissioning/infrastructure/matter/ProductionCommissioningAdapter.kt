package com.vpedrosa.smarthome.commissioning.infrastructure.matter

import android.util.Log
import chip.devicecontroller.ChipDeviceController
import com.vpedrosa.smarthome.shared.domain.model.Blind
import com.vpedrosa.smarthome.shared.domain.model.ContactSensor
import com.vpedrosa.smarthome.shared.domain.model.Device
import com.vpedrosa.smarthome.shared.domain.model.DeviceId
import com.vpedrosa.smarthome.shared.domain.model.DeviceType
import com.vpedrosa.smarthome.commissioning.domain.model.DiscoveredDevice
import com.vpedrosa.smarthome.shared.domain.model.Light
import com.vpedrosa.smarthome.shared.domain.model.Lock
import com.vpedrosa.smarthome.shared.domain.model.SmartTv
import com.vpedrosa.smarthome.shared.domain.model.SmokeSensor
import com.vpedrosa.smarthome.shared.domain.model.Switch
import com.vpedrosa.smarthome.shared.domain.model.TemperatureSensor
import com.vpedrosa.smarthome.shared.domain.model.Thermostat
import com.vpedrosa.smarthome.shared.domain.model.WaterLeakSensor
import com.vpedrosa.smarthome.shared.domain.model.Color as DeviceColor
import com.vpedrosa.smarthome.commissioning.domain.CommissioningPort
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Production commissioning adapter that uses the full Matter commissioning
 * flow: BLE for initial pairing, then mDNS/CASE for operational discovery.
 *
 * Uses [ChipDeviceController.pairDevice] which handles:
 * 1. PASE session establishment via BLE
 * 2. Certificate provisioning (NOC)
 * 3. Network commissioning (Wi-Fi credentials)
 * 4. Operational discovery via mDNS (CASE session)
 *
 * This adapter should only be used on physical devices, not emulators.
 */
class ProductionCommissioningAdapter(
    private val chipController: ChipDeviceController,
) : CommissioningPort {

    override suspend fun commission(device: DiscoveredDevice): Result<Device> = runCatching {
        val nodeId = generateNodeId(device)

        commissionDevice(nodeId, device)

        createDomainDevice(device, nodeId)
    }

    private suspend fun commissionDevice(
        nodeId: Long,
        device: DiscoveredDevice,
    ) = withTimeout(COMMISSIONING_TIMEOUT_MS) {
        suspendCancellableCoroutine { cont ->
            chipController.setCompletionListener(object : ChipDeviceController.CompletionListener {
                override fun onCommissioningComplete(completedNodeId: Long, errorCode: Int) {
                    if (errorCode == 0) {
                        Log.d(TAG, "Commissioning complete: ${device.name} (node=$completedNodeId)")
                        if (cont.isActive) cont.resume(Unit)
                    } else {
                        Log.e(TAG, "Commissioning failed: ${device.name}, error=$errorCode")
                        if (cont.isActive) {
                            cont.resumeWithException(
                                RuntimeException("Commissioning failed: error=$errorCode for ${device.name}"),
                            )
                        }
                    }
                }

                override fun onPairingComplete(code: Int) {
                    Log.d(TAG, "Pairing complete: ${device.name}, code=$code")
                }

                override fun onError(error: Throwable?) {
                    Log.e(TAG, "Commissioning error: ${device.name}", error)
                    if (cont.isActive) {
                        cont.resumeWithException(
                            error ?: RuntimeException("Commissioning failed for ${device.name}"),
                        )
                    }
                }

                override fun onConnectDeviceComplete() {}
                override fun onStatusUpdate(status: Int) {
                    Log.d(TAG, "Commissioning status: $status for ${device.name}")
                }
                override fun onPairingDeleted(code: Int) {}
                override fun onCommissioningStatusUpdate(nodeId: Long, stage: String?, errorCode: Int) {
                    Log.d(TAG, "Commissioning stage: $stage (error=$errorCode) for ${device.name}")
                }
                override fun onNotifyChipConnectionClosed() {}
                override fun onCloseBleComplete() {}
                override fun onReadCommissioningInfo(
                    vendorId: Int, productId: Int, wifiEndpointId: Int, threadEndpointId: Int,
                ) {
                    Log.d(TAG, "Commission info: vendor=$vendorId product=$productId")
                }
                override fun onOpCSRGenerationComplete(csr: ByteArray?) {}
            })

            // Step 1: Establish PASE session via IP (same network)
            chipController.establishPaseConnection(
                nodeId,
                device.host,
                device.port,
                device.passcode,
            )
            // Step 2: commissionDevice handles NOC provisioning and
            // operational discovery (mDNS/CASE). This is the full
            // Matter commissioning flow beyond what the emulator adapter does.
            chipController.commissionDevice(nodeId, null)
        }
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
        const val TAG = "ProductionCommission"
        const val COMMISSIONING_TIMEOUT_MS = 60_000L
    }
}
