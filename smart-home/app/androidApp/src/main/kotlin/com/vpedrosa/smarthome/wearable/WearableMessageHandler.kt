package com.vpedrosa.smarthome.wearable

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.vpedrosa.smarthome.device.application.DeviceWithRoom
import com.vpedrosa.smarthome.device.application.GetAllDevicesWithRoomUseCase
import com.vpedrosa.smarthome.device.application.ToggleDeviceUseCase
import com.vpedrosa.smarthome.shared.domain.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Handles device control messages from the Wear OS app.
 * Used both by [WearableMessageListenerService] (background) and
 * registered directly as a [MessageClient] listener (foreground).
 */
class WearableMessageHandler(
    private val context: Context,
    private val getAllDevicesWithRoom: GetAllDevicesWithRoomUseCase,
    private val toggleDevice: ToggleDeviceUseCase,
) : MessageClient.OnMessageReceivedListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        val sourceNodeId = messageEvent.sourceNodeId

        Log.d(TAG, "onMessageReceived path=${messageEvent.path} from=$sourceNodeId")

        when (messageEvent.path) {
            PATH_DEVICE_LIST_REQUEST -> handleDeviceListRequest(sourceNodeId)
            PATH_DEVICE_ACTION -> handleDeviceAction(sourceNodeId, messageEvent.data)
            else -> Log.w(TAG, "Unknown path: ${messageEvent.path}")
        }
    }

    private fun handleDeviceListRequest(sourceNodeId: String) {
        scope.launch {
            val responsePayload = try {
                val devicesWithRoom = getAllDevicesWithRoom()

                val wearDevices = devicesWithRoom.map { it.toWearDevice() }

                json.encodeToString(WearDeviceList.serializer(), WearDeviceList(wearDevices))
            } catch (e: Exception) {
                Log.e(TAG, "Error building device list: ${e.message}", e)
                json.encodeToString(WearDeviceList.serializer(), WearDeviceList(emptyList()))
            }

            sendMessage(sourceNodeId, PATH_DEVICE_LIST_RESPONSE, responsePayload)
        }
    }

    private fun handleDeviceAction(sourceNodeId: String, data: ByteArray) {
        scope.launch {
            val responsePayload = try {
                val action = json.decodeFromString<WearDeviceAction>(
                    String(data, Charsets.UTF_8)
                )
                val deviceId = DeviceId(action.deviceId)
                toggleDevice(deviceId)

                val devicesWithRoom = getAllDevicesWithRoom()
                val updated = devicesWithRoom.find { it.device.id == deviceId }
                    ?: throw IllegalStateException("Device not found after toggle")

                "OK:" + json.encodeToString(
                    WearDevice.serializer(),
                    updated.toWearDevice(),
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
            Wearable.getMessageClient(context)
                .sendMessage(nodeId, path, payload.toByteArray(Charsets.UTF_8))
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send message to watch: ${e.message}", e)
        }
    }

    companion object {
        private const val TAG = "WearableMessageHandler"
        const val PATH_DEVICE_LIST_REQUEST = "/device_list_request"
        const val PATH_DEVICE_LIST_RESPONSE = "/device_list_response"
        const val PATH_DEVICE_ACTION = "/device_action"
        const val PATH_DEVICE_ACTION_RESULT = "/device_action_result"
    }
}

// --- Serialization models (shared protocol with wearApp) ---

@Serializable
internal data class WearDeviceList(val devices: List<WearDevice>)

@Serializable
internal data class WearDeviceAction(val deviceId: String, val action: String)

@Serializable
internal data class WearDevice(
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

internal fun DeviceWithRoom.toWearDevice(): WearDevice = WearDevice(
    id = device.id.value,
    name = device.name,
    type = device.type.name,
    roomName = roomName,
    isOn = when (device) {
        is Light -> device.isOn
        is Switch -> device.isOn
        is SmartTv -> device.isOn
        else -> false
    },
    isLocked = (device as? Lock)?.isLocked ?: false,
    openingLevel = (device as? Blind)?.openingLevel ?: 0,
    currentTemperature = when (device) {
        is Thermostat -> device.currentTemperature
        is TemperatureSensor -> device.currentTemperature
        else -> 0.0
    },
    targetTemperature = (device as? Thermostat)?.targetTemperature ?: 0.0,
    isHeatingOn = (device as? Thermostat)?.isHeatingOn ?: false,
    isSmokeDetected = (device as? SmokeSensor)?.isSmokeDetected ?: false,
    isLeakDetected = (device as? WaterLeakSensor)?.isLeakDetected ?: false,
    isContactOpen = (device as? ContactSensor)?.isOpen ?: false,
)
