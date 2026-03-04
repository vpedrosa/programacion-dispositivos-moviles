package com.vpedrosa.smarthome.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.vpedrosa.smarthome.navigation.Dashboard
import com.vpedrosa.smarthome.navigation.Devices as DevicesRoute
import com.vpedrosa.smarthome.navigation.Notifications as NotificationsRoute
import com.vpedrosa.smarthome.navigation.Rooms
import com.vpedrosa.smarthome.navigation.Settings as SettingsRoute
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import smarthome.composeapp.generated.resources.Res
import smarthome.composeapp.generated.resources.nav_dashboard
import smarthome.composeapp.generated.resources.nav_devices
import smarthome.composeapp.generated.resources.nav_notifications
import smarthome.composeapp.generated.resources.nav_rooms
import smarthome.composeapp.generated.resources.nav_settings

enum class BottomBarTab(
    val labelRes: StringResource,
    val icon: ImageVector,
    val route: Any,
) {
    DASHBOARD(Res.string.nav_dashboard, Icons.Default.Home, Dashboard),
    ROOMS(Res.string.nav_rooms, Icons.Default.MeetingRoom, Rooms),
    DEVICES(Res.string.nav_devices, Icons.Default.Devices, DevicesRoute),
    NOTIFICATIONS(Res.string.nav_notifications, Icons.Default.Notifications, NotificationsRoute),
    SETTINGS(Res.string.nav_settings, Icons.Default.Settings, SettingsRoute),
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
            val label = stringResource(tab.labelRes)
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = label,
                    )
                },
                label = { Text(text = label) },
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
