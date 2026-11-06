package com.example.edgepulse.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edgepulse.ui.components.ArcadeButton
import com.example.edgepulse.ui.components.StatCard
import com.example.edgepulse.ui.theme.BackgroundDark
import com.example.edgepulse.ui.theme.CyanAccent
import com.example.edgepulse.ui.theme.HazardPrimary
import com.example.edgepulse.ui.theme.TextMuted
import com.example.edgepulse.ui.theme.TextWhite
import com.example.edgepulse.viewmodel.GameViewModel

@Composable
fun ResultScreen(
    viewModel: GameViewModel,
    onPlayAgain: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()
    val stats by viewModel.stats.collectAsState()

    val pulseTransition = rememberInfiniteTransition(label = "badgePulse")
    val badgeGlow by pulseTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val minutes = (gameState.survivalTimeSec / 60).toInt()
    val seconds = (gameState.survivalTimeSec % 60).toInt()
    val tenths = ((gameState.survivalTimeSec * 10) % 10).toInt()
    val timeFormatted = String.format("%02d:%02d.%d", minutes, seconds, tenths)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "RUN TERMINATED",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 3.sp,
                        color = HazardPrimary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Score Title
                Text(
                    text = "FINAL SCORE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 2.sp,
                        color = TextMuted
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = String.format("%,d", gameState.score),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = TextWhite,
                        fontSize = 48.sp
                    )
                )

                if (gameState.isNewHighScore) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyanAccent.copy(alpha = 0.15f * badgeGlow))
                            .border(1.dp, CyanAccent.copy(alpha = badgeGlow), RoundedCornerShape(6.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "★ NEW ALL-TIME BEST! ★",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CyanAccent,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp
                            )
                        )
                    }
                }
            }

            // Metrics Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        label = "Survival Time",
                        value = timeFormatted,
                        accentColor = CyanAccent,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Laps Completed",
                        value = gameState.lapsCompleted.toString(),
                        accentColor = CyanAccent,
                        subValue = "Max streak: ${gameState.consecutiveLaps}",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        label = "Best Score",
                        value = String.format("%,d", stats.bestScore),
                        accentColor = TextWhite,
                        modifier = Modifier.weight(1f)
                    )
                    val bestMin = (stats.longestSurvivalSec / 60).toInt()
                    val bestSec = (stats.longestSurvivalSec % 60).toInt()
                    val bestTimeFormatted = String.format("%02d:%02d", bestMin, bestSec)

                    StatCard(
                        label = "Best Time",
                        value = bestTimeFormatted,
                        accentColor = TextWhite,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ArcadeButton(
                    text = "PLAY AGAIN",
                    onClick = onPlayAgain,
                    isPrimary = true
                )

                ArcadeButton(
                    text = "HOME",
                    onClick = onNavigateToHome,
                    isPrimary = false
                )
            }
        }
    }
}
