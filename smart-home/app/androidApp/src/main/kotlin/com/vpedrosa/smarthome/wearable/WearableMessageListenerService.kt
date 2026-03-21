package com.vpedrosa.smarthome.wearable

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.vpedrosa.smarthome.device.application.ToggleDeviceUseCase
import com.vpedrosa.smarthome.shared.domain.DeviceRepository
import com.vpedrosa.smarthome.shared.domain.RoomRepository
import com.vpedrosa.smarthome.shared.domain.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject

/**
 * Listens for device control messages from the Wear OS app via the
 * Wearable Data Layer API.
 *
 * Protocol:
 * - /device_list_request  → responds with /device_list_response (JSON device list)
 * - /device_action        → responds with /device_action_result (OK:device or ERROR:msg)
 */
class WearableMessageListenerService : WearableListenerService() {

    private val deviceRepository: DeviceRepository by inject()
    private val roomRepository: RoomRepository by inject()
    private val toggleDevice: ToggleDeviceUseCase by inject()

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        val sourceNodeId = messageEvent.sourceNodeId

        when (messageEvent.path) {
            PATH_DEVICE_LIST_REQUEST -> handleDeviceListRequest(sourceNodeId)
            PATH_DEVICE_ACTION -> handleDeviceAction(sourceNodeId, messageEvent.data)
        }
    }

    private fun handleDeviceListRequest(sourceNodeId: String) {
        serviceScope.launch {
            val responsePayload = try {
                val devices = deviceRepository.observeAllDevices().first()
                val rooms = roomRepository.observeAllRooms().first()
                val roomMap = rooms.associateBy { it.id }

                val wearDevices = devices.map { device ->
                    device.toWearDevice(roomMap[device.roomId]?.name)
                }

                json.encodeToString(WearDeviceList.serializer(), WearDeviceList(wearDevices))
            } catch (e: Exception) {
                Log.e(TAG, "Error building device list: ${e.message}", e)
                json.encodeToString(WearDeviceList.serializer(), WearDeviceList(emptyList()))
            }

            sendMessage(sourceNodeId, PATH_DEVICE_LIST_RESPONSE, responsePayload)
        }
    }

    private fun handleDeviceAction(sourceNodeId: String, data: ByteArray) {
        serviceScope.launch {
            val responsePayload = try {
                val action = json.decodeFromString<WearDeviceAction>(
                    String(data, Charsets.UTF_8)
                )
                val deviceId = DeviceId(action.deviceId)
                toggleDevice(deviceId)

                val updated = deviceRepository.observeDevice(deviceId).first()
                    ?: throw IllegalStateException("Device not found after toggle")

                val rooms = roomRepository.observeAllRooms().first()
                val roomName = rooms.find { it.id == updated.roomId }?.name

                "OK:" + json.encodeToString(
                    WearDevice.serializer(),
                    updated.toWearDevice(roomName),
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error executing action: ${e.message}", e)
                "ERROR:${e.message ?: "Unknown error"}"
            }

            sendMessage(sourceNodeId, PATH_DEVICE_ACTION_RESULT, responsePayload)
        }
    }

    private suspend fun sendMessage(nodeId: String, path: String, payload: String) {
        try {
            Wearable.getMessageClient(this@WearableMessageListenerService)
                .sendMessage(nodeId, path, payload.toByteArray(Charsets.UTF_8))
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send message to watch: ${e.message}", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        private const val TAG = "WearableMessageListener"
        private const val PATH_DEVICE_LIST_REQUEST = "/device_list_request"
        private const val PATH_DEVICE_LIST_RESPONSE = "/device_list_response"
        private const val PATH_DEVICE_ACTION = "/device_action"
        private const val PATH_DEVICE_ACTION_RESULT = "/device_action_result"
    }
}

// --- Serialization models (shared protocol with wearApp) ---

@Serializable
private data class WearDeviceList(val devices: List<WearDevice>)

@Serializable
private data class WearDeviceAction(val deviceId: String, val action: String)

@Serializable
private data class WearDevice(
    val id: String,
    val name: String,
    val type: String,
    val roomName: String?,
    val isOn: Boolean = false,
    val isLocked: Boolean = false,
    val openingLevel: Int = 0,
    val currentTemperature: Double = 0.0,
    val targetTemperature: Double = 0.0,
    val isHeatingOn: Boolean = false,
    val isSmokeDetected: Boolean = false,
    val isLeakDetected: Boolean = false,
    val isContactOpen: Boolean = false,
)

private fun Device.toWearDevice(roomName: String?): WearDevice = WearDevice(
    id = id.value,
    name = name,
    type = type.name,
    roomName = roomName,
    isOn = when (this) {
        is Light -> isOn
        is Switch -> isOn
        is SmartTv -> isOn
        else -> false
    },
    isLocked = (this as? Lock)?.isLocked ?: false,
    openingLevel = (this as? Blind)?.openingLevel ?: 0,
    currentTemperature = when (this) {
        is Thermostat -> currentTemperature
        is TemperatureSensor -> currentTemperature
        else -> 0.0
    },
    targetTemperature = (this as? Thermostat)?.targetTemperature ?: 0.0,
    isHeatingOn = (this as? Thermostat)?.isHeatingOn ?: false,
    isSmokeDetected = (this as? SmokeSensor)?.isSmokeDetected ?: false,
    isLeakDetected = (this as? WaterLeakSensor)?.isLeakDetected ?: false,
    isContactOpen = (this as? ContactSensor)?.isOpen ?: false,
)
