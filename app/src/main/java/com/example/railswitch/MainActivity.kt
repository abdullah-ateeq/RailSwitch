package com.example.railswitch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.railswitch.audio.HapticsManager
import com.example.railswitch.audio.SoundManager
import com.example.railswitch.data.PreferencesManager
import com.example.railswitch.ui.navigation.RailSwitchNavHost
import com.example.railswitch.ui.theme.BackgroundBlack
import com.example.railswitch.ui.theme.RailSwitchTheme

class MainActivity : ComponentActivity() {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var soundManager: SoundManager
    private lateinit var hapticsManager: HapticsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferencesManager = PreferencesManager(applicationContext)
        soundManager = SoundManager()
        hapticsManager = HapticsManager(applicationContext)

        setContent {
            val settings by preferencesManager.settings.collectAsState()

            RailSwitchTheme(themePreference = settings.theme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundBlack
                ) {
                    RailSwitchNavHost(
                        preferencesManager = preferencesManager,
                        soundManager = soundManager,
                        hapticsManager = hapticsManager
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}
