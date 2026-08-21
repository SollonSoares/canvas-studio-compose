package com.canvasstudio.ui.block.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.canvasstudio.designsystem.CanvasTheme

@Composable
fun CanvasBackground(
    scale: () -> Float = { 1f }, 
    offset: () -> Offset = { Offset.Zero }, 
    gridColor: Color
) {
    val isTeenage = CanvasTheme.isTeenage
    val paint = remember {
        Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            strokeCap = Paint.Cap.ROUND
        }
    }
    
    // FloatArray pré-alocado reutilizável para zero alocação de memória por frame
    val floatArray = remember { FloatArray(16000) }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val s = scale()
        val o = offset()
        
        val baseSnap = 24.dp.toPx()
        var snapSize = baseSnap * s
        if (snapSize <= 0f) return@Canvas
        
        // Nível de detalhe (LOD): mantém a densidade constante ao afastar o zoom
        while (snapSize < 18.dp.toPx()) {
            snapSize *= 2f
        }
        
        val strokeWidth = (2.2.dp.toPx() * s).coerceIn(1.2.dp.toPx(), 3.5.dp.toPx())
        paint.color = gridColor.toArgb()
        paint.strokeWidth = strokeWidth
        
        val startX = ((o.x % snapSize) + snapSize) % snapSize - snapSize
        val startY = ((o.y % snapSize) + snapSize) % snapSize - snapSize
        
        var index = 0
        val maxFloats = floatArray.size - 8
        var x = startX
        
        if (isTeenage) {
            // Estilo Teenage Engineering: Cruzamentos técnicos (+) de precisão de estúdio
            val arm = (3.dp.toPx() * s).coerceIn(1.5.dp.toPx(), 4.5.dp.toPx())
            while (x < size.width + snapSize && index < maxFloats) {
                var y = startY
                while (y < size.height + snapSize && index < maxFloats) {
                    // Linha Horizontal da Cruzeta
                    floatArray[index++] = x - arm
                    floatArray[index++] = y
                    floatArray[index++] = x + arm
                    floatArray[index++] = y
                    // Linha Vertical da Cruzeta
                    floatArray[index++] = x
                    floatArray[index++] = y - arm
                    floatArray[index++] = x
                    floatArray[index++] = y + arm
                    y += snapSize
                }
                x += snapSize
            }
            if (index > 0) {
                drawContext.canvas.nativeCanvas.drawLines(floatArray, 0, index, paint)
            }
        } else {
            // Estilo Apple Cupertino: Pontos circulares polidos
            while (x < size.width + snapSize && index < maxFloats) {
                var y = startY
                while (y < size.height + snapSize && index < maxFloats) {
                    floatArray[index++] = x
                    floatArray[index++] = y
                    y += snapSize
                }
                x += snapSize
            }
            if (index > 0) {
                drawContext.canvas.nativeCanvas.drawPoints(floatArray, 0, index, paint)
            }
        }
    }
}
