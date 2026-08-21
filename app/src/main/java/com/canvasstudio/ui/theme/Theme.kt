package com.canvasstudio.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.canvasstudio.designsystem.CanvasTheme
import com.canvasstudio.designsystem.tokens.CanvasColors

// Backward-compatible typealiases and delegators
typealias CanvasColors = com.canvasstudio.designsystem.tokens.CanvasColors

val CanvasTheme = com.canvasstudio.designsystem.CanvasTheme

@Composable
fun CanvasStudioTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    com.canvasstudio.designsystem.CanvasStudioTheme(darkTheme = darkTheme, content = content)
}
