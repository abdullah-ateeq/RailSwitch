package com.example.railswitch.model

/**
 * Complete snapshot of the game state at any frame.
 */
data class GameState(
    val currentRail: Rail = Rail.LEFT,
    val targetRail: Rail = Rail.LEFT,
    val railProgress: Float = 0f, // 0.0f = LEFT, 1.0f = RIGHT
    val isSwitching: Boolean = false,
    val blocks: List<Block> = emptyList(),
    val score: Int = 0,
    val streak: Int = 0,
    val maxStreak: Int = 0,
    val blocksAvoided: Int = 0,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val currentSpeed: Float = Difficulty.MEDIUM.initialSpeed,
    val spawnIntervalMs: Long = Difficulty.MEDIUM.initialSpawnIntervalMs,
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false,
    val justDodged: Boolean = false,
    val lastAddedScore: Int = 0,
    val shakeIntensity: Float = 0f,
    val comboMilestone: Int? = null,
    val playerY: Float = 820f, // Virtual Y position of the player (out of 1000f)
    val playerRadius: Float = 22f
)
