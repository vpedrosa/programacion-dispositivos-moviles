package com.vpedrosa.smarthome.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.vpedrosa.smarthome.ui.screens.AntiSquatterScreen
import com.vpedrosa.smarthome.ui.screens.DashboardScreen
import com.vpedrosa.smarthome.ui.screens.DeviceDetailScreen
import com.vpedrosa.smarthome.ui.screens.DevicesScreen
import com.vpedrosa.smarthome.ui.screens.EditGroupScreen
import com.vpedrosa.smarthome.ui.screens.NotificationsScreen
import com.vpedrosa.smarthome.ui.screens.RoomsScreen
import com.vpedrosa.smarthome.ui.screens.SettingsScreen
import com.vpedrosa.smarthome.ui.screens.SplashScreen
import com.vpedrosa.smarthome.ui.screens.VoiceControlScreen

@Composable
fun SmartHomeNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Splash,
        modifier = modifier,
    ) {
        composable<Splash> {
            SplashScreen(
                onNavigateToDashboard = {
                    navController.navigate(Dashboard) {
                        popUpTo<Splash> { inclusive = true }
                    }
                },
            )
        }

        composable<Dashboard> {
            DashboardScreen()
        }

        composable<Rooms> {
            RoomsScreen()
        }

        composable<EditGroup> {
            EditGroupScreen()
        }

        composable<Devices> {
            DevicesScreen()
        }

        composable<DeviceDetail> { backStackEntry ->
            val detail = backStackEntry.toRoute<DeviceDetail>()
            DeviceDetailScreen(deviceId = detail.deviceId)
        }

        composable<Notifications> {
            NotificationsScreen()
        }

        composable<AntiSquatter> {
            AntiSquatterScreen()
        }

        composable<VoiceControl> {
            VoiceControlScreen()
        }

        composable<Settings> {
            SettingsScreen()
        }
    }
}
