package com.vpedrosa.smarthome.wearable

import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

/**
 * Background service that receives device control messages from
 * the Wear OS app when the phone app is not in the foreground.
 * Delegates to [WearableMessageHandler] for actual processing.
 */
class WearableMessageListenerService : WearableListenerService() {

    private val handler by lazy { WearableMessageHandler(this) }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        handler.onMessageReceived(messageEvent)
    }
}
