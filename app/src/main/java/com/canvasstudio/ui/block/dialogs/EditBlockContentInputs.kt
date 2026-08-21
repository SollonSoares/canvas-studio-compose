package com.canvasstudio.ui.block.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.canvasstudio.designsystem.components.CanvasTextField
import com.canvasstudio.designsystem.tokens.CanvasDimens
import com.canvasstudio.ui.theme.CanvasColors

@Composable
fun EditBlockContentInputs(
    type: String,
    textContent: String,
    onTextContentChange: (String) -> Unit,
    imageUrl: String,
    onImageUrlChange: (String) -> Unit,
    ninjutsu: Float, onNinjutsuChange: (Float) -> Unit,
    inteligencia: Float, onInteligenciaChange: (Float) -> Unit,
    chakra: Float, onChakraChange: (Float) -> Unit,
    taijutsu: Float, onTaijutsuChange: (Float) -> Unit,
    vigor: Float, onVigorChange: (Float) -> Unit,
    genjutsu: Float, onGenjutsuChange: (Float) -> Unit,
    colors: CanvasColors
) {
    when (type) {
        "text" -> {
            var textFieldValue by remember { mutableStateOf(TextFieldValue(textContent)) }

            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                IconButton(onClick = {
                    textFieldValue = insertTag(textFieldValue, "**", "**")
                    onTextContentChange(textFieldValue.text)
                }) { Icon(Icons.Default.FormatBold, "Negrito", tint = colors.accent) }
                IconButton(onClick = {
                    textFieldValue = insertTag(textFieldValue, "*", "*")
                    onTextContentChange(textFieldValue.text)
                }) { Icon(Icons.Default.FormatItalic, "Itálico", tint = colors.accent) }
            }

            TextField(
                value = textFieldValue,
                onValueChange = {
                    textFieldValue = it
                    onTextContentChange(it.text)
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 110.dp).semantics { contentDescription = "Campo Conteúdo Texto" },
                placeholder = { Text("Use **negrito** ou *itálico*", color = colors.textMuted.copy(alpha = 0.5f)) },
                shape = CanvasDimens.shapeMd,
                colors = TextFieldDefaults.textFieldColors(textColor = colors.textMain, cursorColor = colors.accent, focusedIndicatorColor = colors.accent, backgroundColor = colors.bgInput)
            )
        }
        "image" -> {
            CanvasTextField(
                value = imageUrl,
                onValueChange = onImageUrlChange,
                placeholder = "https://... ou URI do arquivo",
                label = "URL ou Caminho da Imagem",
                modifier = Modifier.semantics { contentDescription = "Campo URL Imagem" }
            )
        }
        "chart" -> {
            EditBlockChartInputs(
                ninjutsu, onNinjutsuChange,
                inteligencia, onInteligenciaChange,
                chakra, onChakraChange,
                taijutsu, onTaijutsuChange,
                vigor, onVigorChange,
                genjutsu, onGenjutsuChange,
                colors
            )
        }
    }
}

private fun insertTag(textFieldValue: TextFieldValue, startTag: String, endTag: String): TextFieldValue {
    val text = textFieldValue.text
    val selection = textFieldValue.selection
    val newText = text.substring(0, selection.start) + startTag + text.substring(selection.start, selection.end) + endTag + text.substring(selection.end)
    return textFieldValue.copy(text = newText, selection = TextRange(selection.start + startTag.length, selection.end + startTag.length))
}
