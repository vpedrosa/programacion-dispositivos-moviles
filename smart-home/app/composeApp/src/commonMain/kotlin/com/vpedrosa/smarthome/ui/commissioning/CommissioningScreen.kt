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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vpedrosa.smarthome.commissioning.domain.model.DiscoveredDevice
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import smarthome.composeapp.generated.resources.Res
import smarthome.composeapp.generated.resources.a11y_navigate_back
import smarthome.composeapp.generated.resources.commissioning_all_done
import smarthome.composeapp.generated.resources.commissioning_button
import smarthome.composeapp.generated.resources.commissioning_commissioned
import smarthome.composeapp.generated.resources.commissioning_devices_available
import smarthome.composeapp.generated.resources.commissioning_passcode
import smarthome.composeapp.generated.resources.commissioning_success
import smarthome.composeapp.generated.resources.commissioning_title
import smarthome.composeapp.generated.resources.a11y_commissioned_status
import smarthome.composeapp.generated.resources.commissioning_recommission

private val ActiveGreen = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommissioningScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: CommissioningViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val pendingDevices = state.allDevices.filterNot { it.serialNumber in state.commissionedSerials }

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

        if (pendingDevices.isEmpty() && state.allDevices.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = stringResource(Res.string.commissioning_all_done),
                    modifier = Modifier.size(64.dp),
                    tint = ActiveGreen,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(Res.string.commissioning_all_done),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    Text(
                        text = stringResource(Res.string.commissioning_devices_available, state.allDevices.size),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }

                items(state.allDevices, key = { it.serialNumber }) { device ->
                    val isCommissioned = device.serialNumber in state.commissionedSerials
                    DiscoveredDeviceCard(
                        device = device,
                        isCommissioned = isCommissioned,
                        isCommissioning = device.serialNumber in state.commissioningInProgress,
                        onCommission = { viewModel.commission(device) },
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }

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

@Composable
private fun DiscoveredDeviceCard(
    device: DiscoveredDevice,
    isCommissioned: Boolean,
    isCommissioning: Boolean,
    onCommission: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCommissioned) {
                ActiveGreen.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "${device.type.name} · ${device.host}:${device.port}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
                Text(
                    text = stringResource(Res.string.commissioning_passcode, device.passcode),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                )
            }

            when {
                isCommissioned -> {
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = stringResource(Res.string.a11y_commissioned_status),
                                modifier = Modifier.size(16.dp),
                                tint = ActiveGreen,
                            )
                            Text(
                                text = stringResource(Res.string.commissioning_commissioned),
                                modifier = Modifier.padding(start = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = ActiveGreen,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = onCommission,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sensors,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = stringResource(Res.string.commissioning_recommission),
                                modifier = Modifier.padding(start = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }
                isCommissioning -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                else -> {
                    Button(
                        onClick = onCommission,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = stringResource(Res.string.commissioning_button),
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                }
            }
        }
    }
}
