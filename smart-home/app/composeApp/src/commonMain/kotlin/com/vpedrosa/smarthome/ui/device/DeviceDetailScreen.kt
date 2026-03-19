package com.vpedrosa.smarthome.ui.device

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vpedrosa.smarthome.shared.domain.model.Blind
import com.vpedrosa.smarthome.shared.domain.model.ContactSensor
import com.vpedrosa.smarthome.shared.domain.model.Device
import com.vpedrosa.smarthome.shared.domain.model.DeviceEvent
import com.vpedrosa.smarthome.shared.domain.model.DeviceEventType
import com.vpedrosa.smarthome.shared.domain.model.Light
import com.vpedrosa.smarthome.shared.domain.model.Lock
import com.vpedrosa.smarthome.shared.domain.model.SmartTv
import com.vpedrosa.smarthome.shared.domain.model.SmokeSensor
import com.vpedrosa.smarthome.shared.domain.model.Switch
import com.vpedrosa.smarthome.shared.domain.model.TemperatureSensor
import com.vpedrosa.smarthome.shared.domain.model.Thermostat
import com.vpedrosa.smarthome.shared.domain.model.WaterLeakSensor
import com.vpedrosa.smarthome.shared.domain.model.Color as DomainColor
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import smarthome.composeapp.generated.resources.Res
import smarthome.composeapp.generated.resources.a11y_cast_content
import smarthome.composeapp.generated.resources.a11y_decrease_temperature
import smarthome.composeapp.generated.resources.a11y_increase_temperature
import smarthome.composeapp.generated.resources.a11y_lock_door
import smarthome.composeapp.generated.resources.a11y_navigate_back
import smarthome.composeapp.generated.resources.a11y_play_video
import smarthome.composeapp.generated.resources.a11y_unlock_door
import smarthome.composeapp.generated.resources.blind_closed
import smarthome.composeapp.generated.resources.blind_open
import smarthome.composeapp.generated.resources.blind_open_percent
import smarthome.composeapp.generated.resources.casting_active
import smarthome.composeapp.generated.resources.casting_inactive
import smarthome.composeapp.generated.resources.casting_section
import smarthome.composeapp.generated.resources.contact_closed
import smarthome.composeapp.generated.resources.contact_open
import smarthome.composeapp.generated.resources.control_brightness
import smarthome.composeapp.generated.resources.control_color
import smarthome.composeapp.generated.resources.control_current_temperature
import smarthome.composeapp.generated.resources.control_heating
import smarthome.composeapp.generated.resources.control_opening_level
import smarthome.composeapp.generated.resources.control_target_temperature
import smarthome.composeapp.generated.resources.device_detail_no_events
import smarthome.composeapp.generated.resources.device_detail_recent_history
import smarthome.composeapp.generated.resources.device_detail_state
import smarthome.composeapp.generated.resources.heating_active
import smarthome.composeapp.generated.resources.heating_cooling
import smarthome.composeapp.generated.resources.heating_inactive
import smarthome.composeapp.generated.resources.heating_warming
import smarthome.composeapp.generated.resources.lock_closed
import smarthome.composeapp.generated.resources.lock_open
import smarthome.composeapp.generated.resources.lock_toggle_lock
import smarthome.composeapp.generated.resources.lock_toggle_unlock
import smarthome.composeapp.generated.resources.send_cast
import smarthome.composeapp.generated.resources.sensor_no_leak
import smarthome.composeapp.generated.resources.sensor_no_smoke
import smarthome.composeapp.generated.resources.sensor_smoke_detected
import smarthome.composeapp.generated.resources.sensor_water_leak
import smarthome.composeapp.generated.resources.action_close
import smarthome.composeapp.generated.resources.action_on
import smarthome.composeapp.generated.resources.action_off
import smarthome.composeapp.generated.resources.action_open
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

// ── Color conversion helpers ────────────────────────────────────────────────────

private fun DomainColor.toComposeColor(): Color =
    Color(red = red / 255f, green = green / 255f, blue = blue / 255f)

private fun hsvToColor(hue: Float, saturation: Float, value: Float): Color {
    val c = value * saturation
    val x = c * (1f - kotlin.math.abs((hue / 60f) % 2f - 1f))
    val m = value - c
    val (r, g, b) = when {
        hue < 60f -> Triple(c, x, 0f)
        hue < 120f -> Triple(x, c, 0f)
        hue < 180f -> Triple(0f, c, x)
        hue < 240f -> Triple(0f, x, c)
        hue < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    return Color(r + m, g + m, b + m)
}

private fun hsvToDomainColor(hue: Float, saturation: Float, value: Float): DomainColor {
    val compose = hsvToColor(hue, saturation, value)
    return DomainColor(
        red = (compose.red * 255).roundToInt().coerceIn(0, 255),
        green = (compose.green * 255).roundToInt().coerceIn(0, 255),
        blue = (compose.blue * 255).roundToInt().coerceIn(0, 255),
    )
}

// ── Main screen ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailScreen(
    deviceId: String,
    onNavigateBack: () -> Unit,
    viewModel: DeviceDetailViewModel = koinViewModel { parametersOf(deviceId) },
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
                    text = state.device?.name ?: "",
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

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            val device = state.device
            if (device != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    when (device) {
                        is Light -> LightContent(
                            device = device,
                            roomName = state.roomName,
                            onToggle = viewModel::onToggle,
                            onUpdateColor = viewModel::onUpdateColor,
                            onUpdateBrightness = viewModel::onUpdateBrightness,
                        )
                        is Lock -> LockContent(
                            device = device,
                            roomName = state.roomName,
                            events = state.deviceEvents,
                            onToggle = viewModel::onToggle,
                        )
                        is Blind -> BlindContent(
                            device = device,
                            onUpdateOpeningLevel = viewModel::onUpdateOpeningLevel,
                        )
                        is Thermostat -> ThermostatContent(
                            device = device,
                            onUpdateTargetTemp = viewModel::onUpdateTargetTemperature,
                            onToggleHeating = viewModel::onToggleHeating,
                        )
                        is SmartTv -> SmartTvContent(
                            device = device,
                            roomName = state.roomName,
                            onToggle = viewModel::onToggle,
                            onToggleCasting = viewModel::onToggleCasting,
                        )
                        is Switch -> SwitchContent(
                            device = device,
                            roomName = state.roomName,
                            onToggle = viewModel::onToggle,
                        )
                        is SmokeSensor -> SmokeSensorContent(device = device)
                        is WaterLeakSensor -> WaterLeakSensorContent(device = device)
                        is TemperatureSensor -> TemperatureSensorContent(device = device)
                        is ContactSensor -> ContactSensorContent(device = device)
                    }
                }
            }
        }
    }
}

// ── Reusable section card ───────────────────────────────────────────────────────

@Composable
private fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            content()
        }
    }
}

// ── Light ────────────────────────────────────────────────────────────────────────

@Composable
private fun LightContent(
    device: Light,
    roomName: String?,
    onToggle: () -> Unit,
    onUpdateColor: (DomainColor) -> Unit,
    onUpdateBrightness: (Int) -> Unit,
) {
    // State card
    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = stringResource(Res.string.device_detail_state),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(4.dp))
                val onOffText = if (device.isOn) stringResource(Res.string.action_on) else stringResource(Res.string.action_off)
                val subtitle = buildString {
                    append(onOffText)
                    if (roomName != null) append(" · $roomName")
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
            }
            Switch(
                checked = device.isOn,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    checkedThumbColor = Color.White,
                    uncheckedTrackColor = MaterialTheme.colorScheme.secondary,
                    uncheckedThumbColor = Color.White,
                ),
            )
        }
    }

    // Color card
    SectionCard {
        Text(
            text = stringResource(Res.string.control_color),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(12.dp))
        HueBarPicker(
            currentColor = device.color,
            onColorSelected = onUpdateColor,
        )
    }

    // Brightness card
    SectionCard {
        Text(
            text = stringResource(Res.string.control_brightness),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(8.dp))

        var localBrightness by remember(device.id) { mutableFloatStateOf(device.brightness.toFloat()) }
        var isDragging by remember { mutableStateOf(false) }
        if (!isDragging) localBrightness = device.brightness.toFloat()

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Slider(
                value = localBrightness,
                onValueChange = {
                    isDragging = true
                    localBrightness = it
                },
                onValueChangeFinished = {
                    isDragging = false
                    onUpdateBrightness(localBrightness.roundToInt())
                },
                valueRange = 0f..100f,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                ),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "${localBrightness.roundToInt()}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(48.dp),
                textAlign = TextAlign.End,
            )
        }
    }
}

// ── Color picker (hue bar) ──────────────────────────────────────────────────────

@Composable
private fun HueBarPicker(
    currentColor: DomainColor,
    onColorSelected: (DomainColor) -> Unit,
) {
    val composeColor = currentColor.toComposeColor()

    // Hue spectrum bar
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val hue = (offset.x / size.width) * 360f
                    val newColor = hsvToDomainColor(
                        hue = hue.coerceIn(0f, 360f),
                        saturation = 1f,
                        value = 1f,
                    )
                    onColorSelected(newColor)
                }
            },
    ) {
        val barHeight = size.height
        val barWidth = size.width
        val steps = 360
        val stepWidth = barWidth / steps

        for (i in 0 until steps) {
            val hue = i.toFloat()
            val color = hsvToColor(hue, 1f, 1f)
            drawRect(
                color = color,
                topLeft = Offset(i * stepWidth, 0f),
                size = Size(stepWidth + 1f, barHeight),
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Current color preview
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(composeColor)
                .border(2.dp, MaterialTheme.colorScheme.secondary, CircleShape),
        )
        Text(
            text = "RGB(${currentColor.red}, ${currentColor.green}, ${currentColor.blue})",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        )
    }
}

// ── Lock ─────────────────────────────────────────────────────────────────────────

@Composable
private fun LockContent(
    device: Lock,
    roomName: String?,
    events: List<DeviceEvent>,
    onToggle: () -> Unit,
) {
    val navy = MaterialTheme.colorScheme.primary

    // Lock status circle
    SectionCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        if (device.isLocked) navy.copy(alpha = 0.1f)
                        else Color(0xFFFFEBEE),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (device.isLocked) Icons.Default.Lock
                    else Icons.Default.LockOpen,
                    contentDescription = stringResource(
                        if (device.isLocked) Res.string.a11y_lock_door
                        else Res.string.a11y_unlock_door,
                    ),
                    modifier = Modifier.size(48.dp),
                    tint = if (device.isLocked) navy else Color(0xFFD32F2F),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(
                    if (device.isLocked) Res.string.lock_closed else Res.string.lock_open
                ),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (roomName != null) {
                Text(
                    text = roomName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onToggle,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (device.isLocked) Color(0xFFD32F2F) else navy,
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = stringResource(
                        if (device.isLocked) Res.string.lock_toggle_unlock
                        else Res.string.lock_toggle_lock,
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
        }
    }

    // Recent history
    SectionCard {
        Text(
            text = stringResource(Res.string.device_detail_recent_history),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (events.isEmpty()) {
            Text(
                text = stringResource(Res.string.device_detail_no_events),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            )
        } else {
            events.take(10).forEach { event ->
                EventRow(event)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun EventRow(event: DeviceEvent) {
    val localDateTime = event.timestamp.toLocalDateTime(TimeZone.currentSystemDefault())
    val timeText = "%02d:%02d".format(localDateTime.hour, localDateTime.minute)
    val dateText = "%02d/%02d/%d".format(
        localDateTime.date.day,
        localDateTime.date.month.ordinal + 1,
        localDateTime.year,
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val isLocked = event.type == DeviceEventType.DOOR_CLOSED
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (isLocked) MaterialTheme.colorScheme.primary
                        else Color(0xFFD32F2F),
                    ),
            )
            Text(
                text = event.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        Text(
            text = "$timeText · $dateText",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
        )
    }
}

// ── Blind ────────────────────────────────────────────────────────────────────────

@Composable
private fun BlindContent(
    device: Blind,
    onUpdateOpeningLevel: (Int) -> Unit,
) {
    val navy = MaterialTheme.colorScheme.primary

    // Visual representation
    SectionCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BlindVisual(openingLevel = device.openingLevel)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(Res.string.blind_open_percent, device.openingLevel),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }

    // Opening level slider
    SectionCard {
        Text(
            text = stringResource(Res.string.control_opening_level),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(8.dp))

        var localLevel by remember(device.id) { mutableFloatStateOf(device.openingLevel.toFloat()) }
        var isDragging by remember { mutableStateOf(false) }
        if (!isDragging) localLevel = device.openingLevel.toFloat()

        Slider(
            value = localLevel,
            onValueChange = {
                isDragging = true
                localLevel = it
            },
            onValueChangeFinished = {
                isDragging = false
                onUpdateOpeningLevel(localLevel.roundToInt())
            },
            valueRange = 0f..100f,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = navy,
                activeTrackColor = navy,
                inactiveTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
            ),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(Res.string.blind_closed),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            )
            Text(
                text = stringResource(Res.string.blind_open),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            )
        }
    }

    // Action buttons
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = { onUpdateOpeningLevel(0) },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onBackground,
            ),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text = stringResource(Res.string.action_close),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
        Button(
            onClick = { onUpdateOpeningLevel(100) },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = navy,
                contentColor = Color.White,
            ),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text = stringResource(Res.string.action_open),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun BlindVisual(openingLevel: Int) {
    val navy = MaterialTheme.colorScheme.primary
    val closedFraction = 1f - (openingLevel / 100f)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
    ) {
        val frameMargin = 24f
        val frameWidth = size.width - frameMargin * 2
        val frameHeight = size.height - 16f
        val frameLeft = frameMargin
        val frameTop = 8f

        // Window frame
        drawRect(
            color = Color(0xFFE0E0E0),
            topLeft = Offset(frameLeft, frameTop),
            size = Size(frameWidth, frameHeight),
        )

        // Sky / light area
        drawRect(
            color = Color(0xFFBBDEFB),
            topLeft = Offset(frameLeft + 4f, frameTop + 4f),
            size = Size(frameWidth - 8f, frameHeight - 8f),
        )

        // Blind slats (closed portion)
        val blindHeight = (frameHeight - 8f) * closedFraction
        val slatCount = (blindHeight / 12f).toInt().coerceAtLeast(0)
        for (i in 0 until slatCount) {
            val y = frameTop + 4f + i * 12f
            drawRect(
                color = Color(0xFF123458).copy(alpha = 0.15f),
                topLeft = Offset(frameLeft + 4f, y),
                size = Size(frameWidth - 8f, 10f),
            )
            drawLine(
                color = Color(0xFF123458).copy(alpha = 0.3f),
                start = Offset(frameLeft + 4f, y + 10f),
                end = Offset(frameLeft + frameWidth - 4f, y + 10f),
                strokeWidth = 1f,
            )
        }

        // Frame border
        drawRect(
            color = Color(0xFF123458),
            topLeft = Offset(frameLeft, frameTop),
            size = Size(frameWidth, frameHeight),
            style = Stroke(width = 3f),
        )
    }
}

// ── Thermostat ───────────────────────────────────────────────────────────────────

@Composable
private fun ThermostatContent(
    device: Thermostat,
    onUpdateTargetTemp: (Double) -> Unit,
    onToggleHeating: () -> Unit,
) {
    val navy = MaterialTheme.colorScheme.primary

    // Current temperature
    SectionCard {
        Text(
            text = stringResource(Res.string.control_current_temperature),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${device.currentTemperature}\u00B0C",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = navy,
        )
    }

    // Target temperature dial
    SectionCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(Res.string.control_target_temperature).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                letterSpacing = 2.sp,
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Circular dial
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 12f
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    // Background arc
                    drawArc(
                        color = Color(0xFFE0E0E0),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )

                    // Filled arc based on target temp (range 15-30)
                    val fraction = ((device.targetTemperature - 15.0) / 15.0)
                        .coerceIn(0.0, 1.0).toFloat()
                    drawArc(
                        color = Color(0xFF123458),
                        startAngle = 135f,
                        sweepAngle = 270f * fraction,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${"%.1f".format(device.targetTemperature)}\u00B0",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = navy,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // +/- buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = {
                        val newTemp = (device.targetTemperature - 0.1).coerceAtLeast(15.0)
                        onUpdateTargetTemp((newTemp * 10).roundToInt() / 10.0)
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)),
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = stringResource(Res.string.a11y_decrease_temperature),
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }

                Text(
                    text = "${"%.1f".format(device.targetTemperature)}\u00B0C",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                IconButton(
                    onClick = {
                        val newTemp = (device.targetTemperature + 0.1).coerceAtMost(30.0)
                        onUpdateTargetTemp((newTemp * 10).roundToInt() / 10.0)
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(Res.string.a11y_increase_temperature),
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        }
    }

    // Heating toggle
    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = stringResource(Res.string.control_heating),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(4.dp))
                val heatingStatus = if (device.isHeatingOn) {
                    val mode = if (device.currentTemperature < device.targetTemperature)
                        stringResource(Res.string.heating_warming)
                    else
                        stringResource(Res.string.heating_cooling)
                    "${stringResource(Res.string.heating_active)} · $mode"
                } else {
                    stringResource(Res.string.heating_inactive)
                }
                Text(
                    text = heatingStatus,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
            }
            Switch(
                checked = device.isHeatingOn,
                onCheckedChange = { onToggleHeating() },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    checkedThumbColor = Color.White,
                    uncheckedTrackColor = MaterialTheme.colorScheme.secondary,
                    uncheckedThumbColor = Color.White,
                ),
            )
        }
    }
}

// ── Smart TV ─────────────────────────────────────────────────────────────────────

@Composable
private fun SmartTvContent(
    device: SmartTv,
    roomName: String?,
    onToggle: () -> Unit,
    onToggleCasting: () -> Unit,
) {
    val navy = MaterialTheme.colorScheme.primary

    // State toggle
    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = stringResource(Res.string.device_detail_state),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(4.dp))
                val onOffText = if (device.isOn) stringResource(Res.string.action_on) else stringResource(Res.string.action_off)
                val subtitle = buildString {
                    append(onOffText)
                    if (roomName != null) append(" · $roomName")
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
            }
            Switch(
                checked = device.isOn,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = navy,
                    checkedThumbColor = Color.White,
                    uncheckedTrackColor = MaterialTheme.colorScheme.secondary,
                    uncheckedThumbColor = Color.White,
                ),
            )
        }
    }

    // Casting section
    SectionCard {
        Text(
            text = stringResource(Res.string.casting_section),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Dark preview area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1A1A1A)),
            contentAlignment = Alignment.Center,
        ) {
            if (device.isCasting) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = stringResource(Res.string.a11y_play_video),
                    modifier = Modifier.size(48.dp),
                    tint = Color.White.copy(alpha = 0.8f),
                )
            } else {
                Icon(
                    imageVector = Icons.Default.CastConnected,
                    contentDescription = stringResource(Res.string.a11y_cast_content),
                    modifier = Modifier.size(48.dp),
                    tint = Color.White.copy(alpha = 0.3f),
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(
                if (device.isCasting) Res.string.casting_active
                else Res.string.casting_inactive,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
        )

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onToggleCasting,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = navy,
                contentColor = Color.White,
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = device.isOn,
        ) {
            Icon(
                imageVector = Icons.Default.CastConnected,
                contentDescription = stringResource(Res.string.a11y_cast_content),
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(Res.string.send_cast),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
    }
}

// ── Switch ───────────────────────────────────────────────────────────────────────

@Composable
private fun SwitchContent(
    device: Switch,
    roomName: String?,
    onToggle: () -> Unit,
) {
    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = stringResource(Res.string.device_detail_state),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(4.dp))
                val onOffText = if (device.isOn) stringResource(Res.string.action_on) else stringResource(Res.string.action_off)
                val subtitle = buildString {
                    append(onOffText)
                    if (roomName != null) append(" · $roomName")
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
            }
            Switch(
                checked = device.isOn,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    checkedThumbColor = Color.White,
                    uncheckedTrackColor = MaterialTheme.colorScheme.secondary,
                    uncheckedThumbColor = Color.White,
                ),
            )
        }
    }
}

// ── Sensors ──────────────────────────────────────────────────────────────────────

@Composable
private fun SmokeSensorContent(device: SmokeSensor) {
    SectionCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val isAlert = device.isSmokeDetected
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        if (isAlert) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (isAlert) "!" else "OK",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isAlert) Color(0xFFD32F2F) else Color(0xFF388E3C),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(
                    if (isAlert) Res.string.sensor_smoke_detected
                    else Res.string.sensor_no_smoke,
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
private fun WaterLeakSensorContent(device: WaterLeakSensor) {
    SectionCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val isAlert = device.isLeakDetected
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        if (isAlert) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (isAlert) "!" else "OK",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isAlert) Color(0xFFD32F2F) else Color(0xFF388E3C),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(
                    if (isAlert) Res.string.sensor_water_leak
                    else Res.string.sensor_no_leak,
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
private fun TemperatureSensorContent(device: TemperatureSensor) {
    SectionCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Default.Thermostat,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${device.currentTemperature}\u00B0C",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(Res.string.control_current_temperature),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
private fun ContactSensorContent(device: ContactSensor) {
    SectionCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val isOpen = device.isOpen
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        if (isOpen) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isOpen) Icons.Default.LockOpen else Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = if (isOpen) Color(0xFFD32F2F) else Color(0xFF388E3C),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(
                    if (isOpen) Res.string.contact_open else Res.string.contact_closed,
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}
