package com.example.railswitch.logic

import com.example.railswitch.model.Block
import com.example.railswitch.model.Difficulty
import com.example.railswitch.model.Rail
import kotlin.math.abs
import kotlin.random.Random

/**
 * Handles fair obstacle generation ensuring no impossible or simultaneous-collision patterns.
 */
class BlockSpawner(
    private val random: Random = Random.Default
) {
    private var nextBlockId: Long = 1L

    /**
     * Resets the spawner internal state.
     */
    fun reset() {
        nextBlockId = 1L
    }

    /**
     * Checks whether adding a new block maintains a fair, survivable pattern.
     * Rule 1: No two blocks overlap in Y coordinates.
     * Rule 2: If blocks are on opposite rails, their vertical separation must be >= minSafeGapY.
     * Rule 3: Blocks on the same rail must not overlap.
     */
    fun isPatternSafe(
        existingBlocks: List<Block>,
        candidate: Block,
        minSafeGapY: Float
    ): Boolean {
        for (existing in existingBlocks) {
            val dist = abs(existing.y - candidate.y)
            if (existing.rail != candidate.rail) {
                // On opposite rails, player must have enough vertical distance to switch
                if (dist < minSafeGapY) {
                    return false
                }
            } else {
                // On the same rail, blocks must not collide/overlap
                val blockHeight = candidate.height
                if (dist < blockHeight + 30f) {
                    return false
                }
            }
        }
        return true
    }

    /**
     * Decides the next rail to spawn a block on, considering the difficulty and recent pattern.
     */
    fun chooseNextRail(
        existingBlocks: List<Block>,
        difficulty: Difficulty
    ): Rail {
        val lastBlock = existingBlocks.minByOrNull { it.y } // highest on screen (lowest y)
        if (lastBlock == null) {
            return if (random.nextBoolean()) Rail.LEFT else Rail.RIGHT
        }

        return when (difficulty) {
            Difficulty.EASY -> {
                // Easy prefers runs of the same rail or clean deliberate alternations
                if (random.nextFloat() < 0.65f) {
                    lastBlock.rail.opposite()
                } else {
                    lastBlock.rail
                }
            }
            Difficulty.MEDIUM -> {
                // Balanced mix of switches and same-rail blocks
                if (random.nextFloat() < 0.60f) {
                    lastBlock.rail.opposite()
                } else {
                    lastBlock.rail
                }
            }
            Difficulty.HARD -> {
                // Hard tests quick transitions more frequently
                if (random.nextFloat() < 0.70f) {
                    lastBlock.rail.opposite()
                } else {
                    lastBlock.rail
                }
            }
        }
    }

    /**
     * Attempts to create a new safe block at spawn position (y = -height).
     * Returns the Block if safe to spawn now, or null if existing blocks are too close.
     */
    fun trySpawnBlock(
        existingBlocks: List<Block>,
        difficulty: Difficulty,
        spawnY: Float = -60f,
        blockHeight: Float = 55f
    ): Block? {
        val highestBlock = existingBlocks.minByOrNull { it.y }

        // If the highest block is still above or right at the top edge, wait
        if (highestBlock != null && highestBlock.y < 0f) {
            return null
        }

        val targetRail = chooseNextRail(existingBlocks, difficulty)
        val candidate = Block(
            id = nextBlockId,
            rail = targetRail,
            y = spawnY,
            height = blockHeight
        )

        // Validate fairness against all active blocks
        return if (isPatternSafe(existingBlocks, candidate, difficulty.minSafeGapY)) {
            nextBlockId++
            candidate
        } else {
            // If candidate wasn't safe on targetRail, check if opposite rail is safe
            val altCandidate = candidate.copy(rail = targetRail.opposite())
            if (isPatternSafe(existingBlocks, altCandidate, difficulty.minSafeGapY)) {
                nextBlockId++
                altCandidate
            } else {
                null
            }
        }
    }
}
