package com.example.edgepulse.model

enum class GameStatus {
    READY,
    RUNNING,
    PAUSED,
    GAME_OVER
}

/**
 * Complete immutable snapshot of the gameplay state.
 */
data class GameState(
    val status: GameStatus = GameStatus.READY,
    val player: Player = Player(),
    val hazards: List<Hazard> = emptyList(),
    val score: Int = 0,
    val survivalTimeSec: Float = 0.0f,
    val lapsCompleted: Int = 0,
    val lapProgress: Float = 0.0f, // [0.0, 1.0) towards next lap
    val consecutiveLaps: Int = 0,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val lastLapPulseTime: Float = -1.0f,
    val lastImpactTime: Float = -1.0f,
    val isNewHighScore: Boolean = false
) {
    val isPlaying: Boolean
        get() = status == GameStatus.RUNNING

    val isGameOver: Boolean
        get() = status == GameStatus.GAME_OVER
}
