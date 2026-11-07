package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val JarvisColorScheme = darkColorScheme(
    primary = JarvisCyan,
    onPrimary = JarvisVoid,
    primaryContainer = JarvisSurfaceVariant,
    onPrimaryContainer = JarvisCyanGlow,
    secondary = JarvisBlue,
    onSecondary = JarvisTextPrimary,
    secondaryContainer = JarvisSurfaceElevated,
    onSecondaryContainer = JarvisCyan,
    tertiary = JarvisGold,
    onTertiary = JarvisVoid,
    background = JarvisVoid,
    onBackground = JarvisTextPrimary,
    surface = JarvisSurface,
    onSurface = JarvisTextPrimary,
    surfaceVariant = JarvisSurfaceVariant,
    onSurfaceVariant = JarvisTextSecondary,
    outline = JarvisBorder,
    error = JarvisAlert,
    onError = JarvisTextPrimary
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = JarvisColorScheme,
        typography = Typography,
        content = content
    )
}
