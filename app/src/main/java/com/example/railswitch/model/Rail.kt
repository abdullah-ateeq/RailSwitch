package com.example.railswitch.model

/**
 * Represents the two parallel tracks in RailSwitch.
 */
enum class Rail {
    LEFT,
    RIGHT;

    fun opposite(): Rail = when (this) {
        LEFT -> RIGHT
        RIGHT -> LEFT
    }
}
