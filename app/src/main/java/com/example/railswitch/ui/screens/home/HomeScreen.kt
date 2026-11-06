package com.example.railswitch.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.railswitch.model.Difficulty
import com.example.railswitch.model.GameSettings
import com.example.railswitch.model.GameStats
import com.example.railswitch.ui.components.ArcadeButton
import com.example.railswitch.ui.components.ArcadeStatCard
import com.example.railswitch.ui.components.DifficultySelector
import com.example.railswitch.ui.theme.BackgroundBlack
import com.example.railswitch.ui.theme.ElectricCyan
import com.example.railswitch.ui.theme.ElectricCyanGlow
import com.example.railswitch.ui.theme.TextDark
import com.example.railswitch.ui.theme.TextMuted
import com.example.railswitch.ui.theme.TextWhite

@Composable
fun HomeScreen(
    settings: GameSettings,
    stats: GameStats,
    onDifficultyChanged: (Difficulty) -> Unit,
    onPlayClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onAboutClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Title & Tagline & Decorative rails
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                // Minimal glowing dual tracks icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .width(3.dp)
                            .height(36.dp),
                        color = ElectricCyan,
                        shape = RoundedCornerShape(1.5.dp)
                    ) {}
                    Spacer(modifier = Modifier.width(18.dp))
                    Surface(
                        modifier = Modifier
                            .width(3.dp)
                            .height(36.dp),
                        color = ElectricCyanGlow,
                        shape = RoundedCornerShape(1.5.dp)
                    ) {}
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "RAILSWITCH",
                    color = TextWhite,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Choose the rail. Beat the block.",
                    color = TextMuted,
                    fontSize = 14.sp,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Stats Cards: Best Score & Best Streak
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ArcadeStatCard(
                    title = "Best Score",
                    value = "${stats.bestScore}",
                    subtitle = "All-time High",
                    icon = Icons.Default.EmojiEvents,
                    highlight = stats.bestScore > 0,
                    modifier = Modifier.weight(1f)
                )

                ArcadeStatCard(
                    title = "Best Streak",
                    value = "${stats.highestStreak}x",
                    subtitle = "Consecutive",
                    icon = Icons.Default.Whatshot,
                    highlight = stats.highestStreak >= 5,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Difficulty Selector Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "SELECT DIFFICULTY",
                    color = TextDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                DifficultySelector(
                    selected = settings.difficulty,
                    onSelect = onDifficultyChanged
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = settings.difficulty.description,
                    color = TextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Main Actions: Play Button & Navigation Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ArcadeButton(
                    text = "Play Game",
                    icon = Icons.Default.PlayArrow,
                    onClick = onPlayClicked,
                    modifier = Modifier.fillMaxWidth(),
                    isPrimary = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ArcadeButton(
                        text = "Settings",
                        icon = Icons.Default.Settings,
                        onClick = onSettingsClicked,
                        modifier = Modifier.weight(1f),
                        isPrimary = false
                    )

                    ArcadeButton(
                        text = "About",
                        icon = Icons.Default.Info,
                        onClick = onAboutClicked,
                        modifier = Modifier.weight(1f),
                        isPrimary = false
                    )
                }
            }
        }
    }
}
