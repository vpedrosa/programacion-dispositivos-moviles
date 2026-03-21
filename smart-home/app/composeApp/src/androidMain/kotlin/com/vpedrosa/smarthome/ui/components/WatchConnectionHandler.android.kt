package com.vpedrosa.smarthome.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

@Composable
actual fun WatchConnectionHandler(
    content: @Composable (isConnected: Boolean, watchName: String?) -> Unit,
) {
    val context = LocalContext.current
    var isConnected by remember { mutableStateOf(false) }
    var watchName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            try {
                val nodes = Wearable.getNodeClient(context).connectedNodes.await()
                val node = nodes.firstOrNull()
                isConnected = node != null
                watchName = node?.displayName
            } catch (_: Exception) {
                isConnected = false
                watchName = null
            }
            delay(POLL_INTERVAL_MS)
        }
    }

    content(isConnected, watchName)
}

private const val POLL_INTERVAL_MS = 5_000L
