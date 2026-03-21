package com.vpedrosa.smarthome.wear.voice_control.adapters

import android.content.Context
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.vpedrosa.smarthome.wear.R
import com.vpedrosa.smarthome.wear.voice_control.domain.ports.VoiceCommandPort
import com.vpedrosa.smarthome.wear.voice_control.domain.ports.VoiceCommandResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Driven adapter that sends voice command text to the phone app via
 * the Wearable Data Layer MessageClient and waits for the response.
 *
 * Protocol:
 * - Send: path="/voice_command", payload=command text (UTF-8)
 * - Receive: path="/voice_command_result", payload="OK:<message>" or "ERROR:<message>"
 */
class WearableVoiceCommandAdapter(
    private val context: Context,
) : VoiceCommandPort {

    private val messageClient: MessageClient by lazy {
        Wearable.getMessageClient(context)
    }

    private val nodeClient by lazy {
        Wearable.getNodeClient(context)
    }

    override suspend fun sendCommand(transcribedText: String): VoiceCommandResult {
        // Find the connected phone node
        val nodes = try {
            nodeClient.connectedNodes.await()
        } catch (e: Exception) {
            return VoiceCommandResult.Error(
                context.getString(R.string.wear_phone_connect_error, e.message ?: "")
            )
        }

        val phoneNode = nodes.firstOrNull()
            ?: return VoiceCommandResult.Error(
                context.getString(R.string.wear_phone_not_connected)
            )

        // Register a listener for the response before sending
        val response = withTimeoutOrNull(RESPONSE_TIMEOUT_MS) {
            suspendCancellableCoroutine { continuation ->
                val listener = object : MessageClient.OnMessageReceivedListener {
                    override fun onMessageReceived(messageEvent: MessageEvent) {
                        if (messageEvent.path == RESULT_PATH) {
                            messageClient.removeListener(this)
                            val payload = String(messageEvent.data, Charsets.UTF_8)
                            val result = parseResultPayload(payload)
                            if (continuation.isActive) {
                                continuation.resume(result)
                            }
                        }
                    }
                }

                messageClient.addListener(listener)

                continuation.invokeOnCancellation {
                    messageClient.removeListener(listener)
                }

                // Send the command
                try {
                    messageClient.sendMessage(
                        phoneNode.id,
                        COMMAND_PATH,
                        transcribedText.toByteArray(Charsets.UTF_8),
                    )
                } catch (e: Exception) {
                    messageClient.removeListener(listener)
                    if (continuation.isActive) {
                        continuation.resume(
                            VoiceCommandResult.Error(
                                context.getString(R.string.wear_send_error, e.message ?: "")
                            )
                        )
                    }
                }
            }
        }

        return response ?: VoiceCommandResult.Error(
            context.getString(R.string.wear_timeout)
        )
    }

    private fun parseResultPayload(payload: String): VoiceCommandResult {
        return when {
            payload.startsWith("OK:") -> {
                VoiceCommandResult.Success(payload.removePrefix("OK:"))
            }
            payload.startsWith("ERROR:") -> {
                VoiceCommandResult.Error(payload.removePrefix("ERROR:"))
            }
            else -> {
                VoiceCommandResult.Error(
                    context.getString(R.string.wear_unknown_response, payload)
                )
            }
        }
    }

    companion object {
        const val COMMAND_PATH = "/voice_command"
        const val RESULT_PATH = "/voice_command_result"
        private const val RESPONSE_TIMEOUT_MS = 10_000L
    }
}
