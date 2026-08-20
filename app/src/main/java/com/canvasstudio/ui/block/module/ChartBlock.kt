package com.canvasstudio.ui.block.modules

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.canvasstudio.data.local.entity.BlockEntity
import com.canvasstudio.ui.theme.CanvasColors
import kotlinx.serialization.json.Json
import kotlin.math.roundToInt
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ChartBlock(
    block: BlockEntity,
    modifier: Modifier = Modifier,
    colors: CanvasColors? = null
) {
    val accentColor = colors?.accent ?: Color(0xFF0071E3)
    val textColor = colors?.textMain ?: Color.Black
    val guideColor = colors?.canvasGrid ?: Color.Gray

    // Extração robusta dos status alinhada com a lógica do Studio Web
    val stats = remember(block.contentJson) {
        try {
            val json = Json.parseToJsonElement(block.contentJson).jsonObject
            // Tentamos extrair da estrutura "inputs" ou direto da raiz para compatibilidade
            val root = json["inputs"]?.jsonObject ?: json
            listOf(
                root["ninjutsu"]?.jsonPrimitive?.floatOrNull ?: 0f,
                root["inteligencia"]?.jsonPrimitive?.floatOrNull ?: 0f,
                root["chakraMax"]?.jsonPrimitive?.floatOrNull ?: root["chakra"]?.jsonPrimitive?.floatOrNull ?: 6f,
                root["taijutsu"]?.jsonPrimitive?.floatOrNull ?: 0f,
                root["vigor"]?.jsonPrimitive?.floatOrNull ?: 0f,
                root["genjutsu"]?.jsonPrimitive?.floatOrNull ?: 0f
            )
        } catch (e: Exception) {
            listOf(0f, 0f, 6f, 0f, 0f, 0f)
        }
    }

    // Lógica de normalização idêntica ao ChartModule.js (Fórmulas RPG Naruto)
    val normalizedStats = remember(stats) {
        val nin = (stats[0] / 10f) + 0.5f
        val intel = stats[1] + 0.5f
        val chk = (stats[2] - 6f) / 10f
        val tai = (stats[3] / 10f) + 0.5f
        val vig = stats[4] + 0.5f
        val gen = (stats[5] / 10f) + 0.5f

        val ajustarNota = { nota: Float ->
            val arredondado = (nota * 2).roundToInt() / 2f
            arredondado.coerceIn(0.5f, 8.0f)
        }

        listOf(
            ajustarNota(nin),
            ajustarNota(intel),
            ajustarNota(chk),
            ajustarNota(tai),
            ajustarNota(vig),
            ajustarNota(gen)
        )
    }

    val labels = listOf("NIN", "INT", "CHK", "TAI", "VIG", "GEN")
    val average = normalizedStats.average().toFloat()

    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = (size.minDimension / 2 * 0.65f).coerceAtLeast(10f)
                val tetoSistema = 8.0f

                // 1. Desenho das Guias Hexagonais (Radar Background)
                val levels = listOf(2f, 4f, 6f, 8f)
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
                    drawPath(
                        path = path,
                        color = guideColor.copy(alpha = 0.25f),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                // 2. Eixos Radiais e Labels (Identificadores de Atributos)
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

                    drawLine(
                        color = guideColor.copy(alpha = 0.5f),
                        start = center,
                        end = Offset(x, y),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Posicionamento inteligente das labels NIN, INT, etc.
                    val labelRadius = radius + 16.dp.toPx()
                    val lx = center.x + (labelRadius * cos(angle)).toFloat()
                    val ly = center.y + (labelRadius * sin(angle)).toFloat()
                    
                    val labelText = "${labels[i]} (${normalizedStats[i].let { if (it % 1 == 0f) it.toInt() else "%.1f".format(it) }})"
                    drawContext.canvas.nativeCanvas.drawText(labelText, lx, ly + 3.dp.toPx(), paint)
                }

                // 3. Polígono de Atributos (Status Polygon)
                val statPath = Path()
                for (i in 0 until 6) {
                    val angle = (i * PI / 3) - PI / 2
                    val r = (normalizedStats[i] / tetoSistema) * radius
                    val x = center.x + (r * cos(angle)).toFloat()
                    val y = center.y + (r * sin(angle)).toFloat()
                    if (i == 0) statPath.moveTo(x, y) else statPath.lineTo(x, y)
                }
                statPath.close()

                drawPath(
                    path = statPath,
                    color = Color(0xFFFF453A).copy(alpha = 0.4f), // --danger-subtle
                    style = Fill
                )
                drawPath(
                    path = statPath,
                    color = Color(0xFFFF453A), // --danger
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        // 4. Rodapé Informativo (Métrica de Média - Padrão Studio Web)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Média: ",
                color = accentColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "%.1f".format(average),
                color = accentColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 300, heightDp = 300)
@Composable
fun ChartBlockPreview() {
    val block = BlockEntity(
        id = 1,
        projectId = 0,
        title = "Status do Ninja",
        type = "chart",
        posX = 0f,
        posY = 0f,
        width = 300,
        height = 300,
        contentJson = """{
            "ninjutsu": 8.5,
            "inteligencia": 7.0,
            "chakra": 9.0,
            "taijutsu": 6.0,
            "vigor": 8.0,
            "genjutsu": 4.5
        }""".trimIndent()
    )
    
    val mockColors = CanvasColors(
        bgMain = Color.White,
        bgMenu = Color(0xFFF5F5F7),
        bgCard = Color.White,
        bgInput = Color.LightGray.copy(alpha = 0.1f),
        bgButton = Color.LightGray.copy(alpha = 0.2f),
        bgButtonHover = Color.LightGray.copy(alpha = 0.3f),
        accent = Color(0xFF0071E3),
        canvasGrid = Color.LightGray,
        textMain = Color.Black,
        textSecondary = Color.DarkGray,
        textMuted = Color.Gray,
        border = Color.LightGray,
        borderSubtle = Color.LightGray.copy(alpha = 0.5f)
    )

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        ChartBlock(block = block, colors = mockColors)
    }
}
