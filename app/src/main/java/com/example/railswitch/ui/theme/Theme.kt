package com.example.railswitch.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.railswitch.model.AppTheme

private val DarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = BackgroundBlack,
    primaryContainer = ElectricCyanDark,
    onPrimaryContainer = Color.White,
    secondary = ElectricCyanDark,
    onSecondary = Color.White,
    background = BackgroundBlack,
    onBackground = TextWhite,
    surface = SurfaceDark,
    onSurface = TextWhite,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextMuted,
    outline = BorderSubtle
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricCyanDark,
    onPrimary = Color.White,
    primaryContainer = ElectricCyanSubtle,
    onPrimaryContainer = BackgroundBlack,
    secondary = Color(0xFF0288D1),
    onSecondary = Color.White,
    background = Color(0xFF0F141C),
    onBackground = TextWhite,
    surface = Color(0xFF161E2B),
    onSurface = TextWhite,
    surfaceVariant = Color(0xFF1F293A),
    onSurfaceVariant = TextMuted,
    outline = BorderSubtle
)

@Composable
fun RailSwitchTheme(
    themePreference: AppTheme = AppTheme.DARK,
    content: @Composable () -> Unit
) {
    val isDark = when (themePreference) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = BackgroundBlack.toArgb()
            window.navigationBarColor = BackgroundBlack.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
