package com.example.edgepulse.model

/**
 * Player state on the square perimeter.
 *
 * @param position Normalized perimeter coordinate in [0.0, 1.0).
 * @param direction 1 for clockwise, -1 for counter-clockwise.
 * @param speed Laps per second.
 * @param lastReverseTime Monotonic game time (in seconds) of last reversal, used for cooldown.
 * @param trailPoints Ring of recent perimeter points for drawing the luminous neon trail.
 */
data class Player(
    val position: Float = 0.0f,
    val direction: Int = 1,
    val speed: Float = 0.30f,
    val lastReverseTime: Float = -1.0f,
    val trailPoints: List<Float> = emptyList()
) {
    companion object {
        const val REVERSE_COOLDOWN_SEC = 0.12f // Prevents accidental double-tap reversals
        const val TRAIL_MAX_POINTS = 16
        const val PLAYER_HITBOX_RADIUS = 0.022f // In normalized perimeter units
    }

    fun canReverse(currentTimeSec: Float): Boolean {
        return currentTimeSec - lastReverseTime >= REVERSE_COOLDOWN_SEC
    }

    fun reverse(currentTimeSec: Float): Player {
        return copy(
            direction = -direction,
            lastReverseTime = currentTimeSec
        )
    }
}
