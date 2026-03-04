package com.vpedrosa.smarthome.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.vpedrosa.smarthome.wear.theme.SmartHomeWearTheme
import com.vpedrosa.smarthome.wear.voice_control.VoiceControlScreen
import com.vpedrosa.smarthome.wear.voice_control.VoiceControlViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val voiceControlViewModel: VoiceControlViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SmartHomeWearTheme {
                VoiceControlScreen(viewModel = voiceControlViewModel)
            }
        }
    }
}
