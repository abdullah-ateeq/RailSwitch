package com.example.railswitch.model

/**
 * Difficulty levels balancing block speed, spawn frequency, and spacing.
 */
enum class Difficulty(
    val title: String,
    val description: String,
    val initialSpeed: Float,         // virtual height units per sec (virtual height = 1000)
    val maxSpeed: Float,
    val speedIncrement: Float,       // increase per avoided block
    val initialSpawnIntervalMs: Long,
    val minSpawnIntervalMs: Long,
    val spawnIntervalReductionMs: Long,
    val minSafeGapY: Float,          // minimum vertical spacing between consecutive opposing blocks
    val switchDurationMs: Long       // switching transition time
) {
    EASY(
        title = "Easy",
        description = "Slower blocks, wider spacing, relaxed pace",
        initialSpeed = 340f,
        maxSpeed = 580f,
        speedIncrement = 3.5f,
        initialSpawnIntervalMs = 1600L,
        minSpawnIntervalMs = 1100L,
        spawnIntervalReductionMs = 15L,
        minSafeGapY = 280f,
        switchDurationMs = 140L
    ),
    MEDIUM(
        title = "Medium",
        description = "Faster tempo, varied patterns, standard challenge",
        initialSpeed = 480f,
        maxSpeed = 760f,
        speedIncrement = 5.5f,
        initialSpawnIntervalMs = 1250L,
        minSpawnIntervalMs = 780L,
        spawnIntervalReductionMs = 18L,
        minSafeGapY = 220f,
        switchDurationMs = 120L
    ),
    HARD(
        title = "Hard",
        description = "High speed, fast reflex transitions, razor precision",
        initialSpeed = 620f,
        maxSpeed = 960f,
        speedIncrement = 7.5f,
        initialSpawnIntervalMs = 950L,
        minSpawnIntervalMs = 560L,
        spawnIntervalReductionMs = 22L,
        minSafeGapY = 175f,
        switchDurationMs = 100L
    );

    companion object {
        fun fromName(name: String, default: Difficulty = MEDIUM): Difficulty {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: default
        }
    }
}
