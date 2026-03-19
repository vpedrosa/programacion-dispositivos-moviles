package com.vpedrosa.smarthome.ui.antisquatter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.vpedrosa.smarthome.antisquatter.domain.model.LightTimeSlot
import com.vpedrosa.smarthome.shared.domain.model.RoomId
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import smarthome.composeapp.generated.resources.Res
import smarthome.composeapp.generated.resources.anti_squatter_active_subtitle
import smarthome.composeapp.generated.resources.anti_squatter_add_slot
import smarthome.composeapp.generated.resources.anti_squatter_summary_empty
import smarthome.composeapp.generated.resources.anti_squatter_summary_lights
import smarthome.composeapp.generated.resources.anti_squatter_summary_lights_no_rooms
import smarthome.composeapp.generated.resources.anti_squatter_summary_title
import smarthome.composeapp.generated.resources.anti_squatter_summary_tv
import smarthome.composeapp.generated.resources.anti_squatter_change_video
import smarthome.composeapp.generated.resources.anti_squatter_inactive_subtitle
import smarthome.composeapp.generated.resources.anti_squatter_lights_section
import smarthome.composeapp.generated.resources.anti_squatter_play_video
import smarthome.composeapp.generated.resources.anti_squatter_presence_simulation
import smarthome.composeapp.generated.resources.anti_squatter_time_range
import smarthome.composeapp.generated.resources.anti_squatter_tv_section
import smarthome.composeapp.generated.resources.anti_squatter_video_url
import smarthome.composeapp.generated.resources.a11y_add_time_slot
import smarthome.composeapp.generated.resources.a11y_navigate_back
import smarthome.composeapp.generated.resources.a11y_remove_time_slot
import smarthome.composeapp.generated.resources.title_anti_squatter

private val ActiveGreen = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AntiSquatterScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: AntiSquatterViewModel = koinViewModel(),
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
                    text = stringResource(Res.string.title_anti_squatter),
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
            // --- Presence simulation toggle ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (state.isEnabled) {
                        ActiveGreen.copy(alpha = 0.12f)
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
                            text = stringResource(Res.string.anti_squatter_presence_simulation),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = if (state.isEnabled) {
                                stringResource(Res.string.anti_squatter_active_subtitle)
                            } else {
                                stringResource(Res.string.anti_squatter_inactive_subtitle)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        )
                    }
                    Switch(
                        checked = state.isEnabled,
                        onCheckedChange = { viewModel.toggleEnabled() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ActiveGreen,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = MaterialTheme.colorScheme.secondary,
                        ),
                    )
                }
            }

            // --- ACTIVE PATTERN SUMMARY ---
            if (state.isEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                PatternSummaryCard(state = state)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- LIGHT TIME SLOTS SECTION ---
            Text(
                text = stringResource(Res.string.anti_squatter_lights_section),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            )
            Spacer(modifier = Modifier.height(12.dp))

            state.timeSlots.forEach { slot ->
                TimeSlotCard(
                    slot = slot,
                    roomNames = state.rooms.associate { it.id to it.name },
                    allRoomIds = state.rooms.map { it.id },
                    onRemove = { viewModel.removeTimeSlot(slot.id) },
                    onUpdate = { viewModel.updateTimeSlot(it) },
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            TextButton(
                onClick = { viewModel.addTimeSlot() },
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(Res.string.a11y_add_time_slot),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(Res.string.anti_squatter_add_slot),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- SMART TV SECTION ---
            Text(
                text = stringResource(Res.string.anti_squatter_tv_section),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            )
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    // Video toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(Res.string.anti_squatter_play_video),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Switch(
                            checked = state.videoConfig.isEnabled,
                            onCheckedChange = { viewModel.toggleVideoEnabled() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ActiveGreen,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = MaterialTheme.colorScheme.secondary,
                            ),
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Time range display
                    Text(
                        text = "${stringResource(Res.string.anti_squatter_time_range)}: ${state.videoConfig.formatTimeRange()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // URL field
                    OutlinedTextField(
                        value = state.videoConfig.videoUrl,
                        onValueChange = { viewModel.updateVideoUrl(it) },
                        label = {
                            Text(text = stringResource(Res.string.anti_squatter_video_url))
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

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { /* Video change action - placeholder */ },
                    ) {
                        Text(
                            text = stringResource(Res.string.anti_squatter_change_video),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TimeSlotCard(
    slot: LightTimeSlot,
    roomNames: Map<RoomId, String>,
    allRoomIds: List<RoomId>,
    onRemove: () -> Unit,
    onUpdate: (LightTimeSlot) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = slot.formatTimeRange(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(Res.string.a11y_remove_time_slot),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    )
                }
            }

            // Room names as comma-separated text
            val slotRoomNames = slot.roomIds.mapNotNull { roomNames[it] }
            if (slotRoomNames.isNotEmpty()) {
                Text(
                    text = slotRoomNames.joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Room selection chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                allRoomIds.forEach { roomId ->
                    val isSelected = roomId in slot.roomIds
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val updatedRoomIds = if (isSelected) {
                                slot.roomIds - roomId
                            } else {
                                slot.roomIds + roomId
                            }
                            onUpdate(slot.copy(roomIds = updatedRoomIds))
                        },
                        label = {
                            Text(
                                text = roomNames[roomId] ?: roomId.value,
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
    }
}

@Composable
private fun PatternSummaryCard(state: AntiSquatterUiState) {
    val roomNames = state.rooms.associate { it.id to it.name }

    val lines = buildList {
        for (slot in state.timeSlots) {
            val names = slot.roomIds.mapNotNull { roomNames[it] }
            if (names.isNotEmpty()) {
                add(stringResource(Res.string.anti_squatter_summary_lights, names.joinToString(", "), slot.formatTimeRange()))
            } else {
                add(stringResource(Res.string.anti_squatter_summary_lights_no_rooms, slot.formatTimeRange()))
            }
        }
        if (state.videoConfig.isEnabled) {
            val url = state.videoConfig.videoUrl.ifBlank { "—" }
            add(stringResource(Res.string.anti_squatter_summary_tv, state.videoConfig.formatTimeRange(), url))
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(Res.string.anti_squatter_summary_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (lines.isEmpty()) {
                Text(
                    text = stringResource(Res.string.anti_squatter_summary_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
            } else {
                lines.forEach { line ->
                    Text(
                        text = "• $line",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        modifier = Modifier.padding(vertical = 2.dp),
                    )
                }
            }
        }
    }
}
