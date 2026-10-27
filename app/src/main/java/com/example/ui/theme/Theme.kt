package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val KayaraColorScheme = darkColorScheme(
    primary = RosePrimary,
    onPrimary = MistText,
    primaryContainer = RoseDark,
    onPrimaryContainer = MistText,
    secondary = GoldAccent,
    onSecondary = InkBackground,
    secondaryContainer = PanelSurfaceVariant,
    onSecondaryContainer = MistText,
    tertiary = GoldAccent,
    onTertiary = InkBackground,
    background = InkBackground,
    onBackground = MistText,
    surface = PanelSurface,
    onSurface = MistText,
    surfaceVariant = PanelSurfaceVariant,
    onSurfaceVariant = DimText,
    outline = LineBorder,
    error = ErrorRed,
    onError = MistText
)

@Composable
fun KayaraTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = KayaraColorScheme,
        typography = Typography,
        content = content
    )
}
