package com.example.railswitch.logic

import com.example.railswitch.model.Block
import com.example.railswitch.model.Difficulty
import com.example.railswitch.model.GameState
import com.example.railswitch.model.Rail
import kotlin.math.max
import kotlin.math.min

/**
 * Core game loop engine for RailSwitch.
 * Decoupled from UI and Canvas rendering for pure testability.
 */
class GameEngine(
    difficulty: Difficulty = Difficulty.MEDIUM,
    private val spawner: BlockSpawner = BlockSpawner()
) {
    var state: GameState = GameState(
        difficulty = difficulty,
        currentSpeed = difficulty.initialSpeed,
        spawnIntervalMs = difficulty.initialSpawnIntervalMs
    )
        private set

    private var switchElapsedMs: Long = 0L
    private var timeSinceLastSpawnMs: Long = 0L

    fun startNewGame(difficulty: Difficulty = state.difficulty) {
        spawner.reset()
        switchElapsedMs = 0L
        timeSinceLastSpawnMs = 0L
        state = GameState(
            currentRail = Rail.LEFT,
            targetRail = Rail.LEFT,
            railProgress = 0f,
            isSwitching = false,
            blocks = emptyList(),
            score = 0,
            streak = 0,
            maxStreak = 0,
            blocksAvoided = 0,
            difficulty = difficulty,
            currentSpeed = difficulty.initialSpeed,
            spawnIntervalMs = difficulty.initialSpawnIntervalMs,
            isGameOver = false,
            isPaused = false,
            justDodged = false,
            lastAddedScore = 0,
            shakeIntensity = 0f,
            comboMilestone = null
        )
    }

    /**
     * Initiates rail switch to the opposite rail.
     * Returns true if switch started, false if ignored due to ongoing transition or game over.
     */
    fun switchRail(): Boolean {
        if (state.isGameOver || state.isPaused) return false
        if (state.isSwitching) return false // Ignore input during short switching animation

        val nextRail = state.currentRail.opposite()
        switchElapsedMs = 0L
        state = state.copy(
            targetRail = nextRail,
            isSwitching = true
        )
        return true
    }

    /**
     * Explicit switch toward a specific direction (e.g. from swipe gestures).
     */
    fun switchToRail(target: Rail): Boolean {
        if (state.isGameOver || state.isPaused) return false
        if (state.isSwitching) return false
        if (state.currentRail == target) return false

        return switchRail()
    }

    fun setPaused(paused: Boolean) {
        if (!state.isGameOver) {
            state = state.copy(isPaused = paused)
        }
    }

    /**
     * Primary frame update tick.
     * @param deltaSeconds time elapsed since previous tick (e.g. 0.016s for 60fps)
     */
    fun update(deltaSeconds: Float) {
        if (state.isGameOver || state.isPaused) return

        var currentState = state
        var justDodged = false
        var lastAddedScore = 0
        var comboMilestone: Int? = null

        // 1. Process Rail Switching Animation
        var currentRail = currentState.currentRail
        var targetRail = currentState.targetRail
        var railProgress = currentState.railProgress
        var isSwitching = currentState.isSwitching

        if (isSwitching) {
            switchElapsedMs += (deltaSeconds * 1000f).toLong()
            val duration = currentState.difficulty.switchDurationMs.toFloat()
            val t = (switchElapsedMs / duration).coerceIn(0f, 1f)

            // Smooth ease-out curve for responsive feel
            val eased = 1f - (1f - t) * (1f - t)

            railProgress = if (targetRail == Rail.RIGHT) {
                eased
            } else {
                1f - eased
            }

            if (t >= 1f) {
                isSwitching = false
                currentRail = targetRail
                railProgress = if (currentRail == Rail.RIGHT) 1f else 0f
            }
        }

        // 2. Spawn Block Checking
        timeSinceLastSpawnMs += (deltaSeconds * 1000f).toLong()
        val blocks = currentState.blocks.toMutableList()

        if (timeSinceLastSpawnMs >= currentState.spawnIntervalMs || blocks.isEmpty()) {
            val newBlock = spawner.trySpawnBlock(
                existingBlocks = blocks,
                difficulty = currentState.difficulty
            )
            if (newBlock != null) {
                blocks.add(newBlock)
                timeSinceLastSpawnMs = 0L
            }
        }

        // 3. Move Blocks and Collision / Avoidance Detection
        val playerTop = currentState.playerY - currentState.playerRadius
        val playerBottom = currentState.playerY + currentState.playerRadius
        var score = currentState.score
        var streak = currentState.streak
        var maxStreak = currentState.maxStreak
        var blocksAvoided = currentState.blocksAvoided
        var currentSpeed = currentState.currentSpeed
        var spawnIntervalMs = currentState.spawnIntervalMs
        var isGameOver = false
        var shakeIntensity = max(0f, currentState.shakeIntensity - deltaSeconds * 3.5f)

        val updatedBlocks = mutableListOf<Block>()

        for (block in blocks) {
            val newY = block.y + currentSpeed * deltaSeconds
            val updatedBlock = block.copy(y = newY)

            // Collision check: overlapping vertically with player
            val overlapsY = updatedBlock.top <= playerBottom && updatedBlock.bottom >= playerTop
            if (overlapsY && !updatedBlock.passed) {
                // Determine player's effective rail occupancy during transition
                val playerOnSameRail = when (updatedBlock.rail) {
                    Rail.LEFT -> railProgress < 0.6f
                    Rail.RIGHT -> railProgress > 0.4f
                }

                if (playerOnSameRail) {
                    // Collision occurred!
                    isGameOver = true
                    shakeIntensity = 1.0f
                    updatedBlocks.add(updatedBlock)
                    break
                }
            }

            // Check if block safely passed below player
            if (updatedBlock.top > playerBottom && !updatedBlock.scored) {
                val scoredBlock = updatedBlock.copy(passed = true, scored = true)
                updatedBlocks.add(scoredBlock)

                // Scoring calculation
                blocksAvoided++
                streak++
                if (streak > maxStreak) {
                    maxStreak = streak
                }

                // Base 100 + streak multiplier bonus
                val streakBonus = (streak - 1) * 15
                val pointsWon = 100 + streakBonus
                score += pointsWon
                lastAddedScore = pointsWon
                justDodged = true

                // Check streak milestones (every 5)
                if (streak % 5 == 0) {
                    comboMilestone = streak
                    score += 50 * streak // Extra milestone bonus
                }

                // Difficulty progression: gradually increase speed and decrease spawn interval
                currentSpeed = min(
                    currentState.difficulty.maxSpeed,
                    currentSpeed + currentState.difficulty.speedIncrement
                )
                spawnIntervalMs = max(
                    currentState.difficulty.minSpawnIntervalMs,
                    spawnIntervalMs - currentState.difficulty.spawnIntervalReductionMs
                )

                // Requirement: "After each successful avoidance, spawn another block."
                // Accelerate next spawn opportunity if no block is currently high up
                val highestBlock = updatedBlocks.minByOrNull { it.y }
                if (highestBlock == null || highestBlock.y > 120f) {
                    timeSinceLastSpawnMs = max(timeSinceLastSpawnMs, spawnIntervalMs - 150L)
                }
            } else if (updatedBlock.y < 1100f) {
                // Keep blocks on screen
                updatedBlocks.add(updatedBlock)
            }
        }

        state = currentState.copy(
            currentRail = currentRail,
            targetRail = targetRail,
            railProgress = railProgress,
            isSwitching = isSwitching,
            blocks = if (isGameOver) blocks else updatedBlocks,
            score = score,
            streak = if (isGameOver) 0 else streak,
            maxStreak = maxStreak,
            blocksAvoided = blocksAvoided,
            currentSpeed = currentSpeed,
            spawnIntervalMs = spawnIntervalMs,
            isGameOver = isGameOver,
            justDodged = justDodged,
            lastAddedScore = lastAddedScore,
            shakeIntensity = shakeIntensity,
            comboMilestone = comboMilestone
        )
    }

    /**
     * Calculates the horizontal X position (0.0f..1.0f or scaled to canvasWidth)
     * based on leftRailX, rightRailX and railProgress.
     */
    fun calculatePlayerX(leftRailX: Float, rightRailX: Float): Float {
        return leftRailX + (rightRailX - leftRailX) * state.railProgress
    }
}
