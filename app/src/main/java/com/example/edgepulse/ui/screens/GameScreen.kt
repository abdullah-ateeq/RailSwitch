package com.example.edgepulse.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edgepulse.model.GameStatus
import com.example.edgepulse.ui.components.ArcadeButton
import com.example.edgepulse.ui.components.GameArena
import com.example.edgepulse.ui.theme.BackgroundDark
import com.example.edgepulse.ui.theme.CyanAccent
import com.example.edgepulse.ui.theme.SurfaceBorderDark
import com.example.edgepulse.ui.theme.SurfaceDark
import com.example.edgepulse.ui.theme.TextMuted
import com.example.edgepulse.ui.theme.TextWhite
import com.example.edgepulse.viewmodel.GameViewModel
import kotlinx.coroutines.delay

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onGameOver: () -> Unit,
    onExitToHome: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()

    // Handle back button while playing: pause game instead of exiting abruptly
    BackHandler(enabled = gameState.isPlaying) {
        viewModel.pauseGame()
    }

    // Automatically transition to result screen on game over after a short impact delay
    LaunchedEffect(gameState.status) {
        if (gameState.status == GameStatus.GAME_OVER) {
            delay(500)
            onGameOver()
        }
    }

    var accumulatedDragX by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .pointerInput(gameState.isPlaying) {
                if (!gameState.isPlaying) return@pointerInput
                detectTapGestures(
                    onTap = {
                        viewModel.reverseDirection()
                    }
                )
            }
            .pointerInput(gameState.isPlaying) {
                if (!gameState.isPlaying) return@pointerInput
                detectDragGestures(
                    onDragStart = { accumulatedDragX = 0f },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        accumulatedDragX += dragAmount.x
                        if (kotlin.math.abs(accumulatedDragX) > 40f) {
                            viewModel.reverseDirection()
                            accumulatedDragX = 0f
                        }
                    }
                )
            }
    ) {
        // Main Game Arena Canvas
        GameArena(
            gameState = gameState,
            modifier = Modifier.fillMaxSize()
        )

        // Top Minimal HUD
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lap Counter Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SurfaceDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LAP",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = gameState.lapsCompleted.toString(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = CyanAccent,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }

                // Centered Score Display
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SCORE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            letterSpacing = 1.2.sp,
                            fontSize = 10.sp
                        )
                    )
                    Text(
                        text = String.format("%05d", gameState.score),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = TextWhite,
                            letterSpacing = 2.sp,
                            fontSize = 24.sp
                        )
                    )
                }

                // Survival Time + Pause Button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val minutes = (gameState.survivalTimeSec / 60).toInt()
                    val seconds = (gameState.survivalTimeSec % 60).toInt()
                    val tenths = ((gameState.survivalTimeSec * 10) % 10).toInt()
                    val timeFormatted = String.format("%02d:%02d.%d", minutes, seconds, tenths)

                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            color = TextMuted,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(end = 10.dp)
                    )

                    Surface(
                        modifier = Modifier.size(38.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = SurfaceDark,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark)
                    ) {
                        IconButton(
                            onClick = {
                                if (gameState.isPlaying) viewModel.pauseGame()
                                else if (gameState.status == GameStatus.PAUSED) viewModel.resumeGame()
                            }
                        ) {
                            Text(
                                text = if (gameState.status == GameStatus.PAUSED) "▶" else "❚❚",
                                color = TextWhite,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Streak bonus notification if streak > 1
            if (gameState.consecutiveLaps > 1) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${gameState.consecutiveLaps}X STREAK",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = CyanAccent,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        // Bottom Tap Hint
        Text(
            text = "TAP OR SWIPE TO REVERSE",
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextMuted.copy(alpha = 0.45f),
                letterSpacing = 1.5.sp,
                fontSize = 11.sp
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )

        // Pause Menu Dialog
        if (gameState.status == GameStatus.PAUSED) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(24.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "PAUSED",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 3.sp,
                                color = TextWhite
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        ArcadeButton(
                            text = "RESUME",
                            onClick = { viewModel.resumeGame() },
                            isPrimary = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        ArcadeButton(
                            text = "RESTART",
                            onClick = { viewModel.startNewGame(gameState.difficulty) },
                            isPrimary = false
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        ArcadeButton(
                            text = "QUIT TO HOME",
                            onClick = onExitToHome,
                            isPrimary = false,
                            isDestructive = true
                        )
                    }
                }
            }
        }
    }
}
