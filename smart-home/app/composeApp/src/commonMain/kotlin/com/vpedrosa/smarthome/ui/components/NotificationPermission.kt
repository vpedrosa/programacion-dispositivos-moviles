package com.vpedrosa.smarthome.ui.components

import androidx.compose.runtime.Composable

/**
 * Platform-specific composable that provides notification permission state
 * and a mechanism to request it.
 *
 * @param onPermissionResult called with the current or updated permission state.
 *        `true` if POST_NOTIFICATIONS is granted (or not needed on API < 33).
 * @param content composable content that receives:
 *        - `hasPermission`: whether notifications are currently allowed
 *        - `requestPermission`: callback to trigger the permission request or open settings
 */
@Composable
expect fun NotificationPermissionHandler(
    onPermissionResult: (Boolean) -> Unit,
    content: @Composable (hasPermission: Boolean, requestPermission: () -> Unit) -> Unit,
)
