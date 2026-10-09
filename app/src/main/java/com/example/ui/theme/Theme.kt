package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ForgeVioletPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4C1D95),
    onPrimaryContainer = Color(0xFFEDE9FE),
    secondary = ForgeAmberAccent,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF78350F),
    onSecondaryContainer = Color(0xFFFEF3C7),
    tertiary = ForgeCoralAccent,
    onTertiary = Color.White,
    background = ForgeDarkBg,
    onBackground = TextPrimaryDark,
    surface = ForgeDarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = ForgeDarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = ForgeDarkSurfaceBorder,
    outlineVariant = Color(0xFF4B5563)
)

private val LightColorScheme = lightColorScheme(
    primary = ForgeLightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = Color(0xFF3B0764),
    secondary = ForgeAmberAccent,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = ForgeCoralAccent,
    onTertiary = Color.White,
    background = ForgeLightBg,
    onBackground = Color(0xFF0F172A),
    surface = ForgeLightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = ForgeLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0)
)

@Composable
fun CreatorForgeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent CreatorForge branding
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else DarkColorScheme // default to sleek creator studio dark theme for high aesthetic

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
