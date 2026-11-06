package com.example.railswitch.ui.screens.settings

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.railswitch.model.AppTheme
import com.example.railswitch.model.Difficulty
import com.example.railswitch.model.GameSettings
import com.example.railswitch.ui.components.ArcadeButton
import com.example.railswitch.ui.components.DifficultySelector
import com.example.railswitch.ui.theme.BackgroundBlack
import com.example.railswitch.ui.theme.BorderSubtle
import com.example.railswitch.ui.theme.ElectricCyan
import com.example.railswitch.ui.theme.ElectricCyanDark
import com.example.railswitch.ui.theme.ElectricCyanGlow
import com.example.railswitch.ui.theme.ElectricCyanSubtle
import com.example.railswitch.ui.theme.ImpactDanger
import com.example.railswitch.ui.theme.SurfaceCard
import com.example.railswitch.ui.theme.SurfaceDark
import com.example.railswitch.ui.theme.SurfaceElevated
import com.example.railswitch.ui.theme.TextDark
import com.example.railswitch.ui.theme.TextMuted
import com.example.railswitch.ui.theme.TextWhite

@Composable
fun SettingsScreen(
    settings: GameSettings,
    onSoundChanged: (Boolean) -> Unit,
    onHapticsChanged: (Boolean) -> Unit,
    onDifficultyChanged: (Difficulty) -> Unit,
    onThemeChanged: (AppTheme) -> Unit,
    onResetStats: () -> Unit,
    onBackClicked: () -> Unit
) {
    var showResetDialog by remember { mutableStateOf(false) }

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
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Top Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                IconButton(
                    onClick = onBackClicked,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceDark)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "SETTINGS",
                    color = TextWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }

            // Audio & Haptics Section
            SettingsSectionHeader("AUDIO & FEEDBACK")
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingsToggleRow(
                        title = "Sound Effects",
                        subtitle = "Synthesized retro arcade audio",
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        checked = settings.soundEnabled,
                        onCheckedChange = onSoundChanged
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsToggleRow(
                        title = "Haptic Vibration",
                        subtitle = "Tactile response on switches & impacts",
                        icon = Icons.Default.Vibration,
                        checked = settings.hapticsEnabled,
                        onCheckedChange = onHapticsChanged
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Gameplay Difficulty Section
            SettingsSectionHeader("DEFAULT DIFFICULTY")
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DifficultySelector(
                        selected = settings.difficulty,
                        onSelect = onDifficultyChanged
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = settings.difficulty.description,
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Theme Preference Section
            SettingsSectionHeader("THEME PREFERENCE")
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppTheme.entries.forEach { themeOption ->
                        val isSelected = themeOption == settings.theme
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onThemeChanged(themeOption) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) ElectricCyanSubtle else SurfaceElevated,
                            border = if (isSelected) BorderStroke(1.dp, ElectricCyanGlow) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = themeOption.label,
                                    color = if (isSelected) ElectricCyan else TextWhite,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                if (isSelected) {
                                    Surface(
                                        modifier = Modifier.size(8.dp),
                                        shape = RoundedCornerShape(4.dp),
                                        color = ElectricCyan
                                    ) {}
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Danger Zone: Reset Statistics
            SettingsSectionHeader("DATA MANAGEMENT")
            ArcadeButton(
                text = "Reset Statistics",
                icon = Icons.Default.DeleteOutline,
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth(),
                isPrimary = false
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Reset Confirmation Dialog
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                containerColor = SurfaceDark,
                titleContentColor = TextWhite,
                textContentColor = TextMuted,
                title = {
                    Text(
                        text = "Reset All Statistics?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Text("This will permanently clear your best score, highest streak, and game history. This action cannot be undone.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onResetStats()
                            showResetDialog = false
                        }
                    ) {
                        Text("Reset", color = ImpactDanger, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("Cancel", color = TextWhite)
                    }
                }
            )
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        color = TextDark,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (checked) ElectricCyan else TextDark,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    color = TextWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TextWhite,
                checkedTrackColor = ElectricCyanDark,
                uncheckedThumbColor = TextDark,
                uncheckedTrackColor = SurfaceElevated
            )
        )
    }
}
