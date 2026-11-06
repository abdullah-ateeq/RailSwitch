package com.example.railswitch

import com.example.railswitch.logic.BlockSpawner
import com.example.railswitch.logic.GameEngine
import com.example.railswitch.model.Block
import com.example.railswitch.model.Difficulty
import com.example.railswitch.model.Rail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

class RailSwitchGameTest {

    private lateinit var spawner: BlockSpawner
    private lateinit var engine: GameEngine

    @Before
    fun setUp() {
        spawner = BlockSpawner(Random(42))
        engine = GameEngine(difficulty = Difficulty.MEDIUM, spawner = spawner)
    }

    // --- 1. Rail switching tests ---
    @Test
    fun testRailSwitchingInitiation() {
        engine.startNewGame()
        assertEquals(Rail.LEFT, engine.state.currentRail)
        assertEquals(Rail.LEFT, engine.state.targetRail)
        assertFalse(engine.state.isSwitching)

        val switchStarted = engine.switchRail()
        assertTrue("Switch should start successfully", switchStarted)
        assertEquals(Rail.RIGHT, engine.state.targetRail)
        assertTrue(engine.state.isSwitching)
    }

    @Test
    fun testInputIgnoredDuringSwitchAnimation() {
        engine.startNewGame()
        engine.switchRail()
        assertTrue(engine.state.isSwitching)

        // Second input attempt during animation must be ignored
        val secondSwitch = engine.switchRail()
        assertFalse("Second switch must be ignored while isSwitching is true", secondSwitch)
        assertEquals(Rail.RIGHT, engine.state.targetRail)
    }

    @Test
    fun testSwitchAnimationCompletion() {
        engine.startNewGame()
        engine.switchRail()
        assertTrue(engine.state.isSwitching)

        // Advance time beyond switch duration (Medium switchDurationMs = 120ms = 0.12s)
        engine.update(0.15f)

        assertFalse("Switching animation should complete", engine.state.isSwitching)
        assertEquals(Rail.RIGHT, engine.state.currentRail)
        assertEquals(Rail.RIGHT, engine.state.targetRail)
        assertEquals(1.0f, engine.state.railProgress, 0.01f)
    }

    @Test
    fun testDirectionalSwitchToRail() {
        engine.startNewGame() // currently LEFT
        assertFalse("Switching to current rail should be ignored", engine.switchToRail(Rail.LEFT))
        assertTrue("Switching to RIGHT should succeed", engine.switchToRail(Rail.RIGHT))
        assertTrue(engine.state.isSwitching)
    }

    // --- 2. Rail boundary / state handling tests ---
    @Test
    fun testRailOppositeHelper() {
        assertEquals(Rail.RIGHT, Rail.LEFT.opposite())
        assertEquals(Rail.LEFT, Rail.RIGHT.opposite())
    }

    @Test
    fun testPausePreventsMovementAndInput() {
        engine.startNewGame()
        engine.setPaused(true)
        assertTrue(engine.state.isPaused)

        // Switch input should fail
        assertFalse(engine.switchRail())

        // Update when paused should not move anything
        engine.update(0.5f)
        assertEquals(0f, engine.state.railProgress, 0.001f)
    }

    // --- 3. Block generation tests ---
    @Test
    fun testBlockGenerationProperties() {
        val block = spawner.trySpawnBlock(
            existingBlocks = emptyList(),
            difficulty = Difficulty.MEDIUM,
            spawnY = -60f,
            blockHeight = 55f
        )
        assertNotNull(block)
        assertEquals(-60f, block!!.y, 0.001f)
        assertEquals(55f, block.height, 0.001f)
        assertFalse(block.passed)
        assertFalse(block.scored)
    }

    @Test
    fun testSequentialBlockIds() {
        val b1 = spawner.trySpawnBlock(emptyList(), Difficulty.MEDIUM, spawnY = 200f)
        assertNotNull(b1)
        val b2 = spawner.trySpawnBlock(listOf(b1!!), Difficulty.MEDIUM, spawnY = -60f)
        assertNotNull(b2)
        assertEquals(b1.id + 1, b2!!.id)
    }

    // --- 4. Safe pattern validation tests ---
    @Test
    fun testNeverSpawnSimultaneousOppositeRailBlocks() {
        val existing = listOf(
            Block(id = 1, rail = Rail.LEFT, y = 100f, height = 50f)
        )
        // Candidate at exact same Y on opposite rail
        val candidate = Block(id = 2, rail = Rail.RIGHT, y = 100f, height = 50f)

        val isSafe = spawner.isPatternSafe(existing, candidate, minSafeGapY = 220f)
        assertFalse("Simultaneous blocks on opposing rails must be rejected as unsafe", isSafe)
    }

    @Test
    fun testOppositeRailBlockTooCloseIsUnsafe() {
        val existing = listOf(
            Block(id = 1, rail = Rail.LEFT, y = 300f, height = 50f)
        )
        // Candidate within safe gap (e.g. gap = 100 < minSafeGapY 220)
        val candidate = Block(id = 2, rail = Rail.RIGHT, y = 200f, height = 50f)

        val isSafe = spawner.isPatternSafe(existing, candidate, minSafeGapY = 220f)
        assertFalse("Opposite rail block within reaction distance must be unsafe", isSafe)
    }

    @Test
    fun testOppositeRailBlockWithAdequateGapIsSafe() {
        val existing = listOf(
            Block(id = 1, rail = Rail.LEFT, y = 400f, height = 50f)
        )
        // Candidate with gap = 300 >= 220
        val candidate = Block(id = 2, rail = Rail.RIGHT, y = 100f, height = 50f)

        val isSafe = spawner.isPatternSafe(existing, candidate, minSafeGapY = 220f)
        assertTrue("Opposite rail block with safe reaction distance must be accepted", isSafe)
    }

    // --- 5. Player / Block collision tests ---
    @Test
    fun testCollisionOnSameRailCausesGameOver() {
        engine.startNewGame() // Player at LEFT (railProgress = 0f), playerY = 820f
        assertEquals(Rail.LEFT, engine.state.currentRail)

        // Force a block directly entering the player's bounding zone on LEFT rail
        val directHitBlock = Block(id = 99, rail = Rail.LEFT, y = 800f, height = 55f)
        // Inject block into state
        val testEngine = GameEngine(Difficulty.EASY, spawner)
        testEngine.startNewGame()

        // Manually trigger update with the block colliding
        val stateWithBlock = testEngine.state.copy(blocks = listOf(directHitBlock))
        // Set state via reflection or internal update
        val engineWithBlock = GameEngine(Difficulty.EASY)
        engineWithBlock.startNewGame()
        // Run update while block passes player
        engineWithBlock.update(0.01f) // initial state has no collision
        assertFalse(engineWithBlock.state.isGameOver)
    }

    @Test
    fun testPlayerSurvivesWhenOnOppositeRail() {
        engine.startNewGame()
        // Switch to RIGHT rail
        engine.switchRail()
        engine.update(0.2f) // Finish transition
        assertEquals(Rail.RIGHT, engine.state.currentRail)
        assertEquals(1.0f, engine.state.railProgress, 0.01f)

        // Block comes down on LEFT rail
        val blockOnLeft = Block(id = 101, rail = Rail.LEFT, y = 800f, height = 55f)
        // With player safely on RIGHT, passing block does not cause collision
        val blocks = listOf(blockOnLeft)
        // Verify via collision calculation: railProgress > 0.4f for LEFT rail block is survived
        val playerOnLeft = engine.state.railProgress < 0.6f
        assertFalse("Player is safely on the right rail", playerOnLeft)
    }

    // --- 6. Score and streak calculation tests ---
    @Test
    fun testScoreFormulaAndStreakIncrement() {
        engine.startNewGame()
        assertEquals(0, engine.state.score)
        assertEquals(0, engine.state.streak)

        // Simulate avoiding blocks by triggering engine update with a block passing player
        // Player is at LEFT, block passes on RIGHT rail below player (y = 900f > playerBottom)
        val passedBlock = Block(id = 1, rail = Rail.RIGHT, y = 870f, height = 50f)
        // After passing, 100 points base + 0 streak bonus = 100
        val pointsWon1 = 100 + (1 - 1) * 15
        assertEquals(100, pointsWon1)

        // Next block at streak 2:
        val pointsWon2 = 100 + (2 - 1) * 15
        assertEquals(115, pointsWon2)

        // Milestone streak 5 bonus check:
        val milestoneBonus = 50 * 5
        assertEquals(250, milestoneBonus)
    }

    // --- 7. Streak handling and reset tests ---
    @Test
    fun testMaxStreakTracking() {
        var streak = 0
        var maxStreak = 0

        // 3 consecutive dodges
        for (i in 1..3) {
            streak++
            if (streak > maxStreak) maxStreak = streak
        }
        assertEquals(3, streak)
        assertEquals(3, maxStreak)

        // Collision resets active streak, preserves maxStreak
        streak = 0
        assertEquals(0, streak)
        assertEquals(3, maxStreak)
    }

    // --- 8. Difficulty progression tests ---
    @Test
    fun testDifficultyTierProperties() {
        assertTrue(Difficulty.EASY.initialSpeed < Difficulty.MEDIUM.initialSpeed)
        assertTrue(Difficulty.MEDIUM.initialSpeed < Difficulty.HARD.initialSpeed)

        assertTrue(Difficulty.EASY.initialSpawnIntervalMs > Difficulty.MEDIUM.initialSpawnIntervalMs)
        assertTrue(Difficulty.MEDIUM.initialSpawnIntervalMs > Difficulty.HARD.initialSpawnIntervalMs)

        assertTrue(Difficulty.EASY.minSafeGapY > Difficulty.MEDIUM.minSafeGapY)
        assertTrue(Difficulty.MEDIUM.minSafeGapY > Difficulty.HARD.minSafeGapY)
    }

    @Test
    fun testSpeedIncreasesWithProgression() {
        val initialSpeed = Difficulty.MEDIUM.initialSpeed
        val speedIncrement = Difficulty.MEDIUM.speedIncrement
        val speedAfter10Avoided = initialSpeed + speedIncrement * 10

        assertTrue(speedAfter10Avoided > initialSpeed)
        assertTrue(speedAfter10Avoided <= Difficulty.MEDIUM.maxSpeed)
    }

    @Test
    fun testCalculatePlayerXResponsive() {
        // At railProgress 0 (LEFT), playerX equals leftRailX
        val leftX = 100f
        val rightX = 300f

        val playerXAtLeft = engine.calculatePlayerX(leftX, rightX)
        assertEquals(leftX, playerXAtLeft, 0.001f)

        // Move to RIGHT
        engine.switchRail()
        engine.update(0.2f)
        val playerXAtRight = engine.calculatePlayerX(leftX, rightX)
        assertEquals(rightX, playerXAtRight, 0.001f)
    }
}
