package com.vpedrosa.smarthome.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.vpedrosa.smarthome.navigation.Dashboard
import com.vpedrosa.smarthome.navigation.Devices as DevicesRoute
import com.vpedrosa.smarthome.navigation.Notifications as NotificationsRoute
import com.vpedrosa.smarthome.navigation.Rooms
import com.vpedrosa.smarthome.navigation.Settings as SettingsRoute

enum class BottomBarTab(
    val label: String,
    val icon: ImageVector,
    val route: Any,
) {
    DASHBOARD("Inicio", Icons.Default.Home, Dashboard),
    ROOMS("Habitaciones", Icons.Default.MeetingRoom, Rooms),
    DEVICES("Dispositivos", Icons.Default.Devices, DevicesRoute),
    NOTIFICATIONS("Notificaciones", Icons.Default.Notifications, NotificationsRoute),
    SETTINGS("Ajustes", Icons.Default.Settings, SettingsRoute),
}

@Composable
fun SmartHomeBottomBar(
    currentRoute: String?,
    onTabSelected: (BottomBarTab) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        BottomBarTab.entries.forEach { tab ->
            val selected = currentRoute == tab.route::class.qualifiedName
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                    )
                },
                label = { Text(text = tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                ),
            )
        }
    }
}
