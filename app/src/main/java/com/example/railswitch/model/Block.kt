package com.example.railswitch.model

/**
 * Geometric obstacle moving downward along one of the two rails.
 * Coordinates are in virtual space (virtual height = 1000f).
 */
data class Block(
    val id: Long,
    val rail: Rail,
    val y: Float,
    val height: Float = 55f,
    val passed: Boolean = false,
    val scored: Boolean = false
) {
    val top: Float get() = y
    val bottom: Float get() = y + height
    val centerY: Float get() = y + height / 2f
}
