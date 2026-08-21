package com.canvasstudio.ui.block.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
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

    fun buildCurrentContent(): String {
        return when (type) {
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
        }.toString()
    }

    LaunchedEffect(title, type, textContent, imageUrl, ninjutsu, inteligencia, chakra, taijutsu, vigor, genjutsu) {
        onLiveUpdate(block.copy(title = title, type = type, contentJson = buildCurrentContent()))
    }

    CanvasModal(
        title = "Editar Bloco",
        onDismiss = onDismiss,
        confirmButton = {
            CanvasButton(
                onClick = { onConfirm(block.copy(title = title, type = type, contentJson = buildCurrentContent())) },
                variant = CanvasButtonVariant.Primary
            ) { Text("Salvar", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            CanvasButton(onClick = onDismiss, variant = CanvasButtonVariant.Ghost) { Text("Cancelar") }
        }
    ) {
        CanvasSectionHeader("Título do Bloco")
        Spacer(Modifier.height(CanvasDimens.spaceXs))
        CanvasTextField(
            value = title,
            onValueChange = { title = it },
            placeholder = "Título do bloco...",
            modifier = Modifier.semantics { contentDescription = "Campo Título" }
        )

        CanvasDivider()

        CanvasSectionHeader("Tipo de Bloco")
        Spacer(Modifier.height(CanvasDimens.spaceXs))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(CanvasDimens.spaceSm)) {
            listOf("text" to "Texto", "image" to "Imagem", "chart" to "Radar").forEach { (t, label) ->
                val isSelected = type == t
                OutlinedButton(
                    onClick = { type = t },
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, if (isSelected) colors.accent else colors.borderSubtle),
                    shape = CanvasDimens.shapeMd,
                    colors = ButtonDefaults.outlinedButtonColors(backgroundColor = if (isSelected) colors.accent.copy(alpha = 0.12f) else Color.Transparent)
                ) {
                    Text(text = label, color = if (isSelected) colors.accent else colors.textMain, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, fontSize = 12.sp)
                }
            }
        }

        CanvasDivider()

        CanvasSectionHeader("Conteúdo")
        Spacer(Modifier.height(CanvasDimens.spaceXs))
        EditBlockContentInputs(
            type, textContent, { textContent = it },
            imageUrl, { imageUrl = it },
            ninjutsu, { ninjutsu = it },
            inteligencia, { inteligencia = it },
            chakra, { chakra = it },
            taijutsu, { taijutsu = it },
            vigor, { vigor = it },
            genjutsu, { genjutsu = it },
            colors
        )
    }
}
