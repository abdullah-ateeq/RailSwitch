package com.example.edgepulse

import com.example.edgepulse.engine.GameEngine
import com.example.edgepulse.model.Difficulty
import com.example.edgepulse.model.GameStatus
import com.example.edgepulse.model.PerimeterBlockHazard
import com.example.edgepulse.model.PerimeterGeometry
import com.example.edgepulse.model.PerimeterSurgeHazard
import com.example.edgepulse.model.Player
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class EdgePulseCoreTests {

    @Test
    fun testPerimeterNormalization() {
        assertEquals(0.25f, PerimeterGeometry.normalize(0.25f), 0.0001f)
        assertEquals(0.0f, PerimeterGeometry.normalize(1.0f), 0.0001f)
        assertEquals(0.75f, PerimeterGeometry.normalize(-0.25f), 0.0001f)
        assertEquals(0.1f, PerimeterGeometry.normalize(2.1f), 0.0001f)
    }

    @Test
    fun testPerimeterToPoint() {
        // Corners:
        // Top-left: p=0.0 -> (0, 0)
        val p0 = PerimeterGeometry.perimeterToPoint(0.0f)
        assertEquals(0.0f, p0.x, 0.001f)
        assertEquals(0.0f, p0.y, 0.001f)

        // Top-right: p=0.25 -> (1, 0)
        val pTopRight = PerimeterGeometry.perimeterToPoint(0.25f)
        assertEquals(1.0f, pTopRight.x, 0.001f)
        assertEquals(0.0f, pTopRight.y, 0.001f)

        // Bottom-right: p=0.50 -> (1, 1)
        val pBottomRight = PerimeterGeometry.perimeterToPoint(0.50f)
        assertEquals(1.0f, pBottomRight.x, 0.001f)
        assertEquals(1.0f, pBottomRight.y, 0.001f)

        // Bottom-left: p=0.75 -> (0, 1)
        val pBottomLeft = PerimeterGeometry.perimeterToPoint(0.75f)
        assertEquals(0.0f, pBottomLeft.x, 0.001f)
        assertEquals(1.0f, pBottomLeft.y, 0.001f)

        // Mid-top: p=0.125 -> (0.5, 0)
        val pMidTop = PerimeterGeometry.perimeterToPoint(0.125f)
        assertEquals(0.5f, pMidTop.x, 0.001f)
        assertEquals(0.0f, pMidTop.y, 0.001f)
    }

    @Test
    fun testShortestDistance() {
        assertEquals(0.1f, PerimeterGeometry.shortestDistance(0.1f, 0.2f), 0.0001f)
        // Wrap around distance: 0.95 to 0.05 is 0.10
        assertEquals(0.1f, PerimeterGeometry.shortestDistance(0.95f, 0.05f), 0.0001f)
        // Opposite side: 0.2 to 0.7 is 0.5
        assertEquals(0.5f, PerimeterGeometry.shortestDistance(0.2f, 0.7f), 0.0001f)
    }

    @Test
    fun testDirectionReversal() {
        val engine = GameEngine(Difficulty.MEDIUM)
        engine.start()

        val initialDirection = engine.state.player.direction
        val reversed = engine.reverseDirection()
        assertTrue("Reversal should succeed", reversed)
        assertEquals(-initialDirection, engine.state.player.direction)

        // Immediate second reversal within cooldown window should fail to prevent accidental spam
        val reversedImmediately = engine.reverseDirection()
        assertFalse("Rapid consecutive reversal within cooldown should be ignored", reversedImmediately)
        assertEquals(-initialDirection, engine.state.player.direction)
    }

    @Test
    fun testLapDetectionAndScoreCalculation() {
        val engine = GameEngine(Difficulty.EASY, spawnHazards = false)
        engine.start()
        val initialLaps = engine.state.lapsCompleted
        val initialScore = engine.state.score

        // Advance by exactly 1 full lap worth of time
        // speed = 0.22 laps/sec -> time for 1 lap = 1.0 / 0.22 ~= 4.545 sec
        val timeForOneLap = 1.0f / engine.state.player.speed
        val step = 0.05f
        var elapsed = 0f
        var lapDetected = false

        while (elapsed < timeForOneLap + 0.1f) {
            val result = engine.update(step)
            elapsed += step
            if (result.lapCompleted) {
                lapDetected = true
            }
        }

        assertTrue("Lap should have been completed", lapDetected)
        assertEquals(initialLaps + 1, engine.state.lapsCompleted)
        assertTrue("Score should have received at least 100 lap bonus + survival time points",
            engine.state.score >= initialScore + 100)
    }

    @Test
    fun testDifficultyProgression() {
        val engine = GameEngine(Difficulty.HARD, spawnHazards = false)
        engine.start()

        val initialSpeed = engine.state.player.speed

        // Advance 20 seconds
        for (i in 0 until 400) {
            engine.update(0.05f)
        }

        val laterSpeed = engine.state.player.speed
        assertTrue("Speed should increase over survival time", laterSpeed > initialSpeed)
        assertTrue("Speed should not exceed maxPlayerSpeed", laterSpeed <= Difficulty.HARD.maxPlayerSpeed)
    }

    @Test
    fun testHazardMovementAndCollision() {
        val block = PerimeterBlockHazard(
            id = 1L,
            startPos = 0.5f,
            length = 0.10f,
            speed = 0.10f, // moves CW at 0.10 laps/sec
            spawnTimeSec = 0f,
            telegraphDurationSec = 1.0f,
            totalLifetimeSec = 5.0f
        )

        // During telegraph (< 1.0s), hazard is not active, so no collision
        assertFalse("Should not collide during telegraph", block.collidesWith(0.5f, 0.5f))

        // At t = 1.0s (active starts, center at startPos = 0.5)
        assertTrue("Should collide at exact center when active", block.collidesWith(0.5f, 1.0f))

        // Player far away at 0.0
        assertFalse("Should not collide with distant player", block.collidesWith(0.0f, 1.0f))

        // At t = 2.0s, hazard has moved by speed * 1.0s = 0.10 -> center is 0.60
        assertEquals(0.60f, block.currentCenter(2.0f), 0.001f)
        assertTrue("Should collide at new center position", block.collidesWith(0.60f, 2.0f))
    }

    @Test
    fun testPerimeterSurgeCollision() {
        val surge = PerimeterSurgeHazard(
            id = 2L,
            startPos = 0.20f,
            endPos = 0.35f,
            spawnTimeSec = 0f,
            telegraphDurationSec = 1.0f,
            totalLifetimeSec = 4.0f
        )

        assertFalse(surge.collidesWith(0.25f, 0.5f)) // Still telegraphing
        assertTrue(surge.collidesWith(0.25f, 1.5f)) // Active and inside segment
        assertFalse(surge.collidesWith(0.60f, 1.5f)) // Active but outside segment
    }

    @Test
    fun testFairHazardGeneration() {
        val engine = GameEngine(Difficulty.MEDIUM, Random(42))
        val playerPos = 0.15f
        val hazard = engine.generateFairHazard(10.0f, playerPos)

        assertNotNull("Hazard should be generated", hazard)
        // Check that it doesn't immediately collide with player
        assertFalse("Hazard must not collide during telegraph period",
            hazard!!.collidesWith(playerPos, 10.0f))
    }
}
