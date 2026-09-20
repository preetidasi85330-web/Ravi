package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VaultColorScheme = darkColorScheme(
    primary = VaultPrimary,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF97F0FF),

    secondary = VaultSecondary,
    onSecondary = Color(0xFF003824),
    secondaryContainer = Color(0xFF005236),
    onSecondaryContainer = Color(0xFF88F8C2),

    tertiary = VaultTertiary,
    onTertiary = Color(0xFF2C0064),
    tertiaryContainer = Color(0xFF450099),
    onTertiaryContainer = Color(0xFFE9DDFF),

    background = VaultBgDark,
    onBackground = VaultTextPrimary,

    surface = VaultSurfaceDark,
    onSurface = VaultTextPrimary,
    surfaceVariant = VaultCardDark,
    onSurfaceVariant = VaultTextSecondary,

    outline = VaultBorder,
    outlineVariant = Color(0xFF1E293B),

    error = VaultError,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = VaultColorScheme,
        typography = Typography,
        content = content
    )
}
