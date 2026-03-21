package com.vpedrosa.smarthome.ui.components

import androidx.compose.runtime.Composable

/**
 * Platform-specific composable that checks whether a Wear OS watch
 * is connected via the Wearable Data Layer API.
 *
 * @param content composable content that receives:
 *        - `isConnected`: whether a watch node is currently reachable
 *        - `watchName`: display name of the connected watch, or null
 */
@Composable
expect fun WatchConnectionHandler(
    content: @Composable (isConnected: Boolean, watchName: String?) -> Unit,
)
