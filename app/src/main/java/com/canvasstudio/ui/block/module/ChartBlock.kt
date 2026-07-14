package com.canvasstudio.ui.block.modules

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.canvasstudio.data.local.entity.BlockEntity
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.floatOrNull
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

import com.canvasstudio.ui.block.CanvasColors

@Composable
fun ChartBlock(
    block: BlockEntity,
    modifier: Modifier = Modifier,
    colors: CanvasColors? = null
) {
    // FASE 3: Deserialização pura e extração dos atributos de status do contentJson
    val stats = try {
        val json = Json.parseToJsonElement(block.contentJson).jsonObject
        listOf(
            json["ninjutsu"]?.jsonPrimitive?.floatOrNull ?: 0f,
            json["inteligencia"]?.jsonPrimitive?.floatOrNull ?: 0f,
            json["chakra"]?.jsonPrimitive?.floatOrNull ?: 0f,
            json["taijutsu"]?.jsonPrimitive?.floatOrNull ?: 0f,
            json["vigor"]?.jsonPrimitive?.floatOrNull ?: 0f,
            json["genjutsu"]?.jsonPrimitive?.floatOrNull ?: 0f
        )
    } catch (e: Exception) {
        listOf(0f, 0f, 0f, 0f, 0f, 0f)
    }

    val primaryColor = Color(0xFFFF453A)
    val guideColor = colors?.canvasGrid ?: Color.Gray

    Column(
        modifier = modifier.fillMaxSize().padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(modifier = Modifier.weight(1f).aspectRatio(1f)) {
            val radius = size.minDimension / 2 * 0.65f
            val center = Offset(size.width / 2, size.height / 2)
            val maxScore = 8f

            // Desenho: Guias Concêntricas
            val guideLevels = listOf(2f, 4f, 6f, 8f)
            guideLevels.forEach { level ->
                val path = Path()
                for (i in 0 until 6) {
                    val angle = (i * PI / 3) - PI / 2
                    val r = (level / maxScore) * radius
                    val x = center.x + (r * cos(angle)).toFloat()
                    val y = center.y + (r * sin(angle)).toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                drawPath(path, color = guideColor.copy(alpha = 0.25f), style = Stroke(width = 1f))
            }

            // Desenho: Eixos
            for (i in 0 until 6) {
                val angle = (i * PI / 3) - PI / 2
                val x = center.x + (radius * cos(angle)).toFloat()
                val y = center.y + (radius * sin(angle)).toFloat()
                drawLine(color = guideColor.copy(alpha = 0.5f), start = center, end = Offset(x, y), strokeWidth = 1f)
            }

            // Desenho: Polígono de Status Preenchido
            val statPath = Path()
            for (i in 0 until 6) {
                val angle = (i * PI / 3) - PI / 2
                val r = (stats[i] / maxScore) * radius
                val x = center.x + (r * cos(angle)).toFloat()
                val y = center.y + (r * sin(angle)).toFloat()
                if (i == 0) statPath.moveTo(x, y) else statPath.lineTo(x, y)
            }
            statPath.close()
            drawPath(statPath, color = primaryColor.copy(alpha = 0.4f), style = Fill)
            drawPath(statPath, color = primaryColor, style = Stroke(width = 2f))
        }
    }
}
