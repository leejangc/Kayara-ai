package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Kayara AI Luxury Futuristic Cosmic Color Palette
val InkBackground = Color(0xFF0C091A)
val DeepSpace = Color(0xFF070510)
val PanelSurface = Color(0xFF16122C)
val PanelSurfaceVariant = Color(0xFF221B42)
val PanelSurfaceElevated = Color(0xFF2D2355)

// Vibrant Neon & Rose Accents
val RosePrimary = Color(0xFFFF2A6D)
val RoseDark = Color(0xFFB5174B)
val RoseSubtle = Color(0x33FF2A6D)
val RoseGlow = Color(0x4DFF2A6D)

// Celestial Gold & Amber Accents
val GoldAccent = Color(0xFFFFBE0B)
val GoldSubtle = Color(0x33FFBE0B)

// Live Status & Cyber Green / Cyan
val NeonCyan = Color(0xFF00F5D4)
val NeonEmerald = Color(0xFF06D6A0)
val EmeraldGlow = Color(0x4006D6A0)
val ElectricViolet = Color(0xFF8338EC)

// High-clarity Text Hierarchy
val MistText = Color(0xFFF9F7FD)
val DimText = Color(0xFFABA4C9)
val MutedText = Color(0xFF756D94)

// Borders & Glass highlights
val LineBorder = Color(0x2EFFFFFF)
val LineBorderSubtle = Color(0x1AFFFFFF)
val GlassSurface = Color(0x1F221B42)
val GlassHighlight = Color(0x26FFFFFF)

val ErrorRed = Color(0xFFFF3366)
val ThinkingDot = Color(0xFFD4C8FA)

// Signature Gradients
val CosmicGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF1A0F35),
        Color(0xFF0E0922),
        Color(0xFF070510)
    )
)

val RoseGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFFF2A6D),
        Color(0xFF8338EC)
    )
)

val GoldGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFFFBE0B),
        Color(0xFFFF2A6D)
    )
)

val LiveEmeraldGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF06D6A0),
        Color(0xFF00F5D4)
    )
)
