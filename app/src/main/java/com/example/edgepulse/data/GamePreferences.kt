package com.example.edgepulse.data

import android.content.Context
import android.content.SharedPreferences
import com.example.edgepulse.model.Difficulty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode {
    SYSTEM,
    DARK,
    LIGHT
}

data class GameSettings(
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val isSoundEnabled: Boolean = true,
    val isHapticsEnabled: Boolean = true,
    val preferredDifficulty: Difficulty = Difficulty.MEDIUM
)

data class GameStats(
    val bestScore: Int = 0,
    val bestLaps: Int = 0,
    val longestSurvivalSec: Float = 0.0f,
    val gamesPlayed: Int = 0,
    val totalLapsCompleted: Int = 0
)

class GamePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("edgepulse_prefs", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<GameSettings> = _settingsFlow.asStateFlow()

    private val _statsFlow = MutableStateFlow(loadStats())
    val statsFlow: StateFlow<GameStats> = _statsFlow.asStateFlow()

    fun getSettings(): GameSettings = _settingsFlow.value

    fun updateSettings(
        themeMode: AppThemeMode? = null,
        sound: Boolean? = null,
        haptics: Boolean? = null,
        difficulty: Difficulty? = null
    ) {
        val current = _settingsFlow.value
        val updated = current.copy(
            themeMode = themeMode ?: current.themeMode,
            isSoundEnabled = sound ?: current.isSoundEnabled,
            isHapticsEnabled = haptics ?: current.isHapticsEnabled,
            preferredDifficulty = difficulty ?: current.preferredDifficulty
        )

        prefs.edit().apply {
            putString(KEY_THEME_MODE, updated.themeMode.name)
            putBoolean(KEY_SOUND, updated.isSoundEnabled)
            putBoolean(KEY_HAPTICS, updated.isHapticsEnabled)
            putString(KEY_DIFFICULTY, updated.preferredDifficulty.name)
            apply()
        }

        _settingsFlow.value = updated
    }

    /**
     * Records a completed game run. Returns true if a new all-time high score was set.
     */
    fun recordGameResult(
        difficulty: Difficulty,
        score: Int,
        laps: Int,
        survivalTimeSec: Float
    ): Boolean {
        val currentStats = _statsFlow.value
        val isNewBestScore = score > currentStats.bestScore
        val newBestScore = maxOf(currentStats.bestScore, score)
        val newBestLaps = maxOf(currentStats.bestLaps, laps)
        val newLongestSurvival = maxOf(currentStats.longestSurvivalSec, survivalTimeSec)
        val newGamesPlayed = currentStats.gamesPlayed + 1
        val newTotalLaps = currentStats.totalLapsCompleted + laps

        // Also record per-difficulty high scores
        val diffKey = difficulty.name
        val diffBestScore = maxOf(prefs.getInt("${KEY_DIFF_SCORE}_$diffKey", 0), score)
        val diffBestLaps = maxOf(prefs.getInt("${KEY_DIFF_LAPS}_$diffKey", 0), laps)
        val diffLongestTime = maxOf(prefs.getFloat("${KEY_DIFF_TIME}_$diffKey", 0.0f), survivalTimeSec)

        prefs.edit().apply {
            putInt(KEY_BEST_SCORE, newBestScore)
            putInt(KEY_BEST_LAPS, newBestLaps)
            putFloat(KEY_LONGEST_SURVIVAL, newLongestSurvival)
            putInt(KEY_GAMES_PLAYED, newGamesPlayed)
            putInt(KEY_TOTAL_LAPS, newTotalLaps)

            putInt("${KEY_DIFF_SCORE}_$diffKey", diffBestScore)
            putInt("${KEY_DIFF_LAPS}_$diffKey", diffBestLaps)
            putFloat("${KEY_DIFF_TIME}_$diffKey", diffLongestTime)
            apply()
        }

        val updatedStats = GameStats(
            bestScore = newBestScore,
            bestLaps = newBestLaps,
            longestSurvivalSec = newLongestSurvival,
            gamesPlayed = newGamesPlayed,
            totalLapsCompleted = newTotalLaps
        )
        _statsFlow.value = updatedStats
        return isNewBestScore
    }

    fun resetStatistics() {
        prefs.edit().apply {
            remove(KEY_BEST_SCORE)
            remove(KEY_BEST_LAPS)
            remove(KEY_LONGEST_SURVIVAL)
            remove(KEY_GAMES_PLAYED)
            remove(KEY_TOTAL_LAPS)
            for (diff in Difficulty.entries) {
                remove("${KEY_DIFF_SCORE}_${diff.name}")
                remove("${KEY_DIFF_LAPS}_${diff.name}")
                remove("${KEY_DIFF_TIME}_${diff.name}")
            }
            apply()
        }
        _statsFlow.value = GameStats()
    }

    private fun loadSettings(): GameSettings {
        val themeStr = prefs.getString(KEY_THEME_MODE, AppThemeMode.DARK.name)
        val themeMode = try {
            AppThemeMode.valueOf(themeStr ?: AppThemeMode.DARK.name)
        } catch (_: Exception) {
            AppThemeMode.DARK
        }

        val sound = prefs.getBoolean(KEY_SOUND, true)
        val haptics = prefs.getBoolean(KEY_HAPTICS, true)
        val diffStr = prefs.getString(KEY_DIFFICULTY, Difficulty.MEDIUM.name)
        val diff = Difficulty.fromString(diffStr)

        return GameSettings(
            themeMode = themeMode,
            isSoundEnabled = sound,
            isHapticsEnabled = haptics,
            preferredDifficulty = diff
        )
    }

    private fun loadStats(): GameStats {
        return GameStats(
            bestScore = prefs.getInt(KEY_BEST_SCORE, 0),
            bestLaps = prefs.getInt(KEY_BEST_LAPS, 0),
            longestSurvivalSec = prefs.getFloat(KEY_LONGEST_SURVIVAL, 0.0f),
            gamesPlayed = prefs.getInt(KEY_GAMES_PLAYED, 0),
            totalLapsCompleted = prefs.getInt(KEY_TOTAL_LAPS, 0)
        )
    }

    companion object {
        private const val KEY_THEME_MODE = "pref_theme_mode"
        private const val KEY_SOUND = "pref_sound"
        private const val KEY_HAPTICS = "pref_haptics"
        private const val KEY_DIFFICULTY = "pref_difficulty"

        private const val KEY_BEST_SCORE = "stat_best_score"
        private const val KEY_BEST_LAPS = "stat_best_laps"
        private const val KEY_LONGEST_SURVIVAL = "stat_longest_survival"
        private const val KEY_GAMES_PLAYED = "stat_games_played"
        private const val KEY_TOTAL_LAPS = "stat_total_laps"

        private const val KEY_DIFF_SCORE = "stat_diff_score"
        private const val KEY_DIFF_LAPS = "stat_diff_laps"
        private const val KEY_DIFF_TIME = "stat_diff_time"
    }
}
