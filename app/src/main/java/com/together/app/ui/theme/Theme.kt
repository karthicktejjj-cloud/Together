package com.together.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = TogetherPrimary,
    onPrimary = TogetherOnBackground,
    secondary = TogetherSecondary,
    onSecondary = TogetherOnBackground,
    tertiary = TogetherTertiary,
    background = TogetherBackground,
    onBackground = TogetherOnBackground,
    surface = TogetherSurface,
    onSurface = TogetherOnSurface,
    surfaceVariant = TogetherSurfaceVariant,
    onSurfaceVariant = TogetherOnSurfaceVariant,
    outline = TogetherSurfaceVariant
)

@Composable
fun TogetherTheme(
    darkTheme: Boolean = true, // Force dark theme for cinematic experience
    dynamicColor: Boolean = false, // Disable dynamic color to maintain visual identity
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
