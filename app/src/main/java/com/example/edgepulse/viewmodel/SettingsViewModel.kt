package com.example.edgepulse.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.edgepulse.data.AppThemeMode
import com.example.edgepulse.data.GamePreferences
import com.example.edgepulse.data.GameSettings
import com.example.edgepulse.data.GameStats
import com.example.edgepulse.model.Difficulty
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = GamePreferences(application)

    val settings: StateFlow<GameSettings> = preferences.settingsFlow
    val stats: StateFlow<GameStats> = preferences.statsFlow

    fun setThemeMode(themeMode: AppThemeMode) {
        preferences.updateSettings(themeMode = themeMode)
    }

    fun setSoundEnabled(enabled: Boolean) {
        preferences.updateSettings(sound = enabled)
    }

    fun setHapticsEnabled(enabled: Boolean) {
        preferences.updateSettings(haptics = enabled)
    }

    fun setDifficulty(difficulty: Difficulty) {
        preferences.updateSettings(difficulty = difficulty)
    }

    fun resetStatistics() {
        preferences.resetStatistics()
    }
}
