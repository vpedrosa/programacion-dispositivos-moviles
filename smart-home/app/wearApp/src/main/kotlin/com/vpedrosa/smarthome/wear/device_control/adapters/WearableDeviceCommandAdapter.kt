package com.vpedrosa.smarthome.wear.device_control.adapters

import android.content.Context
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.vpedrosa.smarthome.wear.R
import com.vpedrosa.smarthome.wear.device_control.domain.ports.ActionResult
import com.vpedrosa.smarthome.wear.device_control.domain.ports.DeviceCommandPort
import com.vpedrosa.smarthome.wear.device_control.domain.ports.DeviceListResult
import com.vpedrosa.smarthome.wear.device_control.model.WearDevice
import com.vpedrosa.smarthome.wear.device_control.model.WearDeviceAction
import com.vpedrosa.smarthome.wear.device_control.model.WearDeviceList
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume

class WearableDeviceCommandAdapter(
    private val context: Context,
) : DeviceCommandPort {

    private val json = Json { ignoreUnknownKeys = true }

    private val messageClient: MessageClient by lazy {
        Wearable.getMessageClient(context)
    }

    private val nodeClient by lazy {
        Wearable.getNodeClient(context)
    }

    override suspend fun requestDeviceList(): DeviceListResult {
        val phoneNodeId = getPhoneNodeId()
            ?: return DeviceListResult.Error(
                context.getString(R.string.wear_phone_not_connected)
            )

        val response = sendAndAwait(
            nodeId = phoneNodeId,
            sendPath = PATH_DEVICE_LIST_REQUEST,
            responsePath = PATH_DEVICE_LIST_RESPONSE,
            payload = ByteArray(0),
        ) ?: return DeviceListResult.Error(context.getString(R.string.wear_timeout))

        return try {
            val deviceList = json.decodeFromString<WearDeviceList>(response)
            DeviceListResult.Success(deviceList.devices)
        } catch (e: Exception) {
            DeviceListResult.Error(
                context.getString(R.string.wear_unknown_response, e.message ?: "")
            )
        }
    }

    override suspend fun sendToggleAction(deviceId: String): ActionResult {
        val phoneNodeId = getPhoneNodeId()
            ?: return ActionResult.Error(
                context.getString(R.string.wear_phone_not_connected)
            )

        val actionJson = json.encodeToString(
            WearDeviceAction.serializer(),
            WearDeviceAction(deviceId = deviceId, action = WearDeviceAction.TOGGLE),
        )

        val response = sendAndAwait(
            nodeId = phoneNodeId,
            sendPath = PATH_DEVICE_ACTION,
            responsePath = PATH_DEVICE_ACTION_RESULT,
            payload = actionJson.toByteArray(Charsets.UTF_8),
        ) ?: return ActionResult.Error(context.getString(R.string.wear_timeout))

        return when {
            response.startsWith("OK:") -> {
                try {
                    val device = json.decodeFromString<WearDevice>(
                        response.removePrefix("OK:")
                    )
                    ActionResult.Success(device)
                } catch (e: Exception) {
                    ActionResult.Error(e.message ?: "Parse error")
                }
            }
            response.startsWith("ERROR:") -> {
                ActionResult.Error(response.removePrefix("ERROR:"))
            }
            else -> ActionResult.Error(
                context.getString(R.string.wear_unknown_response, response)
            )
        }
    }

    private suspend fun getPhoneNodeId(): String? {
        return try {
            nodeClient.connectedNodes.await().firstOrNull()?.id
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun sendAndAwait(
        nodeId: String,
        sendPath: String,
        responsePath: String,
        payload: ByteArray,
    ): String? {
        return withTimeoutOrNull(RESPONSE_TIMEOUT_MS) {
            suspendCancellableCoroutine { continuation ->
                val listener = object : MessageClient.OnMessageReceivedListener {
                    override fun onMessageReceived(event: MessageEvent) {
                        if (event.path == responsePath) {
                            messageClient.removeListener(this)
                            if (continuation.isActive) {
                                continuation.resume(String(event.data, Charsets.UTF_8))
                            }
                        }
                    }
                }

                messageClient.addListener(listener)
                continuation.invokeOnCancellation { messageClient.removeListener(listener) }

                try {
                    messageClient.sendMessage(nodeId, sendPath, payload)
                } catch (e: Exception) {
                    messageClient.removeListener(listener)
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }
            }
        }
    }

    companion object {
        const val PATH_DEVICE_LIST_REQUEST = "/device_list_request"
        const val PATH_DEVICE_LIST_RESPONSE = "/device_list_response"
        const val PATH_DEVICE_ACTION = "/device_action"
        const val PATH_DEVICE_ACTION_RESULT = "/device_action_result"
        private const val RESPONSE_TIMEOUT_MS = 10_000L
    }
}
