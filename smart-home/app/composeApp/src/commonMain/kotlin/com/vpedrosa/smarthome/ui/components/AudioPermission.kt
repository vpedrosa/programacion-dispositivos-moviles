package com.vpedrosa.smarthome.ui.components

import androidx.compose.runtime.Composable

/**
 * Platform-specific composable that provides audio recording permission state
 * and a mechanism to request it.
 *
 * @param onPermissionResult called with the current or updated permission state.
 *        `true` if RECORD_AUDIO is granted, `false` otherwise.
 * @param content composable content that receives:
 *        - `hasPermission`: whether RECORD_AUDIO is currently granted
 *        - `requestPermission`: callback to trigger the permission request dialog
 */
@Composable
expect fun AudioPermissionHandler(
    onPermissionResult: (Boolean) -> Unit,
    content: @Composable (hasPermission: Boolean, requestPermission: () -> Unit) -> Unit,
)
