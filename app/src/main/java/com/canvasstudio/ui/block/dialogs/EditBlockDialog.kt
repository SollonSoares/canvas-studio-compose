package com.canvasstudio.ui.block.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasstudio.data.local.entity.BlockEntity
import com.canvasstudio.designsystem.CanvasTheme
import com.canvasstudio.designsystem.components.*
import com.canvasstudio.designsystem.tokens.CanvasDimens
import com.canvasstudio.ui.theme.CanvasColors
import kotlinx.serialization.json.*

@Composable
fun EditBlockDialog(
    block: BlockEntity, 
    onDismiss: () -> Unit, 
    onConfirm: (BlockEntity) -> Unit, 
    onLiveUpdate: (BlockEntity) -> Unit, 
    colors: CanvasColors = CanvasTheme.colors
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

    LaunchedEffect(title, type, textContent, imageUrl, ninjutsu, inteligencia, chakra, taijutsu, vigor, genjutsu) {
        val currentContent = when(type) {
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
        onLiveUpdate(block.copy(title = title, type = type, contentJson = currentContent.toString()))
    }

    CanvasModal(
        title = "Editar Bloco",
        onDismiss = onDismiss,
        confirmButton = {
            CanvasButton(
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
                variant = CanvasButtonVariant.Primary
            ) {
                Text("Salvar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            CanvasButton(
                onClick = onDismiss,
                variant = CanvasButtonVariant.Ghost
            ) {
                Text("Cancelar")
            }
        }
    ) {
        CanvasSectionHeader("Título do Bloco")
        Spacer(Modifier.height(CanvasDimens.spaceXs))
        CanvasTextField(
            value = title,
            onValueChange = { title = it },
            placeholder = "Título do bloco..."
        )
        
        CanvasDivider()

        CanvasSectionHeader("Tipo de Bloco")
        Spacer(Modifier.height(CanvasDimens.spaceXs))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CanvasDimens.spaceSm)
        ) {
            listOf(
                Pair("text", "Texto"),
                Pair("image", "Imagem"),
                Pair("chart", "Radar")
            ).forEach { (t, label) ->
                val isSelected = type == t
                OutlinedButton(
                    onClick = { type = t },
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, if (isSelected) colors.accent else colors.borderSubtle),
                    shape = CanvasDimens.shapeMd,
                    colors = ButtonDefaults.outlinedButtonColors(
                        backgroundColor = if (isSelected) colors.accent.copy(alpha = 0.12f) else Color.Transparent
                    )
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) colors.accent else colors.textMain,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }
            }
        }

        CanvasDivider()

        CanvasSectionHeader("Conteúdo")
        Spacer(Modifier.height(CanvasDimens.spaceXs))
        when(type) {
            "text" -> {
                var textFieldValue by remember { mutableStateOf(TextFieldValue(textContent)) }
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(onClick = { 
                        textFieldValue = insertTag(textFieldValue, "**", "**")
                        textContent = textFieldValue.text
                    }) { Icon(Icons.Default.FormatBold, "Negrito", tint = colors.accent) }
                    IconButton(onClick = { 
                        textFieldValue = insertTag(textFieldValue, "*", "*")
                        textContent = textFieldValue.text
                    }) { Icon(Icons.Default.FormatItalic, "Itálico", tint = colors.accent) }
                }

                TextField(
                    value = textFieldValue,
                    onValueChange = { 
                        textFieldValue = it
                        textContent = it.text 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 110.dp)
                        .semantics { contentDescription = "Campo Conteúdo Texto" },
                    placeholder = { Text("Use **negrito** ou *itálico*", color = colors.textMuted.copy(alpha = 0.5f)) },
                    shape = CanvasDimens.shapeMd,
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = colors.textMain, 
                        cursorColor = colors.accent, 
                        focusedIndicatorColor = colors.accent, 
                        backgroundColor = colors.bgInput
                    )
                )
            }
            "image" -> {
                CanvasTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    placeholder = "https://... ou URI do arquivo",
                    label = "URL ou Caminho da Imagem"
                )
            }
            "chart" -> {
                Text(
                    text = "Valores dos Atributos:", 
                    color = colors.textMuted, 
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                ChartValueInput("Ninjutsu", ninjutsu, { ninjutsu = it }, colors)
                ChartValueInput("Inteligência", inteligencia, { inteligencia = it }, colors)
                ChartValueInput("Chakra", chakra, { chakra = it }, colors)
                ChartValueInput("Taijutsu", taijutsu, { taijutsu = it }, colors)
                ChartValueInput("Vigor", vigor, { vigor = it }, colors)
                ChartValueInput("Genjutsu", genjutsu, { genjutsu = it }, colors)
            }
        }
    }
}

@Composable
private fun ChartValueInput(
    label: String, 
    value: Float, 
    onValueChange: (Float) -> Unit, 
    colors: CanvasColors
) {
    var textValue by remember(value) { mutableStateOf(value.toString().removeSuffix(".0")) }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp), 
        horizontalArrangement = Arrangement.SpaceBetween, 
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = colors.textMain, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = {
                    val newVal = (value - 1f).coerceAtLeast(0f)
                    onValueChange(newVal)
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Remove, 
                    contentDescription = "Diminuir $label", 
                    tint = colors.accent, 
                    modifier = Modifier.size(18.dp)
                )
            }

            OutlinedTextField(
                value = textValue,
                onValueChange = { newText ->
                    textValue = newText
                    val cleanText = newText.replace(",", ".")
                    val parsed = cleanText.toFloatOrNull()
                    if (parsed != null && parsed >= 0f) {
                        onValueChange(parsed)
                    } else if (newText.isBlank()) {
                        onValueChange(0f)
                    }
                },
                modifier = Modifier
                    .width(85.dp)
                    .semantics { contentDescription = "Campo $label" },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = colors.textMain
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = colors.textMain,
                    cursorColor = colors.accent,
                    focusedBorderColor = colors.accent,
                    unfocusedBorderColor = colors.borderSubtle,
                    backgroundColor = Color.Transparent
                ),
                shape = CanvasDimens.shapeMd
            )

            IconButton(
                onClick = {
                    val newVal = value + 1f
                    onValueChange(newVal)
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Aumentar $label",
                    tint = colors.accent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun insertTag(textFieldValue: TextFieldValue, startTag: String, endTag: String): TextFieldValue {
    val text = textFieldValue.text
    val selection = textFieldValue.selection
    val newText = text.substring(0, selection.start) + startTag + text.substring(selection.start, selection.end) + endTag + text.substring(selection.end)
    return textFieldValue.copy(text = newText, selection = TextRange(selection.start + startTag.length, selection.end + startTag.length))
}
