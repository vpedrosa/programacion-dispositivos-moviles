package com.vpedrosa.smarthome.ui.components

import androidx.compose.runtime.Composable

/**
 * Platform-specific photo picker that shows a bottom sheet / dialog
 * allowing the user to take a photo with the camera or pick one from the gallery.
 *
 * @param showPicker whether the picker UI should be visible.
 * @param hasExistingPhoto whether a photo is already set (to offer "Remove" option).
 * @param onPhotoSelected called with the URI string of the selected/captured photo.
 * @param onPhotoRemoved called when the user chooses to remove the existing photo.
 * @param onDismiss called when the picker is dismissed without selection.
 */
@Composable
expect fun PhotoPicker(
    showPicker: Boolean,
    hasExistingPhoto: Boolean,
    onPhotoSelected: (String) -> Unit,
    onPhotoRemoved: () -> Unit,
    onDismiss: () -> Unit,
)
