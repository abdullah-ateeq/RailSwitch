package com.example.edgepulse.model

enum class Difficulty(
    val displayName: String,
    val initialPlayerSpeed: Float, // laps per second
    val maxPlayerSpeed: Float,
    val speedGrowthRate: Float, // speed increase per second survived
    val minSpawnIntervalSec: Float,
    val maxSpawnIntervalSec: Float,
    val telegraphDurationSec: Float, // fair reaction window
    val hazardSpeedMultiplier: Float,
    val maxConcurrentHazards: Int
) {
    EASY(
        displayName = "Easy",
        initialPlayerSpeed = 0.22f,
        maxPlayerSpeed = 0.38f,
        speedGrowthRate = 0.002f,
        minSpawnIntervalSec = 3.2f,
        maxSpawnIntervalSec = 4.5f,
        telegraphDurationSec = 1.5f,
        hazardSpeedMultiplier = 0.75f,
        maxConcurrentHazards = 2
    ),
    MEDIUM(
        displayName = "Medium",
        initialPlayerSpeed = 0.30f,
        maxPlayerSpeed = 0.50f,
        speedGrowthRate = 0.003f,
        minSpawnIntervalSec = 2.2f,
        maxSpawnIntervalSec = 3.2f,
        telegraphDurationSec = 1.15f,
        hazardSpeedMultiplier = 1.0f,
        maxConcurrentHazards = 3
    ),
    HARD(
        displayName = "Hard",
        initialPlayerSpeed = 0.40f,
        maxPlayerSpeed = 0.65f,
        speedGrowthRate = 0.0045f,
        minSpawnIntervalSec = 1.4f,
        maxSpawnIntervalSec = 2.2f,
        telegraphDurationSec = 0.85f,
        hazardSpeedMultiplier = 1.35f,
        maxConcurrentHazards = 4
    );

    companion object {
        fun fromString(name: String?): Difficulty {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: MEDIUM
        }
    }
}
