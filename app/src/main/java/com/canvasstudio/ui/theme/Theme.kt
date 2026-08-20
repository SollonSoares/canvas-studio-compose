package com.canvasstudio.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class CanvasColors(
    val bgMain: Color,
    val bgMenu: Color,
    val bgCard: Color,
    val bgInput: Color,
    val bgButton: Color,
    val bgButtonHover: Color,
    val accent: Color,
    val canvasGrid: Color,
    val textMain: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val danger: Color,
    val border: Color,
    val borderSubtle: Color
)

private val LightCanvasColors = CanvasColors(
    bgMain = LightBgMain,
    bgMenu = LightBgMenu,
    bgCard = LightBgCard,
    bgInput = LightBgInput,
    bgButton = LightBgButton,
    bgButtonHover = LightBgButtonHover,
    accent = LightAccent,
    canvasGrid = LightCanvasGrid,
    textMain = LightTextMain,
    textSecondary = LightTextSecondary,
    textMuted = LightTextMuted,
    danger = LightDanger,
    border = LightBorder,
    borderSubtle = LightBorderSubtle
)

private val DarkCanvasColors = CanvasColors(
    bgMain = DarkBgMain,
    bgMenu = DarkBgMenu,
    bgCard = DarkCard,
    bgInput = DarkBgInput,
    bgButton = DarkBgButton,
    bgButtonHover = DarkBgButtonHover,
    accent = DarkAccent,
    canvasGrid = DarkCanvasGrid,
    textMain = DarkTextMain,
    textSecondary = DarkTextSecondary,
    textMuted = DarkTextMuted,
    danger = DarkDanger,
    border = DarkBorder,
    borderSubtle = DarkBorderSubtle
)

private val LocalCanvasColors = staticCompositionLocalOf { LightCanvasColors }

object CanvasTheme {
    val colors: CanvasColors
        @Composable
        @ReadOnlyComposable
        get() = LocalCanvasColors.current
}

@Composable
fun CanvasStudioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        darkColors(
            primary = DarkAccent,
            background = DarkBgMain,
            surface = DarkBgMenu,
            onPrimary = Color.White,
            onBackground = DarkTextMain,
            onSurface = DarkTextMain
        )
    } else {
        lightColors(
            primary = LightAccent,
            background = LightBgMain,
            surface = LightBgMenu,
            onPrimary = Color.White,
            onBackground = LightTextMain,
            onSurface = LightTextMain
        )
    }

    val canvasColors = if (darkTheme) DarkCanvasColors else LightCanvasColors

    CompositionLocalProvider(
        LocalCanvasColors provides canvasColors
    ) {
        MaterialTheme(
            colors = colors,
            content = content
        )
    }
}
