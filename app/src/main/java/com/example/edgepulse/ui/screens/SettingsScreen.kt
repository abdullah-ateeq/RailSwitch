package com.example.edgepulse.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edgepulse.data.AppThemeMode
import com.example.edgepulse.model.Difficulty
import com.example.edgepulse.ui.components.ArcadeButton
import com.example.edgepulse.ui.components.ArcadeTopBar
import com.example.edgepulse.ui.components.DifficultySelector
import com.example.edgepulse.ui.theme.BackgroundDark
import com.example.edgepulse.ui.theme.CyanAccent
import com.example.edgepulse.ui.theme.HazardPrimary
import com.example.edgepulse.ui.theme.SurfaceBorderDark
import com.example.edgepulse.ui.theme.SurfaceDark
import com.example.edgepulse.ui.theme.SurfaceVariantDark
import com.example.edgepulse.ui.theme.TextMuted
import com.example.edgepulse.ui.theme.TextWhite
import com.example.edgepulse.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            ArcadeTopBar(
                title = "Settings",
                onBackClick = onNavigateBack
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. Theme Section
                SettingsCard(title = "THEME MODE") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceVariantDark)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AppThemeMode.entries.forEach { mode ->
                            val isSelected = mode == settings.themeMode
                            val bg = if (isSelected) CyanAccent else Color.Transparent
                            val textColor = if (isSelected) Color.Black else TextMuted
                            val fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bg)
                                    .clickable { viewModel.setThemeMode(mode) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode.name,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = textColor,
                                        fontWeight = fontWeight,
                                        letterSpacing = 1.sp
                                    )
                                )
                            }
                        }
                    }
                }

                // 2. Audio & Haptics Section
                SettingsCard(title = "FEEDBACK") {
                    // Sound Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Sound Effects",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = TextWhite,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text = "Procedural arcade chimes and impact tones",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                            )
                        }

                        Switch(
                            checked = settings.isSoundEnabled,
                            onCheckedChange = { viewModel.setSoundEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = CyanAccent,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = SurfaceVariantDark
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Haptics Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Haptic Vibration",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = TextWhite,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text = "Tactile clicks on direction reverse and laps",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                            )
                        }

                        Switch(
                            checked = settings.isHapticsEnabled,
                            onCheckedChange = { viewModel.setHapticsEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = CyanAccent,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = SurfaceVariantDark
                            )
                        )
                    }
                }

                // 3. Difficulty Preference Section
                SettingsCard(title = "DEFAULT DIFFICULTY") {
                    Text(
                        text = "Preferred challenge level when starting a new game:",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    DifficultySelector(
                        selectedDifficulty = settings.preferredDifficulty,
                        onSelectDifficulty = { diff ->
                            viewModel.setDifficulty(diff)
                        }
                    )
                }

                // 4. Statistics Reset Section
                SettingsCard(title = "DATA MANAGEMENT") {
                    Text(
                        text = "Clear all recorded high scores, lap records, and run counts:",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    ArcadeButton(
                        text = "RESET STATISTICS",
                        onClick = { showResetDialog = true },
                        isPrimary = false,
                        isDestructive = true
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Reset Confirmation Dialog
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = {
                    Text(
                        text = "RESET STATISTICS?",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = HazardPrimary
                        )
                    )
                },
                text = {
                    Text(
                        text = "This will permanently erase all best scores, survival times, and lap records. This action cannot be undone.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextWhite)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.resetStatistics()
                            showResetDialog = false
                        }
                    ) {
                        Text(
                            text = "RESET ALL",
                            color = HazardPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text(text = "CANCEL", color = TextMuted)
                    }
                },
                containerColor = SurfaceDark,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = SurfaceDark,
        border = BorderStroke(1.dp, SurfaceBorderDark)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = CyanAccent,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            )
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}
