package com.canvasstudio.designsystem.tokens

import androidx.compose.ui.graphics.Color

enum class ThemeStyle(val id: String, val title: String, val author: String) {
    CUPERTINO("cupertino", "Apple Cupertino", "Sir Jony Ive"),
    TEENAGE_ENGINEERING("teenage", "Teenage Engineering", "Jesper Kouthoofd");

    companion object {
        fun fromId(id: String): ThemeStyle = values().firstOrNull { it.id.equals(id, ignoreCase = true) } ?: CUPERTINO
    }
}

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

// ==========================================
// 🍏 1. APPLE CUPERTINO TOKENS (Sir Jony Ive)
// ==========================================

val CupertinoDarkAccent = Color(0xFF0A84FF)   // Apple Blue
val CupertinoDarkAccentVariant = Color(0xFF5AC8FA)
val CupertinoDarkBgMain = Color(0xFF000000)   // Pure black OLED canvas
val CupertinoDarkBgMenu = Color(0xEB1C1C1E) // 0.92 alpha
val CupertinoDarkCard = Color(0xD12C2C2E)     // 0.82 alpha glassmorphism
val CupertinoDarkBgInput = Color(0x26767680)
val CupertinoDarkBgButton = Color(0x1F767680)
val CupertinoDarkBgButtonHover = Color(0x2E767680)
val CupertinoDarkTextMain = Color(0xFFFFFFFF)
val CupertinoDarkTextSecondary = Color(0xFFEBEBF5).copy(alpha = 0.6f)
val CupertinoDarkTextMuted = Color(0xFFEBEBF5).copy(alpha = 0.3f)
val CupertinoDarkDanger = Color(0xFFFF453A)   // Apple Red
val CupertinoDarkSuccess = Color(0xFF34C759)
val CupertinoDarkBadgePix = Color(0xFF32BCAD)
val CupertinoDarkBorder = Color(0x24FFFFFF)
val CupertinoDarkBorderSubtle = Color(0x14FFFFFF)
val CupertinoDarkCanvasGrid = Color(0x1AFFFFFF)

val CupertinoLightAccent = Color(0xFF007AFF) // Apple Blue
val CupertinoLightAccentVariant = Color(0xFF0056B3)
val CupertinoLightBgMain = Color(0xFFF2F2F7)
val CupertinoLightBgMenu = Color(0xEBFFFFFF) // 0.92 alpha
val CupertinoLightBgCard = Color(0xD1FFFFFF) // 0.82 alpha glassmorphism
val CupertinoLightBgInput = Color(0x0F000000)
val CupertinoLightBgButton = Color(0x0A000000)
val CupertinoLightBgButtonHover = Color(0x14000000)
val CupertinoLightTextMain = Color(0xFF000000)
val CupertinoLightTextSecondary = Color(0xFF3C3C43).copy(alpha = 0.6f)
val CupertinoLightTextMuted = Color(0xFF3C3C43).copy(alpha = 0.3f)
val CupertinoLightDanger = Color(0xFFFF3B30) // Apple Red
val CupertinoLightSuccess = Color(0xFF28A745)
val CupertinoLightBadgePix = Color(0xFF00A896)
val CupertinoLightBorder = Color(0x24000000)
val CupertinoLightBorderSubtle = Color(0x12000000)
val CupertinoLightCanvasGrid = Color(0x0A000000)

val DarkCanvasColors = CanvasColors(
    bgMain = CupertinoDarkBgMain,
    bgMenu = CupertinoDarkBgMenu,
    bgCard = CupertinoDarkCard,
    bgInput = CupertinoDarkBgInput,
    bgButton = CupertinoDarkBgButton,
    bgButtonHover = CupertinoDarkBgButtonHover,
    accent = CupertinoDarkAccent,
    accentVariant = CupertinoDarkAccentVariant,
    canvasGrid = CupertinoDarkCanvasGrid,
    textMain = CupertinoDarkTextMain,
    textSecondary = CupertinoDarkTextSecondary,
    textMuted = CupertinoDarkTextMuted,
    danger = CupertinoDarkDanger,
    success = CupertinoDarkSuccess,
    badgePix = CupertinoDarkBadgePix,
    border = CupertinoDarkBorder,
    borderSubtle = CupertinoDarkBorderSubtle
)

val LightCanvasColors = CanvasColors(
    bgMain = CupertinoLightBgMain,
    bgMenu = CupertinoLightBgMenu,
    bgCard = CupertinoLightBgCard,
    bgInput = CupertinoLightBgInput,
    bgButton = CupertinoLightBgButton,
    bgButtonHover = CupertinoLightBgButtonHover,
    accent = CupertinoLightAccent,
    accentVariant = CupertinoLightAccentVariant,
    canvasGrid = CupertinoLightCanvasGrid,
    textMain = CupertinoLightTextMain,
    textSecondary = CupertinoLightTextSecondary,
    textMuted = CupertinoLightTextMuted,
    danger = CupertinoLightDanger,
    success = CupertinoLightSuccess,
    badgePix = CupertinoLightBadgePix,
    border = CupertinoLightBorder,
    borderSubtle = CupertinoLightBorderSubtle
)

// ========================================================
// 🎛️ 2. TEENAGE ENGINEERING TOKENS (Jesper Kouthoofd)
// ========================================================

val TeenageDarkAccent = Color(0xFFFF4500)        // OP-1 Iconic Signal Orange
val TeenageDarkAccentVariant = Color(0xFFFFCC00) // Rotary Synth Yellow
val TeenageDarkBgMain = Color(0xFF101113)        // Industrial Matte Graphite
val TeenageDarkBgMenu = Color(0xFF1A1C1E)        // Studio Unit Chassis
val TeenageDarkCard = Color(0xFF222528)          // Hardware Module Surface
val TeenageDarkBgInput = Color(0xFF2D3136)       // Recessed Slider Track
val TeenageDarkBgButton = Color(0xFF33383E)
val TeenageDarkBgButtonHover = Color(0xFF424850)
val TeenageDarkTextMain = Color(0xFFF0F0EE)      // Crisp Chalk White
val TeenageDarkTextSecondary = Color(0xFFA5AAB0) // Anodized Metal Gray
val TeenageDarkTextMuted = Color(0xFF6B7280)
val TeenageDarkDanger = Color(0xFFFF2E4D)        // Emergency Red Switch
val TeenageDarkSuccess = Color(0xFF26E070)       // Signal Green LED
val TeenageDarkBadgePix = Color(0xFF00E5FF)      // Electric Synth Cyan
val TeenageDarkBorder = Color(0xFF3E444C)        // Machined Chamfer
val TeenageDarkBorderSubtle = Color(0xFF2B3036)
val TeenageDarkCanvasGrid = Color(0xFF1E2125)

val TeenageLightAccent = Color(0xFFFF3E00)       // OP-1 Iconic Signal Orange
val TeenageLightAccentVariant = Color(0xFFFFB700)// Rotary Synth Yellow
val TeenageLightBgMain = Color(0xFFE8E8E6)       // Matte Industrial Concrete / Chalk
val TeenageLightBgMenu = Color(0xFFF5F5F3)       // Studio Desk Surface
val TeenageLightCard = Color(0xFFFFFFFF)         // White Module Plate
val TeenageLightBgInput = Color(0xFFDCDCD8)      // Recessed Slider Track
val TeenageLightBgButton = Color(0xFFD0D0CC)
val TeenageLightBgButtonHover = Color(0xFFC4C4BF)
val TeenageLightTextMain = Color(0xFF141517)     // High-Contrast Ink
val TeenageLightTextSecondary = Color(0xFF55595E)
val TeenageLightTextMuted = Color(0xFF888E96)
val TeenageLightDanger = Color(0xFFE62E44)
val TeenageLightSuccess = Color(0xFF10B981)
val TeenageLightBadgePix = Color(0xFF00B4D8)
val TeenageLightBorder = Color(0xFFB0B0A8)
val TeenageLightBorderSubtle = Color(0xFFD4D4CE)
val TeenageLightCanvasGrid = Color(0xFFD0D0CB)

val TeenageDarkColors = CanvasColors(
    bgMain = TeenageDarkBgMain,
    bgMenu = TeenageDarkBgMenu,
    bgCard = TeenageDarkCard,
    bgInput = TeenageDarkBgInput,
    bgButton = TeenageDarkBgButton,
    bgButtonHover = TeenageDarkBgButtonHover,
    accent = TeenageDarkAccent,
    accentVariant = TeenageDarkAccentVariant,
    canvasGrid = TeenageDarkCanvasGrid,
    textMain = TeenageDarkTextMain,
    textSecondary = TeenageDarkTextSecondary,
    textMuted = TeenageDarkTextMuted,
    danger = TeenageDarkDanger,
    success = TeenageDarkSuccess,
    badgePix = TeenageDarkBadgePix,
    border = TeenageDarkBorder,
    borderSubtle = TeenageDarkBorderSubtle
)

val TeenageLightColors = CanvasColors(
    bgMain = TeenageLightBgMain,
    bgMenu = TeenageLightBgMenu,
    bgCard = TeenageLightCard,
    bgInput = TeenageLightBgInput,
    bgButton = TeenageLightBgButton,
    bgButtonHover = TeenageLightBgButtonHover,
    accent = TeenageLightAccent,
    accentVariant = TeenageLightAccentVariant,
    canvasGrid = TeenageLightCanvasGrid,
    textMain = TeenageLightTextMain,
    textSecondary = TeenageLightTextSecondary,
    textMuted = TeenageLightTextMuted,
    danger = TeenageLightDanger,
    success = TeenageLightSuccess,
    badgePix = TeenageLightBadgePix,
    border = TeenageLightBorder,
    borderSubtle = TeenageLightBorderSubtle
)

fun getCanvasColors(themeStyle: ThemeStyle, isDark: Boolean): CanvasColors {
    return when (themeStyle) {
        ThemeStyle.CUPERTINO -> if (isDark) DarkCanvasColors else LightCanvasColors
        ThemeStyle.TEENAGE_ENGINEERING -> if (isDark) TeenageDarkColors else TeenageLightColors
    }
}
