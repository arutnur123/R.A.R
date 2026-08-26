package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color.White,
    primaryContainer = IndigoContainer,
    onPrimaryContainer = IndigoPrimaryLight,
    secondary = TealAccent,
    onSecondary = Color.Black,
    secondaryContainer = TealContainer,
    onSecondaryContainer = TealAccent,
    tertiary = EmeraldSuccess,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF064E3B),
    background = CanvasBackground,
    onBackground = TextWhite,
    surface = GlassSurface,
    onSurface = TextWhite,
    surfaceVariant = GlassSurfaceElevated,
    onSurfaceVariant = TextMuted,
    outline = GlassBorder,
    error = RoseDanger,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}



