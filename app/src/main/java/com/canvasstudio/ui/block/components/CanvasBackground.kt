package com.canvasstudio.ui.block.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CanvasBackground(
    scale: () -> Float = { 1f }, 
    offset: () -> Offset = { Offset.Zero }, 
    gridColor: Color
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val s = scale()
        val o = offset()
        
        val snapSize = 20.dp.toPx() * s
        val dotRadius = 1.2.dp.toPx() * s // Fiel ao CSS: 1.2px
        val startX = (o.x % snapSize) - snapSize
        val startY = (o.y % snapSize) - snapSize
        
        var x = startX
        while (x < size.width + snapSize) {
            var y = startY
            while (y < size.height + snapSize) {
                drawCircle(
                    color = gridColor,
                    radius = dotRadius,
                    center = Offset(x, y)
                )
                y += snapSize
            }
            x += snapSize
        }
    }
}
