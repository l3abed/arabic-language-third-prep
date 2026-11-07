package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NightBluePrimary,
    onPrimary = Color.White,
    primaryContainer = NightBlueSurfaceVariant,
    onPrimaryContainer = Color.White,
    secondary = NightGoldAccent,
    onSecondary = NightBlueBackground,
    background = NightBlueBackground,
    onBackground = TextPrimaryDark,
    surface = NightBlueSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = NightBlueSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = NightBlueBorder,
    outlineVariant = NightBlueBorder
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryDarkTeal,
    onPrimary = Color.White,
    primaryContainer = PrimaryDarkTealLight,
    onPrimaryContainer = Color.White,
    secondary = GoldAccent,
    onSecondary = PrimaryDarkTeal,
    background = AppBackgroundLight,
    onBackground = TextPrimaryLight,
    surface = AppSurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = AppSurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderLight,
    outlineVariant = BorderLight
)

@Composable
fun ArabicAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
