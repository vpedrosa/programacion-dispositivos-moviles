package com.vpedrosa.smarthome.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import com.vpedrosa.smarthome.ui.components.NotificationPermissionHandler
import com.vpedrosa.smarthome.ui.components.WatchConnectionHandler
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import smarthome.composeapp.generated.resources.Res
import smarthome.composeapp.generated.resources.settings_about
import smarthome.composeapp.generated.resources.settings_about_value
import smarthome.composeapp.generated.resources.settings_devices
import smarthome.composeapp.generated.resources.settings_general
import smarthome.composeapp.generated.resources.settings_language
import smarthome.composeapp.generated.resources.settings_language_value
import smarthome.composeapp.generated.resources.settings_manage_simulated
import smarthome.composeapp.generated.resources.settings_matter_commissioning
import smarthome.composeapp.generated.resources.settings_modes
import smarthome.composeapp.generated.resources.settings_notification_denied
import smarthome.composeapp.generated.resources.settings_notification_granted
import smarthome.composeapp.generated.resources.settings_notification_permission
import smarthome.composeapp.generated.resources.settings_notifications_section
import smarthome.composeapp.generated.resources.settings_notifications_toggle
import smarthome.composeapp.generated.resources.settings_notifications_toggle_subtitle
import smarthome.composeapp.generated.resources.settings_simulator_section
import smarthome.composeapp.generated.resources.settings_simulator_search
import smarthome.composeapp.generated.resources.settings_simulator_connected
import smarthome.composeapp.generated.resources.settings_simulator_not_found
import smarthome.composeapp.generated.resources.settings_simulator_searching
import smarthome.composeapp.generated.resources.settings_smartwatch_section
import smarthome.composeapp.generated.resources.settings_watch_connected
import smarthome.composeapp.generated.resources.settings_watch_connection
import smarthome.composeapp.generated.resources.settings_watch_not_connected
import smarthome.composeapp.generated.resources.a11y_navigate_forward
import smarthome.composeapp.generated.resources.title_anti_squatter
import smarthome.composeapp.generated.resources.title_settings
import com.vpedrosa.smarthome.ui.theme.ActiveGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToAntiSquatter: () -> Unit = {},
    onNavigateToCommissioning: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel(),
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
                    text = stringResource(Res.string.title_settings),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground,
            ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            // --- DISPOSITIVOS ---
            SectionHeader(text = stringResource(Res.string.settings_devices))
            Spacer(modifier = Modifier.height(8.dp))
            SettingsNavigationItem(
                title = stringResource(Res.string.settings_matter_commissioning),
                subtitle = stringResource(Res.string.settings_manage_simulated),
                onClick = onNavigateToCommissioning,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- SIMULADOR ---
            SectionHeader(text = stringResource(Res.string.settings_simulator_section))
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (state.simulatorHost != null) {
                        ActiveGreen.copy(alpha = 0.1f)
                    } else {
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                    },
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            tint = if (state.simulatorHost != null) ActiveGreen
                            else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            if (state.simulatorHost != null) {
                                Text(
                                    text = stringResource(Res.string.settings_simulator_connected),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                                Text(
                                    text = state.simulatorHost!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ActiveGreen,
                                )
                            } else if (state.isSearching) {
                                Text(
                                    text = stringResource(Res.string.settings_simulator_searching),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                            } else if (state.searchError) {
                                Text(
                                    text = stringResource(Res.string.settings_simulator_not_found),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color(0xFFE53935),
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.searchSimulator() },
                        enabled = !state.isSearching,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        if (state.isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = stringResource(Res.string.settings_simulator_search),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- NOTIFICACIONES ---
            SectionHeader(text = stringResource(Res.string.settings_notifications_section))
            Spacer(modifier = Modifier.height(8.dp))

            NotificationPermissionHandler(onPermissionResult = {}) { hasPermission, requestPermission ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { if (!hasPermission) requestPermission() },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (hasPermission) {
                            ActiveGreen.copy(alpha = 0.1f)
                        } else {
                            Color(0xFFE53935).copy(alpha = 0.1f)
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
                                text = stringResource(Res.string.settings_notification_permission),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Text(
                                text = if (hasPermission) {
                                    stringResource(Res.string.settings_notification_granted)
                                } else {
                                    stringResource(Res.string.settings_notification_denied)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (hasPermission) ActiveGreen else Color(0xFFE53935),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                ),
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    SettingsToggleItem(
                        title = stringResource(Res.string.settings_notifications_toggle),
                        subtitle = stringResource(Res.string.settings_notifications_toggle_subtitle),
                        isChecked = state.notificationsEnabled,
                        onToggle = { viewModel.toggleNotifications() },
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- MODOS ---
            SectionHeader(text = stringResource(Res.string.settings_modes))
            Spacer(modifier = Modifier.height(8.dp))
            SettingsNavigationItem(
                title = stringResource(Res.string.title_anti_squatter),
                onClick = onNavigateToAntiSquatter,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- SMARTWATCH ---
            SectionHeader(text = stringResource(Res.string.settings_smartwatch_section))
            Spacer(modifier = Modifier.height(8.dp))

            WatchConnectionHandler { isConnected, watchName ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isConnected) {
                            ActiveGreen.copy(alpha = 0.1f)
                        } else {
                            Color(0xFFE53935).copy(alpha = 0.1f)
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
                                text = stringResource(Res.string.settings_watch_connection),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Text(
                                text = if (isConnected) {
                                    watchName ?: stringResource(Res.string.settings_watch_connected)
                                } else {
                                    stringResource(Res.string.settings_watch_not_connected)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isConnected) ActiveGreen else Color(0xFFE53935),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- GENERAL ---
            SectionHeader(text = stringResource(Res.string.settings_general))
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                ),
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    SettingsInfoItem(
                        title = stringResource(Res.string.settings_language),
                        value = stringResource(Res.string.settings_language_value),
                    )
                    SettingsInfoItem(
                        title = stringResource(Res.string.settings_about),
                        value = stringResource(Res.string.settings_about_value),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
    )
}

@Composable
private fun SettingsNavigationItem(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
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
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = stringResource(Res.string.a11y_navigate_forward),
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            )
        }
    }
}

@Composable
private fun SettingsToggleItem(
    title: String,
    subtitle: String? = null,
    isChecked: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
            }
        }
        Switch(
            checked = isChecked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ActiveGreen,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = MaterialTheme.colorScheme.secondary,
            ),
        )
    }
}

@Composable
private fun SettingsInfoItem(
    title: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        )
    }
}
