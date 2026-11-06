package com.example.edgepulse.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edgepulse.model.Difficulty
import com.example.edgepulse.ui.components.ArcadeButton
import com.example.edgepulse.ui.components.DifficultySelector
import com.example.edgepulse.ui.components.StatCard
import com.example.edgepulse.ui.theme.BackgroundDark
import com.example.edgepulse.ui.theme.CyanAccent
import com.example.edgepulse.ui.theme.CyanAccentGlow
import com.example.edgepulse.ui.theme.PerimeterInactive
import com.example.edgepulse.ui.theme.SurfaceBorderDark
import com.example.edgepulse.ui.theme.SurfaceDark
import com.example.edgepulse.ui.theme.TextMuted
import com.example.edgepulse.ui.theme.TextWhite
import com.example.edgepulse.viewmodel.GameViewModel

@Composable
fun HomeScreen(
    viewModel: GameViewModel,
    onStartGame: (Difficulty) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val stats by viewModel.stats.collectAsState()

    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseGlow by pulseTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top action bar: Settings & About icons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceDark,
                    border = BorderStroke(1.dp, SurfaceBorderDark)
                ) {
                    IconButton(onClick = onNavigateToSettings) {
                        Text(text = "⚙", color = TextWhite, fontSize = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceDark,
                    border = BorderStroke(1.dp, SurfaceBorderDark)
                ) {
                    IconButton(onClick = onNavigateToAbout) {
                        Text(text = "ℹ", color = TextWhite, fontSize = 18.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Branding Section with mini glowing square icon
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(54.dp)) {
                        val side = size.width
                        drawRect(
                            color = CyanAccentGlow.copy(alpha = pulseGlow),
                            topLeft = Offset(0f, 0f),
                            size = Size(side, side),
                            style = Stroke(width = 6f)
                        )
                        drawRect(
                            color = CyanAccent,
                            topLeft = Offset(0f, 0f),
                            size = Size(side, side),
                            style = Stroke(width = 2.5f)
                        )
                        // Dot at top-right
                        drawCircle(CyanAccent, radius = 5f, center = Offset(side, 0f))
                        drawCircle(Color.White, radius = 2.5f, center = Offset(side, 0f))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "EDGEPULSE",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 5.sp,
                        color = TextWhite
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Stay on the edge. Never get caught.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextMuted,
                        letterSpacing = 0.8.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Stats Cards: Best Score & Best Lap
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "Best Score",
                    value = String.format("%,d", stats.bestScore),
                    accentColor = CyanAccent,
                    subValue = if (stats.gamesPlayed > 0) "${stats.gamesPlayed} runs" else "No runs yet",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Best Lap",
                    value = stats.bestLaps.toString(),
                    accentColor = CyanAccent,
                    subValue = String.format("%.1fs survived", stats.longestSurvivalSec),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Difficulty Selector
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "DIFFICULTY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                DifficultySelector(
                    selectedDifficulty = settings.preferredDifficulty,
                    onSelectDifficulty = { diff ->
                        viewModel.preferences.updateSettings(difficulty = diff)
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Large PLAY Button
            ArcadeButton(
                text = "PLAY",
                onClick = { onStartGame(settings.preferredDifficulty) },
                isPrimary = true,
                modifier = Modifier.height(58.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
