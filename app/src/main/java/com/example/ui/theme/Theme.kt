package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MinimalistColorScheme = darkColorScheme(
    primary = MinimalPrimary,
    onPrimary = Color.White,
    primaryContainer = MinimalPrimaryContainer,
    onPrimaryContainer = Color(0xFFEDE9FE),
    secondary = MinimalAccentAmber,
    onSecondary = Color.Black,
    background = MinimalDarkBg,
    onBackground = TextHigh,
    surface = MinimalSurface,
    onSurface = TextHigh,
    surfaceVariant = MinimalSurfaceHigh,
    onSurfaceVariant = TextMid,
    outline = MinimalBorder,
    outlineVariant = MinimalBorderSubtle
)

@Composable
fun CreatorForgeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MinimalistColorScheme,
        typography = Typography,
        content = content
    )
}
