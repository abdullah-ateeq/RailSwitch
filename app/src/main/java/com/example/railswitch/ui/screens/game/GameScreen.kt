package com.example.railswitch.ui.screens.game

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.railswitch.ui.components.PauseDialog
import com.example.railswitch.ui.theme.BackgroundBlack
import com.example.railswitch.ui.theme.BorderSubtle
import com.example.railswitch.ui.theme.ElectricCyan
import com.example.railswitch.ui.theme.ElectricCyanSubtle
import com.example.railswitch.ui.theme.MilestoneCyan
import com.example.railswitch.ui.theme.StreakGold
import com.example.railswitch.ui.theme.SurfaceCard
import com.example.railswitch.ui.theme.SurfaceDark
import com.example.railswitch.ui.theme.TextDark
import com.example.railswitch.ui.theme.TextMuted
import com.example.railswitch.ui.theme.TextWhite
import kotlinx.coroutines.flow.collectLatest

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onNavigateToResult: (score: Int, avoided: Int, streak: Int, isNewBest: Boolean, bestScore: Int) -> Unit,
    onQuitToMenu: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()

    // Handle back button: pause if playing
    BackHandler(enabled = !gameState.isGameOver) {
        if (!gameState.isPaused) {
            viewModel.pauseGame()
        } else {
            onQuitToMenu()
        }
    }

    // Observe navigation events (game over transition)
    LaunchedEffect(viewModel) {
        viewModel.navigationEvents.collectLatest { event ->
            when (event) {
                is GameNavigationEvent.NavigateToResult -> {
                    onNavigateToResult(
                        event.finalScore,
                        event.blocksAvoided,
                        event.highestStreak,
                        event.isNewBest,
                        event.bestScore
                    )
                }
            }
        }
    }

    // Score pop animation on increase
    val scoreScale = remember { Animatable(1f) }
    LaunchedEffect(gameState.score) {
        if (gameState.score > 0) {
            scoreScale.snapTo(1.22f)
            scoreScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(180, easing = FastOutSlowInEasing)
            )
        }
    }

    // Gesture detection variables
    var totalDragX by remember { mutableFloatStateOf(0f) }
    var swipeConsumed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
            .pointerInput(gameState.isPaused, gameState.isGameOver) {
                if (!gameState.isPaused && !gameState.isGameOver) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            totalDragX = 0f
                            swipeConsumed = false
                        },
                        onDragEnd = {
                            totalDragX = 0f
                            swipeConsumed = false
                        },
                        onDragCancel = {
                            totalDragX = 0f
                            swipeConsumed = false
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            totalDragX += dragAmount
                            if (!swipeConsumed) {
                                if (totalDragX < -35f) {
                                    viewModel.onSwipeLeft()
                                    swipeConsumed = true
                                } else if (totalDragX > 35f) {
                                    viewModel.onSwipeRight()
                                    swipeConsumed = true
                                }
                            }
                        }
                    )
                }
            }
            .pointerInput(gameState.isPaused, gameState.isGameOver) {
                if (!gameState.isPaused && !gameState.isGameOver) {
                    detectTapGestures {
                        viewModel.onScreenTap()
                    }
                }
            }
    ) {
        // Main Compose Canvas
        RailCanvas(
            gameState = gameState,
            modifier = Modifier.fillMaxSize()
        )

        // Minimal HUD at Top
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Score & Avoided Counter
            Column {
                Text(
                    text = "SCORE",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "${gameState.score}",
                    color = TextWhite,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.scale(scoreScale.value),
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "AVOIDED: ${gameState.blocksAvoided}",
                    color = TextDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Streak Badge & Pause Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Streak indicator
                AnimatedVisibility(
                    visible = gameState.streak > 1,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (gameState.streak >= 5) StreakGold.copy(alpha = 0.18f) else SurfaceCard,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (gameState.streak >= 5) StreakGold else BorderSubtle
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            if (gameState.streak >= 5) {
                                Icon(
                                    imageVector = Icons.Default.Whatshot,
                                    contentDescription = null,
                                    tint = StreakGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = "${gameState.streak}X",
                                color = if (gameState.streak >= 5) StreakGold else ElectricCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Pause Button
                IconButton(
                    onClick = { viewModel.pauseGame() },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SurfaceDark)
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = TextWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Milestone Notification Overlay
        AnimatedVisibility(
            visible = gameState.comboMilestone != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = ElectricCyanSubtle,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, MilestoneCyan),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "STREAK ${gameState.comboMilestone}!",
                    color = MilestoneCyan,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // Pause Dialog
        if (gameState.isPaused) {
            PauseDialog(
                onResume = { viewModel.resumeGame() },
                onRestart = { viewModel.restartGame() },
                onQuit = onQuitToMenu
            )
        }
    }
}
