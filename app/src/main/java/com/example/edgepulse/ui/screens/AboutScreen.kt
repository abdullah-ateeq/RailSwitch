package com.example.edgepulse.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edgepulse.ui.components.ArcadeTopBar
import com.example.edgepulse.ui.theme.BackgroundDark
import com.example.edgepulse.ui.theme.CyanAccent
import com.example.edgepulse.ui.theme.HazardPrimary
import com.example.edgepulse.ui.theme.SurfaceBorderDark
import com.example.edgepulse.ui.theme.SurfaceDark
import com.example.edgepulse.ui.theme.TextMuted
import com.example.edgepulse.ui.theme.TextWhite

@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ArcadeTopBar(
                title = "About",
                onBackClick = onNavigateBack
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = SurfaceDark,
                    border = BorderStroke(1.dp, SurfaceBorderDark)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "EDGEPULSE",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 3.sp,
                                color = TextWhite
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "VERSION 1.0.0",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = CyanAccent,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "A minimalist, fast-paced reflex arcade game built with Jetpack Compose & Material 3. Navigate the square perimeter, predict incoming hazards, switch directions at split-second timing, and survive as long as possible.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextMuted,
                                lineHeight = 22.sp
                            )
                        )
                    }
                }

                // How to Play Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = SurfaceDark,
                    border = BorderStroke(1.dp, SurfaceBorderDark)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "HOW TO PLAY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CyanAccent,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                        )

                        RuleItem(
                            number = "01",
                            title = "Stay on the Edge",
                            description = "Your pulse travels continuously along the square perimeter. You cannot leave the edge."
                        )

                        RuleItem(
                            number = "02",
                            title = "Instant Direction Reversal",
                            description = "Tap anywhere on the screen or swipe to reverse direction instantly. Anticipate hazard sweeps."
                        )

                        RuleItem(
                            number = "03",
                            title = "Watch the Telegraphs",
                            description = "Hazards blink and charge before turning lethal. Use the telegraph window to relocate safely."
                        )

                        RuleItem(
                            number = "04",
                            title = "Complete Laps for Streaks",
                            description = "Every complete circuit earns +100 bonus points plus an escalating consecutive lap streak multiplier."
                        )
                    }
                }

                // Architecture & Privacy Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = SurfaceDark,
                    border = BorderStroke(1.dp, SurfaceBorderDark)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "TECHNICAL DETAILS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CyanAccent,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• 100% Offline: Zero trackers, no telemetry, no internet access.\n• Native Compose Canvas 60 FPS graphics.\n• Procedural PCM audio synthesis.\n• Clean normalized mathematical perimeter model [0.0..1.0).",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextMuted,
                                lineHeight = 20.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun RuleItem(
    number: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = number,
            style = MaterialTheme.typography.labelLarge.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                color = CyanAccent
            )
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextMuted,
                    lineHeight = 18.sp
                )
            )
        }
    }
}
