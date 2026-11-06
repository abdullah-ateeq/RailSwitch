package com.example.edgepulse.model

import kotlin.math.max
import kotlin.math.min

/**
 * Common interface for all hazards in EdgePulse.
 */
sealed interface Hazard {
    val id: Long
    val spawnTimeSec: Float
    val telegraphDurationSec: Float
    val totalLifetimeSec: Float

    fun isActive(currentTimeSec: Float): Boolean {
        val elapsed = currentTimeSec - spawnTimeSec
        return elapsed >= telegraphDurationSec && elapsed <= totalLifetimeSec
    }

    fun isExpired(currentTimeSec: Float): Boolean {
        return (currentTimeSec - spawnTimeSec) > totalLifetimeSec
    }

    fun isTelegraphing(currentTimeSec: Float): Boolean {
        val elapsed = currentTimeSec - spawnTimeSec
        return elapsed in 0f..telegraphDurationSec
    }

    fun telegraphProgress(currentTimeSec: Float): Float {
        val elapsed = currentTimeSec - spawnTimeSec
        if (telegraphDurationSec <= 0f) return 1f
        return (elapsed / telegraphDurationSec).coerceIn(0f, 1f)
    }

    /**
     * Checks if the player at [playerPos] (normalized [0, 1)) collides with this hazard.
     */
    fun collidesWith(playerPos: Float, currentTimeSec: Float): Boolean
}

/**
 * A block hazard that moves along the perimeter of the square.
 *
 * @param startPos Initial center position on perimeter [0.0, 1.0).
 * @param length Fraction of perimeter length occupied (e.g., 0.08f).
 * @param speed Laps per second. Can be positive (CW), negative (CCW), or 0 (stationary block).
 */
data class PerimeterBlockHazard(
    override val id: Long,
    val startPos: Float,
    val length: Float,
    val speed: Float,
    override val spawnTimeSec: Float,
    override val telegraphDurationSec: Float,
    override val totalLifetimeSec: Float
) : Hazard {

    fun currentCenter(currentTimeSec: Float): Float {
        val activeElapsed = max(0f, currentTimeSec - (spawnTimeSec + telegraphDurationSec))
        return PerimeterGeometry.normalize(startPos + speed * activeElapsed)
    }

    override fun collidesWith(playerPos: Float, currentTimeSec: Float): Boolean {
        if (!isActive(currentTimeSec)) return false
        val center = currentCenter(currentTimeSec)
        val dist = PerimeterGeometry.shortestDistance(playerPos, center)
        val hitThreshold = (length / 2.0f) + Player.PLAYER_HITBOX_RADIUS
        return dist <= hitThreshold
    }
}

/**
 * A stationary surge segment on the perimeter that charges up and then pulses with lethal energy.
 *
 * @param startPos Starting perimeter coordinate [0, 1).
 * @param endPos Ending perimeter coordinate [0, 1).
 */
data class PerimeterSurgeHazard(
    override val id: Long,
    val startPos: Float,
    val endPos: Float,
    override val spawnTimeSec: Float,
    override val telegraphDurationSec: Float,
    override val totalLifetimeSec: Float
) : Hazard {

    override fun collidesWith(playerPos: Float, currentTimeSec: Float): Boolean {
        if (!isActive(currentTimeSec)) return false
        // Extend collision segment slightly by player hitbox radius
        val extendedStart = PerimeterGeometry.normalize(startPos - Player.PLAYER_HITBOX_RADIUS)
        val extendedEnd = PerimeterGeometry.normalize(endPos + Player.PLAYER_HITBOX_RADIUS)
        return PerimeterGeometry.isPointInSegment(playerPos, extendedStart, extendedEnd)
    }
}

/**
 * A geometric beam cutting through the arena connecting two perimeter contact points.
 * When active, the contact points on the perimeter become lethal zones.
 *
 * @param contactP1 First perimeter contact point [0, 1).
 * @param contactP2 Opposing perimeter contact point [0, 1).
 * @param contactRadius Lethal radius around each contact point.
 */
data class CrossBeamHazard(
    override val id: Long,
    val contactP1: Float,
    val contactP2: Float,
    val contactRadius: Float = 0.035f,
    override val spawnTimeSec: Float,
    override val telegraphDurationSec: Float,
    override val totalLifetimeSec: Float
) : Hazard {

    override fun collidesWith(playerPos: Float, currentTimeSec: Float): Boolean {
        if (!isActive(currentTimeSec)) return false
        val dist1 = PerimeterGeometry.shortestDistance(playerPos, contactP1)
        val dist2 = PerimeterGeometry.shortestDistance(playerPos, contactP2)
        val threshold = contactRadius + Player.PLAYER_HITBOX_RADIUS
        return dist1 <= threshold || dist2 <= threshold
    }
}
