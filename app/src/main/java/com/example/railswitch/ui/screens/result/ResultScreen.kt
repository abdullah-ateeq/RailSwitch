package com.example.railswitch.ui.screens.result

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.railswitch.ui.components.ArcadeButton
import com.example.railswitch.ui.components.ArcadeStatCard
import com.example.railswitch.ui.theme.BackgroundBlack
import com.example.railswitch.ui.theme.BorderSubtle
import com.example.railswitch.ui.theme.ElectricCyan
import com.example.railswitch.ui.theme.ElectricCyanSubtle
import com.example.railswitch.ui.theme.ImpactDanger
import com.example.railswitch.ui.theme.StreakGold
import com.example.railswitch.ui.theme.SurfaceCard
import com.example.railswitch.ui.theme.TextDark
import com.example.railswitch.ui.theme.TextMuted
import com.example.railswitch.ui.theme.TextWhite

@Composable
fun ResultScreen(
    finalScore: Int,
    blocksAvoided: Int,
    highestStreak: Int,
    isNewBest: Boolean,
    bestScore: Int,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit
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
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Game Over & New Best Banner
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = "GAME OVER",
                    color = ImpactDanger,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.5.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "$finalScore",
                    color = TextWhite,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp
                )

                Text(
                    text = "FINAL SCORE",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                AnimatedVisibility(
                    visible = isNewBest,
                    enter = fadeIn() + scaleIn()
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = StreakGold.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, StreakGold)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = StreakGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "NEW ALL-TIME BEST!",
                                color = StreakGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Breakdown Grid Cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ArcadeStatCard(
                        title = "Avoided",
                        value = "$blocksAvoided",
                        subtitle = "Blocks",
                        icon = Icons.Default.Shield,
                        highlight = false,
                        modifier = Modifier.weight(1f)
                    )

                    ArcadeStatCard(
                        title = "Streak",
                        value = "${highestStreak}x",
                        subtitle = "Max Combo",
                        icon = Icons.Default.Whatshot,
                        highlight = highestStreak >= 5,
                        modifier = Modifier.weight(1f)
                    )
                }

                ArcadeStatCard(
                    title = "All-Time Best",
                    value = "$bestScore",
                    subtitle = if (isNewBest) "New record established" else "Personal high score",
                    icon = Icons.Default.EmojiEvents,
                    highlight = isNewBest,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Navigation Actions
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ArcadeButton(
                    text = "Play Again",
                    icon = Icons.Default.Refresh,
                    onClick = onPlayAgain,
                    modifier = Modifier.fillMaxWidth(),
                    isPrimary = true
                )

                ArcadeButton(
                    text = "Home Menu",
                    icon = Icons.Default.Home,
                    onClick = onHome,
                    modifier = Modifier.fillMaxWidth(),
                    isPrimary = false
                )
            }
        }
    }
}
