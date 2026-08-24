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
import kotlinx.serialization.json.jsonObject
import java.util.Locale

@Composable
fun ChartBlock(
    block: BlockEntity,
    modifier: Modifier = Modifier,
    colors: CanvasColors? = null
) {
    val textColor = colors?.textMain ?: Color.Black
    val guideColor = colors?.canvasGrid ?: Color.Gray

    val shinobiNotas = remember(block.contentJson) {
        try {
            val json = Json.parseToJsonElement(block.contentJson).jsonObject
            val inputs = ShinobiChartCalculator.parseInputs(json)
            ShinobiChartCalculator.calcularNotas(inputs)
        } catch (e: Exception) {
            ShinobiChartCalculator.calcularNotas(ShinobiInputs())
        }
    }

    val chartDescription = remember(shinobiNotas) {
        ShinobiChartCalculator.LABELS.mapIndexed { i, label ->
            val v = shinobiNotas.asList[i]
            "$label (${String.format(Locale.US, "%.1f", v)})"
        }.joinToString(", ") + ", Média Geral: ${shinobiNotas.formattedMedia()}"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp, vertical = 6.dp),
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
                drawRadar(
                    stats = shinobiNotas.asList,
                    labels = ShinobiChartCalculator.LABELS,
                    tetoSistema = ShinobiChartCalculator.TETO_SISTEMA,
                    textColor = textColor,
                    guideColor = guideColor
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp, bottom = 2.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Média Geral: ",
                color = Color(0xFFFF453A),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = shinobiNotas.formattedMedia(),
                color = Color(0xFFFF453A),
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
