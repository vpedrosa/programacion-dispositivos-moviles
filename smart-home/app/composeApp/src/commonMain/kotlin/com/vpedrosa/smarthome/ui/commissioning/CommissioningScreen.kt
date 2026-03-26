package com.vpedrosa.smarthome.ui.commissioning

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vpedrosa.smarthome.commissioning.domain.model.DiscoveredDevice
import com.vpedrosa.smarthome.ui.theme.ActiveGreen
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import smarthome.composeapp.generated.resources.Res
import smarthome.composeapp.generated.resources.a11y_commissioned_status
import smarthome.composeapp.generated.resources.a11y_navigate_back
import smarthome.composeapp.generated.resources.action_cancel
import smarthome.composeapp.generated.resources.commissioning_button
import smarthome.composeapp.generated.resources.commissioning_commissioned
import smarthome.composeapp.generated.resources.commissioning_name_dialog_message
import smarthome.composeapp.generated.resources.commissioning_name_dialog_title
import smarthome.composeapp.generated.resources.commissioning_name_label
import smarthome.composeapp.generated.resources.commissioning_registered_section
import smarthome.composeapp.generated.resources.commissioning_scan_qr
import smarthome.composeapp.generated.resources.commissioning_scan_qr_description
import smarthome.composeapp.generated.resources.commissioning_success
import smarthome.composeapp.generated.resources.commissioning_title
import smarthome.composeapp.generated.resources.commissioning_no_devices

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommissioningScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToQrScanner: () -> Unit = {},
    viewModel: CommissioningViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    var deviceToName by remember { mutableStateOf<DiscoveredDevice?>(null) }
    var nameInput by remember { mutableStateOf("") }

    // Name dialog
    deviceToName?.let { device ->
        AlertDialog(
            onDismissRequest = { deviceToName = null },
            title = { Text(stringResource(Res.string.commissioning_name_dialog_title)) },
            text = {
                Column {
                    Text(
                        text = stringResource(Res.string.commissioning_name_dialog_message),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text(stringResource(Res.string.commissioning_name_label)) },
                        placeholder = { Text(device.name) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val customName = nameInput.takeIf { it.isNotBlank() }
                    viewModel.commission(device, customName)
                    deviceToName = null
                    nameInput = ""
                }) {
                    Text(stringResource(Res.string.commissioning_button))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    deviceToName = null
                    nameInput = ""
                }) {
                    Text(stringResource(Res.string.action_cancel))
                }
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(Res.string.commissioning_title),
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
            ),
        )

        // Handle QR match: show name dialog for matched device
        state.qrMatchedDevice?.let { matched ->
            if (deviceToName == null) {
                nameInput = matched.name
                deviceToName = matched
                viewModel.dismissQrMatch()
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Scan QR button (prominent)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onNavigateToQrScanner,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(Res.string.commissioning_scan_qr),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            }

            item {
                Text(
                    text = stringResource(Res.string.commissioning_scan_qr_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Commissioning in progress
            if (state.commissioningInProgress.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        ),
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 3.dp,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Registering device...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                }
            }

            // Registered devices section
            if (state.registeredDeviceNames.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(Res.string.commissioning_registered_section),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    )
                }

                items(
                    items = state.registeredDeviceNames,
                    key = { it.first },
                ) { (id, name) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = ActiveGreen.copy(alpha = 0.1f),
                        ),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = stringResource(Res.string.a11y_commissioned_status),
                                modifier = Modifier.size(20.dp),
                                tint = ActiveGreen,
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                                Text(
                                    text = id,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                )
                            }
                            Text(
                                text = stringResource(Res.string.commissioning_commissioned),
                                style = MaterialTheme.typography.labelSmall,
                                color = ActiveGreen,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            } else {
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = stringResource(Res.string.commissioning_no_devices),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        // Snackbars
        state.successMessage?.let { deviceName ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
                containerColor = ActiveGreen,
                action = {
                    TextButton(onClick = { viewModel.dismissSuccess() }) {
                        Text("OK", color = Color.White)
                    }
                },
            ) {
                Text(
                    stringResource(Res.string.commissioning_success, deviceName),
                    color = Color.White,
                )
            }
        }

        state.lastError?.let { errorMsg ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.dismissError() }) {
                        Text("OK")
                    }
                },
            ) {
                Text(errorMsg)
            }
        }
    }
}
