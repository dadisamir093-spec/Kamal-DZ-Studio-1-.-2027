package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StudioColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = SecondaryPanels,
    onPrimaryContainer = CyanAccent,
    secondary = CyanAccent,
    onSecondary = MidnightNavy,
    secondaryContainer = PanelCard,
    onSecondaryContainer = PrimaryText,
    tertiary = PurpleAccent,
    onTertiary = Color.White,
    tertiaryContainer = MagentaAccent.copy(alpha = 0.2f),
    onTertiaryContainer = MagentaAccent,
    background = MidnightNavy,
    onBackground = PrimaryText,
    surface = DarkPanels,
    onSurface = PrimaryText,
    surfaceVariant = SecondaryPanels,
    onSurfaceVariant = SecondaryText,
    outline = PanelBorder,
    outlineVariant = PanelBorder.copy(alpha = 0.5f),
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // KAMAL DZ STUDIO 2027 operates in a dedicated high-end cinematic dark studio experience
    MaterialTheme(
        colorScheme = StudioColorScheme,
        typography = Typography,
        content = content
    )
}
