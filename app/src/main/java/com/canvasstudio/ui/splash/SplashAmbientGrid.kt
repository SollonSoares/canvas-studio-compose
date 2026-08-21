package com.canvasstudio.ui.splash

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.canvasstudio.ui.theme.CanvasColors

@Composable
fun BoxScope.SplashAmbientGrid(
    pulseGlow: Float,
    alphaAnim: Float,
    isTeenage: Boolean,
    colors: CanvasColors
) {
    Box(
        modifier = Modifier
            .size(340.dp)
            .scale(pulseGlow)
            .alpha(alphaAnim * (if (isTeenage) 0.18f else 0.28f))
            .background(
                brush = Brush.radialGradient(colors = listOf(colors.accent, Color.Transparent)),
                shape = CircleShape
            )
    )

    Canvas(modifier = Modifier.fillMaxSize().alpha(alphaAnim * 0.22f)) {
        val step = 28.dp.toPx()
        var x = 0f
        while (x < size.width) {
            var y = 0f
            while (y < size.height) {
                if (isTeenage) {
                    drawLine(colors.accent.copy(alpha = 0.35f), Offset(x - 3.dp.toPx(), y), Offset(x + 3.dp.toPx(), y), 1.dp.toPx())
                    drawLine(colors.accent.copy(alpha = 0.35f), Offset(x, y - 3.dp.toPx()), Offset(x, y + 3.dp.toPx()), 1.dp.toPx())
                } else {
                    drawCircle(colors.accent.copy(alpha = 0.3f), 1.5.dp.toPx(), Offset(x, y))
                }
                y += step
            }
            x += step
        }
    }
}
