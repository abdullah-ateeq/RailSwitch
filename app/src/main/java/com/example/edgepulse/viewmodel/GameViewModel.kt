package com.example.edgepulse.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.edgepulse.audio.HapticManager
import com.example.edgepulse.audio.SoundManager
import com.example.edgepulse.data.GamePreferences
import com.example.edgepulse.data.GameSettings
import com.example.edgepulse.data.GameStats
import com.example.edgepulse.engine.GameEngine
import com.example.edgepulse.model.Difficulty
import com.example.edgepulse.model.GameState
import com.example.edgepulse.model.GameStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    val preferences = GamePreferences(application)
    val soundManager = SoundManager()
    val hapticManager = HapticManager(application)

    val settings: StateFlow<GameSettings> = preferences.settingsFlow
    val stats: StateFlow<GameStats> = preferences.statsFlow

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private var engine: GameEngine? = null
    private var gameLoopJob: Job? = null

    init {
        // Sync audio/haptic toggles with user settings
        viewModelScope.launch {
            preferences.settingsFlow.collect { s ->
                soundManager.isSoundEnabled = s.isSoundEnabled
                hapticManager.isHapticsEnabled = s.isHapticsEnabled
            }
        }
    }

    fun startNewGame(difficulty: Difficulty? = null) {
        val selectedDifficulty = difficulty ?: preferences.getSettings().preferredDifficulty
        val newEngine = GameEngine(selectedDifficulty)
        newEngine.start()
        engine = newEngine
        _gameState.value = newEngine.state

        startGameLoop()
    }

    fun pauseGame() {
        engine?.pause()
        engine?.state?.let { _gameState.value = it }
    }

    fun resumeGame() {
        engine?.resume()
        engine?.state?.let { _gameState.value = it }
    }

    fun reverseDirection() {
        val currentEngine = engine ?: return
        if (currentEngine.reverseDirection()) {
            _gameState.value = currentEngine.state
            soundManager.playReverse()
            hapticManager.vibrateReverse()
        }
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            var lastTime = System.nanoTime()
            val targetFrameTimeNanos = 16_666_666L // ~60 FPS

            while (isActive) {
                val currentEngine = engine ?: break
                if (currentEngine.state.isGameOver) break

                val now = System.nanoTime()
                val dt = ((now - lastTime) / 1_000_000_000.0).toFloat().coerceIn(0.005f, 0.05f)
                lastTime = now

                if (currentEngine.state.isPlaying) {
                    val result = currentEngine.update(dt)
                    _gameState.value = result.state

                    if (result.lapCompleted) {
                        soundManager.playLap()
                        hapticManager.vibrateLap()
                    }

                    if (result.collisionOccurred) {
                        handleGameOver(result.state)
                        break
                    }
                }

                // Frame limiter
                val frameElapsed = System.nanoTime() - now
                val sleepNanos = targetFrameTimeNanos - frameElapsed
                if (sleepNanos > 1_000_000L) {
                    delay(sleepNanos / 1_000_000L)
                } else {
                    delay(4)
                }
            }
        }
    }

    private fun handleGameOver(state: GameState) {
        soundManager.playCollision()
        hapticManager.vibrateCollision()

        val isNewBest = preferences.recordGameResult(
            difficulty = state.difficulty,
            score = state.score,
            laps = state.lapsCompleted,
            survivalTimeSec = state.survivalTimeSec
        )

        _gameState.value = state.copy(isNewHighScore = isNewBest)
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
    }
}
