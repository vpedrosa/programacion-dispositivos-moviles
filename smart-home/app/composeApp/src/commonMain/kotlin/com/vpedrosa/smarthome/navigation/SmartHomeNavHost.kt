package com.vpedrosa.smarthome.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.vpedrosa.smarthome.ui.screens.CommissioningScreen
import com.vpedrosa.smarthome.ui.screens.AntiSquatterScreen
import com.vpedrosa.smarthome.ui.screens.DashboardScreen
import com.vpedrosa.smarthome.ui.screens.DeviceDetailScreen
import com.vpedrosa.smarthome.ui.screens.DevicesScreen
import com.vpedrosa.smarthome.ui.screens.EditGroupScreen
import com.vpedrosa.smarthome.ui.screens.RoomDetailScreen
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
            DashboardScreen(
                onNavigateToCommissioning = {
                    navController.navigate(Commissioning)
                },
            )
        }

        composable<Rooms> {
            RoomsScreen(
                onNavigateToRoomDetail = { roomId ->
                    navController.navigate(RoomDetail(roomId = roomId))
                },
                onNavigateToEditGroup = { roomId ->
                    navController.navigate(EditGroup(roomId = roomId))
                },
                onNavigateToNewGroup = {
                    navController.navigate(EditGroup())
                },
            )
        }

        composable<RoomDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<RoomDetail>()
            RoomDetailScreen(
                roomId = route.roomId,
                onNavigateToDeviceDetail = { deviceId ->
                    navController.navigate(DeviceDetail(deviceId))
                },
                onNavigateToEditGroup = {
                    navController.navigate(EditGroup(roomId = route.roomId))
                },
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable<EditGroup> { backStackEntry ->
            val route = backStackEntry.toRoute<EditGroup>()
            EditGroupScreen(
                roomId = route.roomId,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable<Devices> {
            DevicesScreen(
                onNavigateToDeviceDetail = { deviceId ->
                    navController.navigate(DeviceDetail(deviceId))
                },
            )
        }

        composable<DeviceDetail> { backStackEntry ->
            val detail = backStackEntry.toRoute<DeviceDetail>()
            DeviceDetailScreen(
                deviceId = detail.deviceId,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable<Notifications> {
            NotificationsScreen()
        }

        composable<AntiSquatter> {
            AntiSquatterScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable<VoiceControl> {
            VoiceControlScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable<Commissioning> {
            CommissioningScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable<Settings> {
            SettingsScreen(
                onNavigateToAntiSquatter = {
                    navController.navigate(AntiSquatter)
                },
                onNavigateToCommissioning = {
                    navController.navigate(Commissioning)
                },
            )
        }
    }
}
