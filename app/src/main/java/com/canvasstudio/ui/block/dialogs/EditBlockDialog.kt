package com.canvasstudio.ui.block.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasstudio.data.local.entity.BlockEntity
import com.canvasstudio.ui.theme.CanvasColors
import kotlinx.serialization.json.*

@Composable
fun EditBlockDialog(
    block: BlockEntity, 
    onDismiss: () -> Unit, 
    onConfirm: (BlockEntity) -> Unit, 
    onLiveUpdate: (BlockEntity) -> Unit, 
    colors: CanvasColors
) {
    var title by remember { mutableStateOf(block.title) }
    var type by remember { mutableStateOf(block.type) }
    val initialContent = remember(block.contentJson) {
        try { 
            Json.parseToJsonElement(block.contentJson).jsonObject 
        } catch (e: Exception) { 
            buildJsonObject { 
                if (block.contentJson.isNotBlank() && !block.contentJson.startsWith("{")) {
                    put("text", block.contentJson)
                }
            } 
        }
    }
    
    var textContent by remember { mutableStateOf(initialContent["text"]?.jsonPrimitive?.content ?: "") }
    var imageUrl by remember { mutableStateOf(initialContent["url"]?.jsonPrimitive?.content ?: "") }
    
    var ninjutsu by remember { mutableFloatStateOf(initialContent["ninjutsu"]?.jsonPrimitive?.floatOrNull ?: initialContent["nin"]?.jsonPrimitive?.floatOrNull ?: 4f) }
    var inteligencia by remember { mutableFloatStateOf(initialContent["inteligencia"]?.jsonPrimitive?.floatOrNull ?: initialContent["int"]?.jsonPrimitive?.floatOrNull ?: 4f) }
    var chakra by remember { mutableFloatStateOf(initialContent["chakra"]?.jsonPrimitive?.floatOrNull ?: initialContent["cha"]?.jsonPrimitive?.floatOrNull ?: initialContent["chakraMax"]?.jsonPrimitive?.floatOrNull ?: 4f) }
    var taijutsu by remember { mutableFloatStateOf(initialContent["taijutsu"]?.jsonPrimitive?.floatOrNull ?: initialContent["tai"]?.jsonPrimitive?.floatOrNull ?: 4f) }
    var vigor by remember { mutableFloatStateOf(initialContent["vigor"]?.jsonPrimitive?.floatOrNull ?: initialContent["vig"]?.jsonPrimitive?.floatOrNull ?: 4f) }
    var genjutsu by remember { mutableFloatStateOf(initialContent["genjutsu"]?.jsonPrimitive?.floatOrNull ?: initialContent["gen"]?.jsonPrimitive?.floatOrNull ?: 4f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        backgroundColor = colors.bgMenu,
        shape = RoundedCornerShape(14.dp),
        title = { Text("Editar Bloco", color = colors.textMain, fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Text("Título", color = colors.textMain.copy(0.6f), fontSize = 12.sp)
                TextField(
                    value = title, 
                    onValueChange = { title = it }, 
                    modifier = Modifier.fillMaxWidth(), 
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = colors.textMain, 
                        cursorColor = colors.accent, 
                        focusedIndicatorColor = colors.accent, 
                        backgroundColor = Color.Transparent
                    )
                )
                
                Spacer(Modifier.height(16.dp))
                
                Text("Tipo", color = colors.textMain.copy(0.6f), fontSize = 12.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("text", "image", "chart").forEach { t ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { type = t }) {
                            RadioButton(
                                selected = type == t, 
                                onClick = { type = t }, 
                                colors = RadioButtonDefaults.colors(selectedColor = colors.accent)
                            )
                            Text(t.replaceFirstChar { it.uppercase() }, color = colors.textMain, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                
                Text("Conteúdo", color = colors.textMain.copy(0.6f), fontSize = 12.sp)
                when(type) {
                    "text" -> {
                        var textFieldValue by remember { mutableStateOf(TextFieldValue(textContent)) }
                        
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.Start) {
                            IconButton(onClick = { 
                                textFieldValue = insertTag(textFieldValue, "**", "**")
                                textContent = textFieldValue.text
                            }) { Icon(Icons.Default.FormatBold, null, tint = colors.accent) }
                            IconButton(onClick = { 
                                textFieldValue = insertTag(textFieldValue, "*", "*")
                                textContent = textFieldValue.text
                            }) { Icon(Icons.Default.FormatItalic, null, tint = colors.accent) }
                        }

                        TextField(
                            value = textFieldValue,
                            onValueChange = { 
                                textFieldValue = it
                                textContent = it.text 
                            },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                            placeholder = { Text("Use **negrito** ou *itálico*") },
                            colors = TextFieldDefaults.textFieldColors(
                                textColor = colors.textMain, 
                                cursorColor = colors.accent, 
                                focusedIndicatorColor = colors.accent, 
                                backgroundColor = Color.Transparent
                            )
                        )
                    }
                    "image" -> {
                        TextField(
                            value = imageUrl,
                            onValueChange = { imageUrl = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("URL da imagem") },
                            colors = TextFieldDefaults.textFieldColors(
                                textColor = colors.textMain, 
                                cursorColor = colors.accent, 
                                focusedIndicatorColor = colors.accent, 
                                backgroundColor = Color.Transparent
                            )
                        )
                    }
                    "chart" -> {
                        ChartSlider("Ninjutsu", ninjutsu, { ninjutsu = it }, colors)
                        ChartSlider("Inteligência", inteligencia, { inteligencia = it }, colors)
                        ChartSlider("Chakra", chakra, { chakra = it }, colors)
                        ChartSlider("Taijutsu", taijutsu, { taijutsu = it }, colors)
                        ChartSlider("Vigor", vigor, { vigor = it }, colors)
                        ChartSlider("Genjutsu", genjutsu, { genjutsu = it }, colors)
                    }
                }
            }
        },
        confirmButton = { 
            Button(
                onClick = { 
                    val newContent = when(type) {
                        "text" -> buildJsonObject { put("text", textContent); put("titleSize", 13); put("align", "left") }
                        "image" -> buildJsonObject { put("url", imageUrl) }
                        "chart" -> buildJsonObject {
                            put("ninjutsu", ninjutsu)
                            put("inteligencia", inteligencia)
                            put("chakra", chakra)
                            put("taijutsu", taijutsu)
                            put("vigor", vigor)
                            put("genjutsu", genjutsu)
                        }
                        else -> initialContent
                    }
                    onConfirm(block.copy(title = title, type = type, contentJson = newContent.toString())) 
                }, 
                colors = ButtonDefaults.buttonColors(backgroundColor = colors.accent),
                shape = RoundedCornerShape(6.dp)
            ) { Text("Salvar", color = Color.White) } 
        },
        dismissButton = { 
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(6.dp)
            ) { Text("Cancelar", color = colors.textMain.copy(0.6f)) } 
        }
    )
}

@Composable
private fun ChartSlider(label: String, value: Float, onValueChange: (Float) -> Unit, colors: CanvasColors) {
    Column(Modifier.padding(vertical = 4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = colors.textMain, fontSize = 12.sp)
            Text(
                text = if (value % 1 == 0f) value.toInt().toString() else "%.1f".format(value),
                color = colors.accent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..10f,
            steps = 19,
            colors = SliderDefaults.colors(thumbColor = colors.accent, activeTrackColor = colors.accent)
        )
    }
}

private fun insertTag(textFieldValue: TextFieldValue, startTag: String, endTag: String): TextFieldValue {
    val text = textFieldValue.text
    val selection = textFieldValue.selection
    val newText = text.substring(0, selection.start) + startTag + text.substring(selection.start, selection.end) + endTag + text.substring(selection.end)
    return textFieldValue.copy(text = newText, selection = TextRange(selection.start + startTag.length, selection.end + startTag.length))
}
