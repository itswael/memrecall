package com.memrecall.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Rich purple-indigo palette
private val primaryColor = Color(0xFF6366F1)
private val primaryContainer = Color(0xFF4F46E5)
private val secondaryColor = Color(0xFF8B5CF6)
private val tertiaryColor = Color(0xFF06B6D4)
private val errorColor = Color(0xFFEF4444)
private val successColor = Color(0xFF22C55E)

val DarkColorScheme = darkColorScheme(
    primary = primaryColor,
    primaryContainer = Color(0xFF3730A3),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = secondaryColor,
    secondaryContainer = Color(0xFF6D28D9),
    tertiary = tertiaryColor,
    background = Color(0xFF0F0F1A),
    surface = Color(0xFF1A1A2E),
    surfaceVariant = Color(0xFF252540),
    onBackground = Color(0xFFF1F0FF),
    onSurface = Color(0xFFF1F0FF),
    onSurfaceVariant = Color(0xFFB0AECF),
    error = errorColor,
    outline = Color(0xFF3D3B5C),
)

val LightColorScheme = lightColorScheme(
    primary = primaryColor,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF3730A3),
    secondary = secondaryColor,
    secondaryContainer = Color(0xFFEDE9FE),
    tertiary = tertiaryColor,
    background = Color(0xFFF8F7FF),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF0EFFF),
    onBackground = Color(0xFF1A1A2E),
    onSurface = Color(0xFF1A1A2E),
    onSurfaceVariant = Color(0xFF4A4870),
    error = errorColor,
    outline = Color(0xFFD4D3E8),
)

val GreenSuccess = successColor
val RedError = errorColor
val OrangeWarning = Color(0xFFF59E0B)
val YellowStar = Color(0xFFEAB308)

@Composable
fun MemRecallTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
