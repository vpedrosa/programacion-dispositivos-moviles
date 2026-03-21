package com.vpedrosa.smarthome.ui.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
actual fun NotificationPermissionHandler(
    onPermissionResult: (Boolean) -> Unit,
    content: @Composable (hasPermission: Boolean, requestPermission: () -> Unit) -> Unit,
) {
    val context = LocalContext.current

    // On API < 33, POST_NOTIFICATIONS doesn't exist — permission is always granted.
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        content(true) { onPermissionResult(true) }
        return
    }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasPermission = granted
        onPermissionResult(granted)
    }

    content(hasPermission) {
        if (!hasPermission) {
            // Try runtime request first; if already denied permanently, open app settings
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
