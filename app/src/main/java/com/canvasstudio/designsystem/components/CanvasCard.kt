package com.canvasstudio.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.canvasstudio.designsystem.CanvasTheme
import com.canvasstudio.designsystem.tokens.CanvasDimens

@Composable
fun CanvasCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = CanvasTheme.colors.bgCard,
    borderColor: Color = CanvasTheme.colors.borderSubtle,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 2.dp,
    shape: Shape = CanvasDimens.shapeLg,
    contentPadding: Dp = CanvasDimens.spaceMd,
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = backgroundColor,
        border = BorderStroke(borderWidth, borderColor),
        elevation = elevation
    ) {
        Box(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}
