package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BrightBlue,
    onPrimary = Color.White,
    primaryContainer = DeepNavyLight,
    onPrimaryContainer = LightGrayText,
    secondary = AccentTeal,
    onSecondary = Color.White,
    secondaryContainer = DeepNavyDark,
    background = BackgroundBlue,
    onBackground = LightGrayText,
    surface = SurfaceBlue,
    onSurface = LightGrayText,
    surfaceVariant = DarkCardBg,
    onSurfaceVariant = LightGrayText,
    outline = BrightBlue
)

private val LightColorScheme = lightColorScheme(
    primary = BrightBlue,
    onPrimary = Color.White,
    primaryContainer = DeepNavyLight,
    onPrimaryContainer = LightGrayText,
    secondary = AccentTeal,
    onSecondary = Color.White,
    secondaryContainer = DeepNavyDark,
    background = BackgroundBlue,
    onBackground = LightGrayText,
    surface = SurfaceBlue,
    onSurface = LightGrayText,
    surfaceVariant = DarkCardBg,
    onSurfaceVariant = LightGrayText,
    outline = BrightBlue
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic colors to preserve our stunning custom brand palette
    content: @Composable () -> Unit
) {
    // We enforce our signature dark premium palette as requested: "Dark Blue Premium UI"
    // However, if we want to honor light/dark settings, we can. The user explicitly requested a premium dark blue UI,
    // so let's default to the premium DarkColorScheme.
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
