package com.canvasstudio.ui.block.modules

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasstudio.data.local.entity.BlockEntity
import com.canvasstudio.ui.block.modules.ChartRadarRenderer.drawRadar
import com.canvasstudio.ui.theme.CanvasColors
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.ceil

@Composable
fun ChartBlock(
    block: BlockEntity,
    modifier: Modifier = Modifier,
    colors: CanvasColors? = null
) {
    val accentColor = colors?.accent ?: Color(0xFF0071E3)
    val textColor = colors?.textMain ?: Color.Black
    val guideColor = colors?.canvasGrid ?: Color.Gray

    val stats = remember(block.contentJson) {
        try {
            val json = Json.parseToJsonElement(block.contentJson).jsonObject
            val root = json["inputs"]?.jsonObject ?: json
            listOf(
                root["ninjutsu"]?.jsonPrimitive?.floatOrNull ?: root["nin"]?.jsonPrimitive?.floatOrNull ?: 0f,
                root["inteligencia"]?.jsonPrimitive?.floatOrNull ?: root["int"]?.jsonPrimitive?.floatOrNull ?: 0f,
                root["chakra"]?.jsonPrimitive?.floatOrNull ?: root["cha"]?.jsonPrimitive?.floatOrNull ?: root["chakraMax"]?.jsonPrimitive?.floatOrNull ?: 0f,
                root["taijutsu"]?.jsonPrimitive?.floatOrNull ?: root["tai"]?.jsonPrimitive?.floatOrNull ?: 0f,
                root["vigor"]?.jsonPrimitive?.floatOrNull ?: root["vig"]?.jsonPrimitive?.floatOrNull ?: 0f,
                root["genjutsu"]?.jsonPrimitive?.floatOrNull ?: root["gen"]?.jsonPrimitive?.floatOrNull ?: 0f
            )
        } catch (e: Exception) {
            listOf(0f, 0f, 0f, 0f, 0f, 0f)
        }
    }

    val labels = listOf("NIN", "INT", "CHK", "TAI", "VIG", "GEN")
    val average = if (stats.isNotEmpty()) stats.average().toFloat() else 0f

    val chartDescription = remember(stats, labels) {
        labels.mapIndexed { i, label ->
            val formattedVal = if (stats[i] % 1f == 0f) stats[i].toInt().toString() else "%.1f".format(java.util.Locale.US, stats[i])
            "$label ($formattedVal)"
        }.joinToString(", ")
    }

    val maxStat = stats.maxOrNull()?.coerceAtLeast(1f) ?: 10f
    val tetoSistema = if (maxStat <= 10f) 10f else if (maxStat <= 20f) 20f else if (maxStat <= 50f) 50f else if (maxStat <= 100f) 100f else (ceil(maxStat / 10f) * 10f)

    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .semantics { contentDescription = chartDescription },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRadar(stats, labels, tetoSistema, textColor, guideColor)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Média: ", color = accentColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(
                text = if (average % 1f == 0f) average.toInt().toString() else "%.1f".format(java.util.Locale.US, average),
                color = accentColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
