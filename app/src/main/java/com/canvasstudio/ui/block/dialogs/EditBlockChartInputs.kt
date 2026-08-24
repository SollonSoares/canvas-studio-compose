package com.canvasstudio.ui.block.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasstudio.designsystem.tokens.CanvasDimens
import com.canvasstudio.ui.block.modules.ShinobiChartCalculator
import com.canvasstudio.ui.block.modules.ShinobiInputs
import com.canvasstudio.ui.theme.CanvasColors
import java.util.Locale

@Composable
fun EditBlockChartInputs(
    ninjutsu: Float, onNinjutsuChange: (Float) -> Unit,
    inteligencia: Float, onInteligenciaChange: (Float) -> Unit,
    chakra: Float, onChakraChange: (Float) -> Unit,
    taijutsu: Float, onTaijutsuChange: (Float) -> Unit,
    vigor: Float, onVigorChange: (Float) -> Unit,
    genjutsu: Float, onGenjutsuChange: (Float) -> Unit,
    colors: CanvasColors
) {
    val inputs = ShinobiInputs(
        taijutsu = taijutsu,
        ninjutsu = ninjutsu,
        genjutsu = genjutsu,
        vigor = vigor,
        inteligencia = inteligencia,
        chakraMax = chakra
    )
    val notas = ShinobiChartCalculator.calcularNotas(inputs)

    // Header com a Média Geral Shinobi
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .background(Color(0xFFFF453A).copy(alpha = 0.12f), CanvasDimens.shapeMd)
            .border(1.dp, Color(0xFFFF453A).copy(alpha = 0.3f), CanvasDimens.shapeMd)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "🥋 STATUS SHINOBI (0.5 - 8.0)",
            color = colors.textMain,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Média: ${notas.formattedMedia()}",
            color = Color(0xFFFF453A),
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }

    ChartValueRow("NIN% (Ninjutsu)", ninjutsu, notas.ninjutsu, onNinjutsuChange, max = 100f, step = 5f, colors)
    ChartValueRow("INT+ (Inteligência)", inteligencia, notas.inteligencia, onInteligenciaChange, max = 20f, step = 1f, colors)
    ChartValueRow("CHK+ (Chakra Base 6)", chakra, notas.chakra, onChakraChange, max = 200f, step = 10f, colors)
    ChartValueRow("TAI% (Taijutsu)", taijutsu, notas.taijutsu, onTaijutsuChange, max = 100f, step = 5f, colors)
    ChartValueRow("VIG+ (Vigor)", vigor, notas.vigor, onVigorChange, max = 20f, step = 1f, colors)
    ChartValueRow("GEN% (Genjutsu)", genjutsu, notas.genjutsu, onGenjutsuChange, max = 100f, step = 5f, colors)
}

@Composable
private fun ChartValueRow(
    label: String,
    value: Float,
    notaCalculada: Float,
    onValueChange: (Float) -> Unit,
    max: Float = 100f,
    step: Float = 1f,
    colors: CanvasColors
) {
    var textValue by remember(value) { mutableStateOf(if (value % 1f == 0f) value.toInt().toString() else value.toString()) }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label, color = colors.textMain, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(
                text = "Nota: ${String.format(Locale.US, "%.1f", notaCalculada)}",
                color = Color(0xFFFF453A),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onValueChange((value - step).coerceAtLeast(0f)) }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Remove, "Diminuir $label", tint = colors.accent, modifier = Modifier.size(16.dp))
            }
            OutlinedTextField(
                value = textValue,
                onValueChange = { newText ->
                    textValue = newText
                    val parsed = newText.replace(",", ".").toFloatOrNull()
                    if (parsed != null && parsed >= 0f) onValueChange(parsed.coerceAtMost(max)) else if (newText.isBlank()) onValueChange(0f)
                },
                modifier = Modifier
                    .width(75.dp)
                    .semantics { contentDescription = "Campo $label" },
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, color = colors.textMain),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = colors.textMain,
                    cursorColor = colors.accent,
                    focusedBorderColor = colors.accent,
                    unfocusedBorderColor = colors.borderSubtle,
                    backgroundColor = Color.Transparent
                ),
                shape = CanvasDimens.shapeMd
            )
            IconButton(onClick = { onValueChange((value + step).coerceAtMost(max)) }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Add, "Aumentar $label", tint = colors.accent, modifier = Modifier.size(16.dp))
            }
        }
    }
}
