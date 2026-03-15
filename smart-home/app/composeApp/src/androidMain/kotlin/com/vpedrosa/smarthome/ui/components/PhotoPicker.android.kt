package com.vpedrosa.smarthome.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import org.jetbrains.compose.resources.stringResource
import smarthome.composeapp.generated.resources.Res
import smarthome.composeapp.generated.resources.edit_group_photo_camera
import smarthome.composeapp.generated.resources.edit_group_photo_choose_source
import smarthome.composeapp.generated.resources.edit_group_photo_gallery
import smarthome.composeapp.generated.resources.edit_group_photo_remove
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun PhotoPicker(
    showPicker: Boolean,
    hasExistingPhoto: Boolean,
    onPhotoSelected: (String) -> Unit,
    onPhotoRemoved: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) {
            cameraUri?.let { uri ->
                onPhotoSelected(uri.toString())
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        uri?.let {
            // Copy the image to a persistent location so the URI does not expire
            val persistedUri = copyToInternalStorage(context, it)
            onPhotoSelected(persistedUri.toString())
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            val uri = createCameraImageUri(context)
            cameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    if (showPicker) {
        val sheetState = rememberModalBottomSheetState()

        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
            ) {
                Text(
                    text = stringResource(Res.string.edit_group_photo_choose_source),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Camera option
                ListItem(
                    headlineContent = {
                        Text(
                            text = stringResource(Res.string.edit_group_photo_camera),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    },
                    modifier = Modifier.clickable {
                        onDismiss()
                        launchCamera(
                            context = context,
                            onUriCreated = { uri -> cameraUri = uri },
                            cameraLauncher = { uri -> cameraLauncher.launch(uri) },
                            permissionLauncher = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                        )
                    },
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )

                // Gallery option
                ListItem(
                    headlineContent = {
                        Text(
                            text = stringResource(Res.string.edit_group_photo_gallery),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    },
                    modifier = Modifier.clickable {
                        onDismiss()
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                )

                // Remove photo option (only when a photo is already set)
                if (hasExistingPhoto) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )

                    ListItem(
                        headlineContent = {
                            Text(
                                text = stringResource(Res.string.edit_group_photo_remove),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.error,
                            )
                        },
                        modifier = Modifier.clickable {
                            onDismiss()
                            onPhotoRemoved()
                        },
                    )
                }
            }
        }
    }
}

private fun launchCamera(
    context: Context,
    onUriCreated: (Uri) -> Unit,
    cameraLauncher: (Uri) -> Unit,
    permissionLauncher: () -> Unit,
) {
    val hasPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA,
    ) == PackageManager.PERMISSION_GRANTED

    if (hasPermission) {
        val uri = createCameraImageUri(context)
        onUriCreated(uri)
        cameraLauncher(uri)
    } else {
        permissionLauncher()
    }
}

private fun createCameraImageUri(context: Context): Uri {
    val photosDir = File(context.cacheDir, "camera_photos")
    photosDir.mkdirs()
    val photoFile = File(photosDir, "photo_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        photoFile,
    )
}

private fun copyToInternalStorage(context: Context, sourceUri: Uri): Uri {
    val photosDir = File(context.filesDir, "room_photos")
    photosDir.mkdirs()
    val destFile = File(photosDir, "photo_${System.currentTimeMillis()}.jpg")

    context.contentResolver.openInputStream(sourceUri)?.use { input ->
        destFile.outputStream().use { output ->
            input.copyTo(output)
        }
    }

    return Uri.fromFile(destFile)
}
