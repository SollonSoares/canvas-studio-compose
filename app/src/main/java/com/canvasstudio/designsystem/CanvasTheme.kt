package com.canvasstudio.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.canvasstudio.designsystem.tokens.*

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
