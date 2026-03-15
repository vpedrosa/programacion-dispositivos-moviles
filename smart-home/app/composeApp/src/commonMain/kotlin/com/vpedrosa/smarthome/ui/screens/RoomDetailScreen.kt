package com.vpedrosa.smarthome.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import smarthome.composeapp.generated.resources.Res
import smarthome.composeapp.generated.resources.a11y_edit_group
import smarthome.composeapp.generated.resources.a11y_navigate_back

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailScreen(
    roomId: String,
    onNavigateToDeviceDetail: (String) -> Unit = {},
    onNavigateToEditGroup: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    viewModel: RoomDetailViewModel = koinViewModel { parametersOf(roomId) },
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        TopAppBar(
            title = {
                Text(
                    text = state.roomName,
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
            actions = {
                IconButton(onClick = onNavigateToEditGroup) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(Res.string.a11y_edit_group),
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground,
                navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                actionIconContentColor = MaterialTheme.colorScheme.onBackground,
            ),
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.devicesByType.forEach { (type, devices) ->
                item(key = "header_$type") {
                    DeviceCategorySectionHeader(
                        type = type,
                        count = devices.size,
                    )
                }

                items(
                    items = devices,
                    key = { it.id.value },
                ) { device ->
                    DeviceRow(
                        device = device,
                        roomName = null,
                        isToggleable = type.isToggleable(),
                        onToggle = { viewModel.onToggleDevice(device.id) },
                        onClick = { onNavigateToDeviceDetail(device.id.value) },
                    )
                }

                if (type.supportsBulkToggle()) {
                    item(key = "bulk_$type") {
                        BulkToggleButton(
                            allActive = devices.allActive(),
                            onBulkToggle = { turnOn ->
                                viewModel.onBulkToggle(type, turnOn)
                            },
                        )
                    }
                }

                item(key = "spacer_$type") {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
