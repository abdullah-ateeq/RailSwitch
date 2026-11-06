package com.example.railswitch.ui.screens.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.railswitch.audio.HapticsManager
import com.example.railswitch.audio.SoundManager
import com.example.railswitch.data.PreferencesManager
import com.example.railswitch.logic.GameEngine
import com.example.railswitch.model.Difficulty
import com.example.railswitch.model.GameState
import com.example.railswitch.model.Rail
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed interface GameNavigationEvent {
    data class NavigateToResult(
        val finalScore: Int,
        val blocksAvoided: Int,
        val highestStreak: Int,
        val isNewBest: Boolean,
        val bestScore: Int
    ) : GameNavigationEvent
}

class GameViewModel(
    private val preferencesManager: PreferencesManager,
    private val soundManager: SoundManager,
    private val hapticsManager: HapticsManager,
    initialDifficulty: Difficulty? = null
) : ViewModel() {

    private val difficulty = initialDifficulty ?: preferencesManager.settings.value.difficulty
    private val gameEngine = GameEngine(difficulty = difficulty)

    private val _gameState = MutableStateFlow(gameEngine.state)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<GameNavigationEvent>(replay = 0)
    val navigationEvents: SharedFlow<GameNavigationEvent> = _navigationEvents.asSharedFlow()

    private var loopJob: Job? = null
    private var gameOverHandled = false

    init {
        // Sync sound & haptics from settings
        soundManager.isEnabled = preferencesManager.settings.value.soundEnabled
        hapticsManager.isEnabled = preferencesManager.settings.value.hapticsEnabled
        startLoop()
    }

    private fun startLoop() {
        loopJob?.cancel()
        gameOverHandled = false
        gameEngine.startNewGame(difficulty)
        _gameState.value = gameEngine.state

        loopJob = viewModelScope.launch {
            var lastTimeNanos = System.nanoTime()

            while (isActive) {
                val currentTimeNanos = System.nanoTime()
                val deltaSeconds = ((currentTimeNanos - lastTimeNanos) / 1_000_000_000f).coerceIn(0.005f, 0.05f)
                lastTimeNanos = currentTimeNanos

                val prevDodged = gameEngine.state.blocksAvoided

                gameEngine.update(deltaSeconds)
                val currentState = gameEngine.state
                _gameState.value = currentState

                // Audio / Haptic feedback for dodge & milestones
                if (currentState.blocksAvoided > prevDodged) {
                    if (currentState.comboMilestone != null) {
                        soundManager.playMilestoneSound()
                        hapticsManager.vibrateMilestone()
                    } else {
                        soundManager.playDodgeSound()
                        hapticsManager.vibrateDodge()
                    }
                }

                // Handle Game Over
                if (currentState.isGameOver && !gameOverHandled) {
                    gameOverHandled = true
                    soundManager.playCollisionSound()
                    hapticsManager.vibrateCollision()

                    // Record stats
                    val isNewBest = preferencesManager.recordGameResult(
                        score = currentState.score,
                        streak = currentState.maxStreak,
                        blocksAvoided = currentState.blocksAvoided
                    )
                    val bestScore = preferencesManager.stats.value.bestScore

                    // Brief pause to allow impact shake and crash visual
                    launch {
                        delay(650)
                        _navigationEvents.emit(
                            GameNavigationEvent.NavigateToResult(
                                finalScore = currentState.score,
                                blocksAvoided = currentState.blocksAvoided,
                                highestStreak = currentState.maxStreak,
                                isNewBest = isNewBest,
                                bestScore = bestScore
                            )
                        )
                    }
                }

                delay(16) // ~60 FPS frame cadence
            }
        }
    }

    fun onScreenTap() {
        if (gameEngine.switchRail()) {
            soundManager.playSwitchSound()
            hapticsManager.vibrateSwitch()
            _gameState.value = gameEngine.state
        }
    }

    fun onSwipeLeft() {
        if (gameEngine.switchToRail(Rail.LEFT)) {
            soundManager.playSwitchSound()
            hapticsManager.vibrateSwitch()
            _gameState.value = gameEngine.state
        }
    }

    fun onSwipeRight() {
        if (gameEngine.switchToRail(Rail.RIGHT)) {
            soundManager.playSwitchSound()
            hapticsManager.vibrateSwitch()
            _gameState.value = gameEngine.state
        }
    }

    fun pauseGame() {
        gameEngine.setPaused(true)
        _gameState.value = gameEngine.state
    }

    fun resumeGame() {
        gameEngine.setPaused(false)
        _gameState.value = gameEngine.state
    }

    fun restartGame() {
        startLoop()
    }

    override fun onCleared() {
        super.onCleared()
        loopJob?.cancel()
    }
}
