package com.example.railswitch.data

import android.content.Context
import android.content.SharedPreferences
import com.example.railswitch.model.AppTheme
import com.example.railswitch.model.Difficulty
import com.example.railswitch.model.GameSettings
import com.example.railswitch.model.GameStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages persistent storage for game settings and statistics.
 * Uses SharedPreferences with reactive StateFlow streams.
 */
class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("rail_switch_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<GameSettings> = _settings.asStateFlow()

    private val _stats = MutableStateFlow(loadStats())
    val stats: StateFlow<GameStats> = _stats.asStateFlow()

    private fun loadSettings(): GameSettings {
        val sound = prefs.getBoolean(KEY_SOUND, true)
        val haptics = prefs.getBoolean(KEY_HAPTICS, true)
        val diffName = prefs.getString(KEY_DIFFICULTY, Difficulty.MEDIUM.name) ?: Difficulty.MEDIUM.name
        val themeName = prefs.getString(KEY_THEME, AppTheme.DARK.name) ?: AppTheme.DARK.name

        return GameSettings(
            soundEnabled = sound,
            hapticsEnabled = haptics,
            difficulty = Difficulty.fromName(diffName),
            theme = AppTheme.fromName(themeName)
        )
    }

    private fun loadStats(): GameStats {
        val best = prefs.getInt(KEY_BEST_SCORE, 0)
        val streak = prefs.getInt(KEY_HIGHEST_STREAK, 0)
        val games = prefs.getInt(KEY_TOTAL_GAMES, 0)
        val avoided = prefs.getInt(KEY_TOTAL_AVOIDED, 0)

        return GameStats(
            bestScore = best,
            highestStreak = streak,
            totalGamesPlayed = games,
            totalBlocksAvoided = avoided
        )
    }

    fun updateSound(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND, enabled).apply()
        _settings.value = _settings.value.copy(soundEnabled = enabled)
    }

    fun updateHaptics(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTICS, enabled).apply()
        _settings.value = _settings.value.copy(hapticsEnabled = enabled)
    }

    fun updateDifficulty(difficulty: Difficulty) {
        prefs.edit().putString(KEY_DIFFICULTY, difficulty.name).apply()
        _settings.value = _settings.value.copy(difficulty = difficulty)
    }

    fun updateTheme(theme: AppTheme) {
        prefs.edit().putString(KEY_THEME, theme.name).apply()
        _settings.value = _settings.value.copy(theme = theme)
    }

    /**
     * Records the outcome of a finished run.
     * Returns true if a new all-time best score was set.
     */
    fun recordGameResult(score: Int, streak: Int, blocksAvoided: Int): Boolean {
        val current = _stats.value
        val isNewBest = score > current.bestScore
        val newBest = if (isNewBest) score else current.bestScore
        val newStreak = maxOf(current.highestStreak, streak)
        val newGames = current.totalGamesPlayed + 1
        val newAvoided = current.totalBlocksAvoided + blocksAvoided

        prefs.edit()
            .putInt(KEY_BEST_SCORE, newBest)
            .putInt(KEY_HIGHEST_STREAK, newStreak)
            .putInt(KEY_TOTAL_GAMES, newGames)
            .putInt(KEY_TOTAL_AVOIDED, newAvoided)
            .apply()

        _stats.value = GameStats(
            bestScore = newBest,
            highestStreak = newStreak,
            totalGamesPlayed = newGames,
            totalBlocksAvoided = newAvoided
        )

        return isNewBest
    }

    fun resetStatistics() {
        prefs.edit()
            .remove(KEY_BEST_SCORE)
            .remove(KEY_HIGHEST_STREAK)
            .remove(KEY_TOTAL_GAMES)
            .remove(KEY_TOTAL_AVOIDED)
            .apply()

        _stats.value = GameStats()
    }

    companion object {
        private const val KEY_SOUND = "key_sound"
        private const val KEY_HAPTICS = "key_haptics"
        private const val KEY_DIFFICULTY = "key_difficulty"
        private const val KEY_THEME = "key_theme"
        private const val KEY_BEST_SCORE = "key_best_score"
        private const val KEY_HIGHEST_STREAK = "key_highest_streak"
        private const val KEY_TOTAL_GAMES = "key_total_games"
        private const val KEY_TOTAL_AVOIDED = "key_total_avoided"
    }
}
