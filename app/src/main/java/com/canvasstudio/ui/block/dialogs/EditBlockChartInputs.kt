package com.canvasstudio.ui.block.dialogs

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
import com.canvasstudio.ui.theme.CanvasColors

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
    Text(text = "Valores dos Atributos:", color = colors.textMuted, fontSize = 12.sp, modifier = Modifier.padding(bottom = 6.dp))
    ChartValueRow("Ninjutsu", ninjutsu, onNinjutsuChange, colors)
    ChartValueRow("Inteligência", inteligencia, onInteligenciaChange, colors)
    ChartValueRow("Chakra", chakra, onChakraChange, colors)
    ChartValueRow("Taijutsu", taijutsu, onTaijutsuChange, colors)
    ChartValueRow("Vigor", vigor, onVigorChange, colors)
    ChartValueRow("Genjutsu", genjutsu, onGenjutsuChange, colors)
}

@Composable
private fun ChartValueRow(label: String, value: Float, onValueChange: (Float) -> Unit, colors: CanvasColors) {
    var textValue by remember(value) { mutableStateOf(value.toString().removeSuffix(".0")) }

    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = colors.textMain, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onValueChange((value - 1f).coerceAtLeast(0f)) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Remove, "Diminuir $label", tint = colors.accent, modifier = Modifier.size(18.dp))
            }
            OutlinedTextField(
                value = textValue,
                onValueChange = { newText ->
                    textValue = newText
                    val parsed = newText.replace(",", ".").toFloatOrNull()
                    if (parsed != null && parsed >= 0f) onValueChange(parsed) else if (newText.isBlank()) onValueChange(0f)
                },
                modifier = Modifier.width(85.dp).semantics { contentDescription = "Campo $label" },
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, color = colors.textMain),
                colors = TextFieldDefaults.outlinedTextFieldColors(textColor = colors.textMain, cursorColor = colors.accent, focusedBorderColor = colors.accent, unfocusedBorderColor = colors.borderSubtle, backgroundColor = Color.Transparent),
                shape = CanvasDimens.shapeMd
            )
            IconButton(onClick = { onValueChange(value + 1f) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Add, "Aumentar $label", tint = colors.accent, modifier = Modifier.size(18.dp))
            }
        }
    }
}
