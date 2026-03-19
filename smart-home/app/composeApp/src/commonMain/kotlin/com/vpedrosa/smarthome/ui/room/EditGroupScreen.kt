package com.vpedrosa.smarthome.ui.room

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vpedrosa.smarthome.ui.components.PhotoPicker
import com.vpedrosa.smarthome.ui.components.UriImage
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import smarthome.composeapp.generated.resources.Res
import smarthome.composeapp.generated.resources.action_cancel
import smarthome.composeapp.generated.resources.edit_group_devices_section
import smarthome.composeapp.generated.resources.edit_group_name_label
import smarthome.composeapp.generated.resources.edit_group_name_placeholder
import smarthome.composeapp.generated.resources.edit_group_new_title
import smarthome.composeapp.generated.resources.edit_group_no_devices
import smarthome.composeapp.generated.resources.edit_group_photo_section
import smarthome.composeapp.generated.resources.edit_group_photo_tap
import smarthome.composeapp.generated.resources.edit_group_save
import smarthome.composeapp.generated.resources.a11y_navigate_back
import smarthome.composeapp.generated.resources.a11y_photo_placeholder
import smarthome.composeapp.generated.resources.title_edit_group

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditGroupScreen(
    roomId: String? = null,
    onNavigateBack: () -> Unit = {},
    viewModel: EditGroupViewModel = koinViewModel { parametersOf(roomId) },
) {
    val state by viewModel.uiState.collectAsState()
    val allDevices by viewModel.allDevices.collectAsState()
    var showPhotoPicker by remember { mutableStateOf(false) }

    PhotoPicker(
        showPicker = showPhotoPicker,
        hasExistingPhoto = state.photoUri != null,
        onPhotoSelected = { uri ->
            viewModel.setPhotoUri(uri)
        },
        onPhotoRemoved = {
            viewModel.setPhotoUri(null)
        },
        onDismiss = { showPhotoPicker = false },
    )

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onNavigateBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        TopAppBar(
            title = {
                Text(
                    text = if (state.isEditing) {
                        stringResource(Res.string.title_edit_group)
                    } else {
                        stringResource(Res.string.edit_group_new_title)
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(Res.string.a11y_navigate_back),
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground,
                navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
            ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            // --- Group Name ---
            Text(
                text = stringResource(Res.string.edit_group_name_label),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.setName(it) },
                placeholder = {
                    Text(text = stringResource(Res.string.edit_group_name_placeholder))
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.secondary,
                    cursorColor = MaterialTheme.colorScheme.primary,
                ),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Photography Section ---
            Text(
                text = stringResource(Res.string.edit_group_photo_section),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(8.dp))
            PhotoPlaceholder(
                photoUri = state.photoUri,
                onPhotoCaptureRequest = {
                    showPhotoPicker = true
                },
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Device Assignment ---
            Text(
                text = stringResource(Res.string.edit_group_devices_section),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (allDevices.isEmpty()) {
                Text(
                    text = stringResource(Res.string.edit_group_no_devices),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    allDevices.forEach { device ->
                        val isSelected = device.id in state.selectedDeviceIds
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.toggleDevice(device.id) },
                            label = {
                                Text(
                                    text = device.name,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                labelColor = MaterialTheme.colorScheme.onBackground,
                            ),
                            shape = RoundedCornerShape(20.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- Save Button ---
            Button(
                onClick = { viewModel.save() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = state.name.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f),
                ),
            ) {
                Text(
                    text = stringResource(Res.string.edit_group_save),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Cancel Button ---
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onBackground,
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.secondary,
                ),
            ) {
                Text(
                    text = stringResource(Res.string.action_cancel),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PhotoPlaceholder(
    photoUri: String?,
    onPhotoCaptureRequest: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(onClick = onPhotoCaptureRequest),
        contentAlignment = Alignment.Center,
    ) {
        if (photoUri != null) {
            UriImage(
                uri = photoUri,
                contentDescription = stringResource(Res.string.a11y_photo_placeholder),
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = stringResource(Res.string.a11y_photo_placeholder),
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.edit_group_photo_tap),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                )
            }
        }
    }
}
