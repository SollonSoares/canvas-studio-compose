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
import androidx.compose.ui.text.font.FontFamily
import com.canvasstudio.designsystem.tokens.*

private val LocalCanvasColors = staticCompositionLocalOf { LightCanvasColors }
private val LocalCanvasShapes = staticCompositionLocalOf { CupertinoShapes }

object CanvasTheme {
    val colors: CanvasColors
        @Composable
        @ReadOnlyComposable
        get() = LocalCanvasColors.current

    val shapes: CanvasShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalCanvasShapes.current

    val isTeenage: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalCanvasShapes.current.isTeenage

    val fontFamily: FontFamily
        @Composable
        @ReadOnlyComposable
        get() = if (LocalCanvasShapes.current.isMonospace) FontFamily.Monospace else FontFamily.Default
}

@Composable
fun CanvasStudioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeStyle: String = "cupertino",
    content: @Composable () -> Unit
) {
    val resolvedStyle = ThemeStyle.fromId(themeStyle)
    val canvasColors = getCanvasColors(resolvedStyle, darkTheme)
    val canvasShapes = when (resolvedStyle) {
        ThemeStyle.CUPERTINO -> CupertinoShapes
        ThemeStyle.TEENAGE_ENGINEERING -> TeenageShapes
    }

    val materialColors = if (darkTheme) {
        darkColors(
            primary = canvasColors.accent,
            background = canvasColors.bgMain,
            surface = canvasColors.bgMenu,
            onPrimary = Color.White,
            onBackground = canvasColors.textMain,
            onSurface = canvasColors.textMain
        )
    } else {
        lightColors(
            primary = canvasColors.accent,
            background = canvasColors.bgMain,
            surface = canvasColors.bgMenu,
            onPrimary = Color.White,
            onBackground = canvasColors.textMain,
            onSurface = canvasColors.textMain
        )
    }

    CompositionLocalProvider(
        LocalCanvasColors provides canvasColors,
        LocalCanvasShapes provides canvasShapes
    ) {
        MaterialTheme(
            colors = materialColors,
            content = content
        )
    }
}
