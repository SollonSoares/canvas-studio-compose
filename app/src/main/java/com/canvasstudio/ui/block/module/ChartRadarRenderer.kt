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
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object ChartRadarRenderer {

    fun DrawScope.drawRadar(
        stats: List<Float>,
        labels: List<String> = ShinobiChartCalculator.LABELS,
        tetoSistema: Float = ShinobiChartCalculator.TETO_SISTEMA,
        textColor: Color,
        guideColor: Color
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = (size.minDimension / 2 * 0.65f).coerceAtLeast(10f)

        // 1. Níveis Guia Concêntricos (2, 4, 6, 8)
        val levels = ShinobiChartCalculator.NIVEIS_GUIA
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

        // 2. Linhas dos Eixos e Rótulos com Notas Formatadas
        val paint = Paint().apply {
            color = textColor.toArgb()
            textSize = 10.sp.toPx()
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        for (i in 0 until 6) {
            val angle = (i * PI / 3) - PI / 2
            val xPonta = center.x + (radius * cos(angle)).toFloat()
            val yPonta = center.y + (radius * sin(angle)).toFloat()

            // Linha do eixo
            drawLine(
                color = guideColor.copy(alpha = 0.4f),
                start = center,
                end = Offset(xPonta, yPonta),
                strokeWidth = 1.dp.toPx()
            )

            // Rótulo: NOME (X.X)
            val margemTexto = radius + 16.dp.toPx()
            val xTexto = center.x + (margemTexto * cos(angle)).toFloat()
            val yTexto = center.y + (margemTexto * sin(angle)).toFloat()

            val valor = if (i < stats.size) stats[i] else 0.5f
            val formattedVal = String.format(Locale.US, "%.1f", valor)
            val labelText = "${labels[i]} ($formattedVal)"
            drawContext.canvas.nativeCanvas.drawText(labelText, xTexto, yTexto + 3.5.dp.toPx(), paint)
        }

        // 3. Polígono de Dados Shinobi (Vermelho Naruto)
        val statPath = Path()
        for (i in 0 until 6) {
            val valor = if (i < stats.size) stats[i] else 0.5f
            val r = (valor / tetoSistema).coerceIn(0f, 1f) * radius
            val angle = (i * PI / 3) - PI / 2
            val x = center.x + (r * cos(angle)).toFloat()
            val y = center.y + (r * sin(angle)).toFloat()
            if (i == 0) statPath.moveTo(x, y) else statPath.lineTo(x, y)
        }
        statPath.close()

        drawPath(path = statPath, color = Color(0xFFFF453A).copy(alpha = 0.35f), style = Fill)
        drawPath(path = statPath, color = Color(0xFFFF453A), style = Stroke(width = 2.dp.toPx()))
    }
}
