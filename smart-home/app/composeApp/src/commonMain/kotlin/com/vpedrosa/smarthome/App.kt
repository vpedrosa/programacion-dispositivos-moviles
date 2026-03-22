package com.vpedrosa.smarthome

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vpedrosa.smarthome.navigation.Dashboard
import com.vpedrosa.smarthome.navigation.Devices
import com.vpedrosa.smarthome.navigation.Notifications
import com.vpedrosa.smarthome.navigation.Rooms
import com.vpedrosa.smarthome.navigation.Settings
import com.vpedrosa.smarthome.navigation.SmartHomeNavHost
import com.vpedrosa.smarthome.ui.components.BottomBarTab
import com.vpedrosa.smarthome.event.domain.BackgroundSimulatorPort
import com.vpedrosa.smarthome.ui.components.NotificationPermissionHandler
import com.vpedrosa.smarthome.ui.components.SmartHomeBottomBar
import com.vpedrosa.smarthome.ui.theme.Black
import com.vpedrosa.smarthome.ui.theme.Linen
import com.vpedrosa.smarthome.ui.theme.Navy
import com.vpedrosa.smarthome.ui.theme.WarmGray
import org.koin.compose.koinInject

private val SmartHomeColorScheme = darkColorScheme(
    primary = Navy,
    secondary = WarmGray,
    background = Linen,
    surface = Linen,
    onPrimary = Linen,
    onSecondary = Black,
    onBackground = Black,
    onSurface = Black,
)

private val screensWithBottomBar = setOf(
    Dashboard::class.qualifiedName,
    Rooms::class.qualifiedName,
    Devices::class.qualifiedName,
    Notifications::class.qualifiedName,
    Settings::class.qualifiedName,
)

@Composable
fun App() {
    val simulator = koinInject<BackgroundSimulatorPort>()
    LaunchedEffect(Unit) { simulator.start() }

    // Request notification permission at startup (Android 13+)
    NotificationPermissionHandler(onPermissionResult = {}) { _, requestPermission ->
        LaunchedEffect(Unit) { requestPermission() }
    }

    MaterialTheme(colorScheme = SmartHomeColorScheme) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val showBottomBar = currentRoute in screensWithBottomBar

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (showBottomBar) {
                    SmartHomeBottomBar(
                        currentRoute = currentRoute,
                        onTabSelected = { tab ->
                            navController.navigate(tab.route) {
                                popUpTo<Dashboard> { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            },
        ) { innerPadding ->
            SmartHomeNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

