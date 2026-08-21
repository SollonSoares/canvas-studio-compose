package com.canvasstudio.designsystem.tokens

import androidx.compose.ui.graphics.Color

data class CanvasColors(
    val bgMain: Color,
    val bgMenu: Color,
    val bgCard: Color,
    val bgInput: Color,
    val bgButton: Color,
    val bgButtonHover: Color,
    val accent: Color,
    val accentVariant: Color = accent,
    val canvasGrid: Color,
    val textMain: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val danger: Color,
    val success: Color = Color(0xFF34C759),
    val badgePix: Color = Color(0xFF32BCAD),
    val border: Color,
    val borderSubtle: Color
)

// Dark Colors (macOS/iOS inspired tokens - Blue Accent)
val DarkAccent = Color(0xFF0A84FF)   // Apple Blue
val DarkAccentVariant = Color(0xFF5AC8FA)
val DarkBgMain = Color(0xFF000000)   // Pure black canvas
val DarkBgMenu = Color(0xEB1C1C1E) // 0.92 alpha
val DarkCard = Color(0xD12C2C2E)     // 0.82 alpha glassmorphism
val DarkBgInput = Color(0x26767680)
val DarkBgButton = Color(0x1F767680)
val DarkBgButtonHover = Color(0x2E767680)
val DarkTextMain = Color(0xFFFFFFFF)
val DarkTextSecondary = Color(0xFFEBEBF5).copy(alpha = 0.6f)
val DarkTextMuted = Color(0xFFEBEBF5).copy(alpha = 0.3f)
val DarkDanger = Color(0xFFFF453A)   // Apple Red
val DarkSuccess = Color(0xFF34C759)
val DarkBadgePix = Color(0xFF32BCAD)
val DarkBorder = Color(0x24FFFFFF)
val DarkBorderSubtle = Color(0x14FFFFFF)
val DarkCanvasGrid = Color(0x1AFFFFFF)

// Light Colors (macOS/iOS inspired tokens - Blue Accent)
val LightAccent = Color(0xFF007AFF) // Apple Blue
val LightAccentVariant = Color(0xFF0056B3)
val LightBgMain = Color(0xFFF2F2F7)
val LightBgMenu = Color(0xEBFFFFFF) // 0.92 alpha
val LightBgCard = Color(0xD1FFFFFF) // 0.82 alpha glassmorphism
val LightBgInput = Color(0x0F000000)
val LightBgButton = Color(0x0A000000)
val LightBgButtonHover = Color(0x14000000)
val LightTextMain = Color(0xFF000000)
val LightTextSecondary = Color(0xFF3C3C43).copy(alpha = 0.6f)
val LightTextMuted = Color(0xFF3C3C43).copy(alpha = 0.3f)
val LightDanger = Color(0xFFFF3B30) // Apple Red
val LightSuccess = Color(0xFF28A745)
val LightBadgePix = Color(0xFF00A896)
val LightBorder = Color(0x24000000)
val LightBorderSubtle = Color(0x12000000)
val LightCanvasGrid = Color(0x0A000000)

val DarkCanvasColors = CanvasColors(
    bgMain = DarkBgMain,
    bgMenu = DarkBgMenu,
    bgCard = DarkCard,
    bgInput = DarkBgInput,
    bgButton = DarkBgButton,
    bgButtonHover = DarkBgButtonHover,
    accent = DarkAccent,
    accentVariant = DarkAccentVariant,
    canvasGrid = DarkCanvasGrid,
    textMain = DarkTextMain,
    textSecondary = DarkTextSecondary,
    textMuted = DarkTextMuted,
    danger = DarkDanger,
    success = DarkSuccess,
    badgePix = DarkBadgePix,
    border = DarkBorder,
    borderSubtle = DarkBorderSubtle
)

val LightCanvasColors = CanvasColors(
    bgMain = LightBgMain,
    bgMenu = LightBgMenu,
    bgCard = LightBgCard,
    bgInput = LightBgInput,
    bgButton = LightBgButton,
    bgButtonHover = LightBgButtonHover,
    accent = LightAccent,
    accentVariant = LightAccentVariant,
    canvasGrid = LightCanvasGrid,
    textMain = LightTextMain,
    textSecondary = LightTextSecondary,
    textMuted = LightTextMuted,
    danger = LightDanger,
    success = LightSuccess,
    badgePix = LightBadgePix,
    border = LightBorder,
    borderSubtle = LightBorderSubtle
)
