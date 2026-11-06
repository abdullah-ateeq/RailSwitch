package com.example.railswitch.model

enum class AppTheme(val label: String) {
    SYSTEM("System Default"),
    DARK("Dark Theme"),
    LIGHT("Light Theme");

    companion object {
        fun fromName(name: String): AppTheme =
            entries.find { it.name.equals(name, ignoreCase = true) } ?: DARK
    }
}

data class GameSettings(
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val theme: AppTheme = AppTheme.DARK
)

data class GameStats(
    val bestScore: Int = 0,
    val highestStreak: Int = 0,
    val totalGamesPlayed: Int = 0,
    val totalBlocksAvoided: Int = 0
)
