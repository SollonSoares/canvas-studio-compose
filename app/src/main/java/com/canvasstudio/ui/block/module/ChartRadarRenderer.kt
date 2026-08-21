package com.canvasstudio.ui.block.modules

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object ChartRadarRenderer {

    fun DrawScope.drawRadar(
        stats: List<Float>,
        labels: List<String>,
        tetoSistema: Float,
        textColor: Color,
        guideColor: Color
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = (size.minDimension / 2 * 0.65f).coerceAtLeast(10f)

        // 1. Guias Hexagonais
        val levels = listOf(tetoSistema * 0.25f, tetoSistema * 0.5f, tetoSistema * 0.75f, tetoSistema)
        levels.forEach { level ->
            val r = (level / tetoSistema) * radius
            val path = Path()
            for (i in 0 until 6) {
                val angle = (i * PI / 3) - PI / 2
                val x = center.x + (r * cos(angle)).toFloat()
                val y = center.y + (r * sin(angle)).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path = path, color = guideColor.copy(alpha = 0.25f), style = Stroke(width = 1.dp.toPx()))
        }

        // 2. Eixos e Labels
        val paint = Paint().apply {
            color = textColor.toArgb()
            textSize = 10.sp.toPx()
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        for (i in 0 until 6) {
            val angle = (i * PI / 3) - PI / 2
            val x = center.x + (radius * cos(angle)).toFloat()
            val y = center.y + (radius * sin(angle)).toFloat()

            drawLine(color = guideColor.copy(alpha = 0.5f), start = center, end = Offset(x, y), strokeWidth = 1.dp.toPx())

            val labelRadius = radius + 16.dp.toPx()
            val lx = center.x + (labelRadius * cos(angle)).toFloat()
            val ly = center.y + (labelRadius * sin(angle)).toFloat()

            val formattedVal = if (stats[i] % 1f == 0f) stats[i].toInt().toString() else "%.1f".format(java.util.Locale.US, stats[i])
            val labelText = "${labels[i]} ($formattedVal)"
            drawContext.canvas.nativeCanvas.drawText(labelText, lx, ly + 3.dp.toPx(), paint)
        }

        // 3. Polígono de Atributos
        val statPath = Path()
        for (i in 0 until 6) {
            val angle = (i * PI / 3) - PI / 2
            val r = ((stats[i] / tetoSistema).coerceIn(0f, 1f)) * radius
            val x = center.x + (r * cos(angle)).toFloat()
            val y = center.y + (r * sin(angle)).toFloat()
            if (i == 0) statPath.moveTo(x, y) else statPath.lineTo(x, y)
        }
        statPath.close()

        drawPath(path = statPath, color = Color(0xFFFF453A).copy(alpha = 0.4f), style = Fill)
        drawPath(path = statPath, color = Color(0xFFFF453A), style = Stroke(width = 2.dp.toPx()))
    }
}
