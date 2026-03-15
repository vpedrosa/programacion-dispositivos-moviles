package com.vpedrosa.smarthome.wearable

import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.vpedrosa.smarthome.device.domain.usecases.ExecuteVoiceCommandUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ParseVoiceCommandUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * Listens for voice command messages from the Wear OS app via the
 * Wearable Data Layer API. Parses and executes the command using
 * existing use cases, then sends the result back to the watch.
 *
 * Message protocol:
 * - Incoming: path="/voice_command", payload=command text (UTF-8)
 * - Outgoing: path="/voice_command_result", payload="OK:<message>" or "ERROR:<message>"
 */
class WearableMessageListenerService : WearableListenerService() {

    private val parseVoiceCommand: ParseVoiceCommandUseCase by inject()
    private val executeVoiceCommand: ExecuteVoiceCommandUseCase by inject()

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path != COMMAND_PATH) return

        val commandText = String(messageEvent.data, Charsets.UTF_8)
        val sourceNodeId = messageEvent.sourceNodeId

        serviceScope.launch {
            val parsed = parseVoiceCommand(commandText)
            val result = executeVoiceCommand(parsed)

            val responsePayload = if (result.success) {
                "OK:${result.message}"
            } else {
                "ERROR:${result.message}"
            }

            try {
                Wearable.getMessageClient(this@WearableMessageListenerService)
                    .sendMessage(
                        sourceNodeId,
                        RESULT_PATH,
                        responsePayload.toByteArray(Charsets.UTF_8),
                    )
            } catch (e: Exception) {
                // Best effort: if we can't send the result back, the watch
                // will time out and show an error.
                android.util.Log.e(
                    TAG,
                    "Failed to send result back to watch: ${e.message}",
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        private const val TAG = "WearableMessageListener"
        private const val COMMAND_PATH = "/voice_command"
        private const val RESULT_PATH = "/voice_command_result"
    }
}
