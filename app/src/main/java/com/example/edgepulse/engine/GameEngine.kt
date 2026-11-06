package com.example.edgepulse.engine

import com.example.edgepulse.model.CrossBeamHazard
import com.example.edgepulse.model.Difficulty
import com.example.edgepulse.model.GameState
import com.example.edgepulse.model.GameStatus
import com.example.edgepulse.model.Hazard
import com.example.edgepulse.model.PerimeterBlockHazard
import com.example.edgepulse.model.PerimeterGeometry
import com.example.edgepulse.model.PerimeterSurgeHazard
import com.example.edgepulse.model.Player
import kotlin.math.abs
import kotlin.math.min
import kotlin.random.Random

data class GameStepResult(
    val state: GameState,
    val lapCompleted: Boolean = false,
    val collisionOccurred: Boolean = false
)

class GameEngine(
    val difficulty: Difficulty = Difficulty.MEDIUM,
    private val random: Random = Random.Default,
    private val spawnHazards: Boolean = true
) {
    private var currentState: GameState = GameState(
        status = GameStatus.READY,
        difficulty = difficulty,
        player = Player(
            position = 0.0f,
            direction = 1,
            speed = difficulty.initialPlayerSpeed
        )
    )

    private var nextHazardId: Long = 1L
    private var nextSpawnTimeSec: Float = 1.0f
    private var fractionalScoreAccumulator: Float = 0.0f

    val state: GameState
        get() = currentState

    fun start() {
        currentState = GameState(
            status = GameStatus.RUNNING,
            difficulty = difficulty,
            player = Player(
                position = 0.0f,
                direction = 1,
                speed = difficulty.initialPlayerSpeed
            ),
            survivalTimeSec = 0.0f
        )
        nextSpawnTimeSec = randomSpawnInterval(0.8f, 1.6f)
        fractionalScoreAccumulator = 0.0f
    }

    fun pause() {
        if (currentState.status == GameStatus.RUNNING) {
            currentState = currentState.copy(status = GameStatus.PAUSED)
        }
    }

    fun resume() {
        if (currentState.status == GameStatus.PAUSED) {
            currentState = currentState.copy(status = GameStatus.RUNNING)
        }
    }

    fun reverseDirection(): Boolean {
        if (currentState.status != GameStatus.RUNNING) return false
        val player = currentState.player
        val currentTime = currentState.survivalTimeSec
        if (!player.canReverse(currentTime)) return false

        currentState = currentState.copy(
            player = player.reverse(currentTime)
        )
        return true
    }

    fun update(dt: Float): GameStepResult {
        if (currentState.status != GameStatus.RUNNING) {
            return GameStepResult(currentState)
        }

        val clampedDt = dt.coerceIn(0.001f, 0.1f) // Guard against large lag spikes
        val newTime = currentState.survivalTimeSec + clampedDt

        // 1. Calculate dynamic player speed with difficulty progression
        val currentSpeed = min(
            difficulty.maxPlayerSpeed,
            difficulty.initialPlayerSpeed + newTime * difficulty.speedGrowthRate
        )

        // 2. Advance player position
        val deltaP = currentState.player.direction * currentSpeed * clampedDt
        val newPos = PerimeterGeometry.normalize(currentState.player.position + deltaP)

        // 3. Update trail history
        val updatedTrail = (listOf(newPos) + currentState.player.trailPoints)
            .take(Player.TRAIL_MAX_POINTS)

        val updatedPlayer = currentState.player.copy(
            position = newPos,
            speed = currentSpeed,
            trailPoints = updatedTrail
        )

        // 4. Lap detection and scoring
        var newLapProgress = currentState.lapProgress + abs(deltaP)
        var lapsCompleted = currentState.lapsCompleted
        var consecutiveLaps = currentState.consecutiveLaps
        var lapCompletedThisFrame = false
        var scoreBonus = 0

        while (newLapProgress >= 1.0f) {
            newLapProgress -= 1.0f
            lapsCompleted++
            consecutiveLaps++
            lapCompletedThisFrame = true
            // Lap bonus: 100 + streak bonus (25 per consecutive lap)
            val streakBonus = (consecutiveLaps - 1) * 25
            scoreBonus += 100 + streakBonus
        }

        // 5. Survival time points: 10 points per second
        fractionalScoreAccumulator += clampedDt * 10f
        val timePoints = fractionalScoreAccumulator.toInt()
        if (timePoints > 0) {
            scoreBonus += timePoints
            fractionalScoreAccumulator -= timePoints
        }

        val newScore = currentState.score + scoreBonus

        // 6. Hazard lifecycle and spawning
        val activeHazards = currentState.hazards.filter { !it.isExpired(newTime) }.toMutableList()

        if (spawnHazards && newTime >= nextSpawnTimeSec && activeHazards.size < difficulty.maxConcurrentHazards) {
            val newHazard = generateFairHazard(newTime, newPos)
            if (newHazard != null) {
                activeHazards.add(newHazard)
            }
            nextSpawnTimeSec = newTime + randomSpawnInterval(
                difficulty.minSpawnIntervalSec,
                difficulty.maxSpawnIntervalSec
            )
        }

        // 7. Collision detection
        var collision = false
        for (hazard in activeHazards) {
            if (hazard.collidesWith(newPos, newTime)) {
                collision = true
                break
            }
        }

        val newStatus = if (collision) GameStatus.GAME_OVER else GameStatus.RUNNING

        currentState = currentState.copy(
            status = newStatus,
            player = updatedPlayer,
            hazards = activeHazards,
            score = newScore,
            survivalTimeSec = newTime,
            lapsCompleted = lapsCompleted,
            lapProgress = newLapProgress,
            consecutiveLaps = consecutiveLaps,
            lastLapPulseTime = if (lapCompletedThisFrame) newTime else currentState.lastLapPulseTime,
            lastImpactTime = if (collision) newTime else currentState.lastImpactTime
        )

        return GameStepResult(
            state = currentState,
            lapCompleted = lapCompletedThisFrame,
            collisionOccurred = collision
        )
    }

    /**
     * Generates a hazard with guaranteed fair reaction distance from the player's current position.
     */
    fun generateFairHazard(currentTimeSec: Float, playerPos: Float): Hazard? {
        val hazardType = random.nextInt(3)
        val telegraph = difficulty.telegraphDurationSec

        return when (hazardType) {
            0 -> {
                // PerimeterBlockHazard: Moving patrol block
                val length = 0.08f + random.nextFloat() * 0.04f
                // Pick a position sufficiently far from the player (at least 0.25 away)
                val offset = 0.25f + random.nextFloat() * 0.50f
                val spawnPos = PerimeterGeometry.normalize(playerPos + offset)
                val speed = (if (random.nextBoolean()) 1 else -1) *
                        (0.08f + random.nextFloat() * 0.08f) * difficulty.hazardSpeedMultiplier
                val lifetime = telegraph + 4.0f + random.nextFloat() * 2.0f

                PerimeterBlockHazard(
                    id = nextHazardId++,
                    startPos = spawnPos,
                    length = length,
                    speed = speed,
                    spawnTimeSec = currentTimeSec,
                    telegraphDurationSec = telegraph,
                    totalLifetimeSec = lifetime
                )
            }
            1 -> {
                // PerimeterSurgeHazard: Stationary dangerous edge section
                val sectorLength = 0.14f + random.nextFloat() * 0.06f
                // Spawn safely away from player
                val offset = 0.22f + random.nextFloat() * 0.56f
                val startPos = PerimeterGeometry.normalize(playerPos + offset)
                val endPos = PerimeterGeometry.normalize(startPos + sectorLength)
                val lifetime = telegraph + 2.2f + random.nextFloat() * 1.0f

                PerimeterSurgeHazard(
                    id = nextHazardId++,
                    startPos = startPos,
                    endPos = endPos,
                    spawnTimeSec = currentTimeSec,
                    telegraphDurationSec = telegraph,
                    totalLifetimeSec = lifetime
                )
            }
            else -> {
                // CrossBeamHazard: Slices across opposite edges
                // Pick a contact point on perimeter that is at least 0.20 away from player
                val offset = 0.20f + random.nextFloat() * 0.60f
                val contact1 = PerimeterGeometry.normalize(playerPos + offset)
                // Opposite side is roughly +0.50 around
                val contact2 = PerimeterGeometry.normalize(contact1 + 0.45f + random.nextFloat() * 0.10f)
                val lifetime = telegraph + 1.8f + random.nextFloat() * 1.0f

                CrossBeamHazard(
                    id = nextHazardId++,
                    contactP1 = contact1,
                    contactP2 = contact2,
                    contactRadius = 0.032f,
                    spawnTimeSec = currentTimeSec,
                    telegraphDurationSec = telegraph,
                    totalLifetimeSec = lifetime
                )
            }
        }
    }

    private fun randomSpawnInterval(minSec: Float, maxSec: Float): Float {
        return minSec + random.nextFloat() * (maxSec - minSec)
    }
}
