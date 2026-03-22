package com.vpedrosa.smarthome.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.vpedrosa.smarthome.wear.ui.device_control.DeviceControlScreen
import com.vpedrosa.smarthome.wear.ui.device_control.DeviceControlViewModel
import com.vpedrosa.smarthome.wear.theme.SmartHomeWearTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val deviceControlViewModel: DeviceControlViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SmartHomeWearTheme {
                DeviceControlScreen(viewModel = deviceControlViewModel)
            }
        }
    }
}
