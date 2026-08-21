package com.canvasstudio.ui.block.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasstudio.data.local.entity.BlockEntity
import com.canvasstudio.designsystem.components.CanvasButton
import com.canvasstudio.designsystem.components.CanvasButtonVariant
import com.canvasstudio.designsystem.components.CanvasTextField
import com.canvasstudio.ui.theme.CanvasColors
import kotlinx.serialization.json.*

@Composable
fun SidebarBlockInspector(
    block: BlockEntity,
    onDeselect: () -> Unit,
    onUpdateTitle: (String) -> Unit,
    onUpdateContentText: (String) -> Unit,
    onUpdateTextFormatting: (Int?, Boolean?, Boolean?, String?, String?) -> Unit,
    onUpdateChartAttribute: (String, Float) -> Unit,
    onUpdateImageUrl: (String) -> Unit,
    onUpdateValue: (Float?) -> Unit,
    onUpdateRealizadoEm: (String) -> Unit,
    onUpdatePagador: (String) -> Unit,
    onUpdateDestinatario: (String) -> Unit,
    onUpdateInstituicao: (String) -> Unit,
    onDuplicateBlock: (BlockEntity) -> Unit,
    onDeleteBlock: (BlockEntity) -> Unit,
    colors: CanvasColors
) {
    val metadata = remember(block.contentJson) {
        try { Json.parseToJsonElement(block.contentJson).jsonObject } catch (e: Exception) { null }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextSectionHeader("PROPRIEDADES DO BLOCO", colors)
            IconButton(onClick = onDeselect, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Rounded.Close, "Fechar", tint = colors.textMuted, modifier = Modifier.size(16.dp))
            }
        }

        CanvasTextField(
            value = block.title,
            onValueChange = onUpdateTitle,
            label = "Título do Bloco",
            modifier = Modifier.fillMaxWidth()
        )

        when (block.type.lowercase()) {
            "text" -> {
                Spacer(Modifier.height(8.dp))
                val rawText = metadata?.get("text")?.jsonPrimitive?.content ?: ""
                var localText by remember(rawText) { mutableStateOf(rawText) }
                CanvasTextField(
                    value = localText,
                    onValueChange = { localText = it; onUpdateContentText(it) },
                    label = "Conteúdo do Texto",
                    placeholder = "Edite o texto aqui ou direto no bloco...",
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))
                val align = metadata?.get("align")?.jsonPrimitive?.content ?: "left"
                val textColor = metadata?.get("textColor")?.jsonPrimitive?.content
                SidebarAlignmentSelector(align, { onUpdateTextFormatting(null, null, null, it, null) }, colors)
                Spacer(Modifier.height(8.dp))
                SidebarColorSelector(textColor, { onUpdateTextFormatting(null, null, null, null, it) }, colors)
            }
            "image" -> {
                Spacer(Modifier.height(8.dp))
                val url = metadata?.get("url")?.jsonPrimitive?.content ?: ""
                CanvasTextField(value = url, onValueChange = onUpdateImageUrl, label = "URL da Imagem", modifier = Modifier.fillMaxWidth())
            }
        }

        Spacer(Modifier.height(8.dp))
        val valorStr = metadata?.get("valor")?.jsonPrimitive?.floatOrNull?.toString() ?: ""
        CanvasTextField(value = valorStr, onValueChange = { onUpdateValue(it.toFloatOrNull()) }, label = "Valor (R$)", modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(8.dp))
        val pagador = metadata?.get("pagador")?.jsonPrimitive?.content ?: ""
        CanvasTextField(value = pagador, onValueChange = onUpdatePagador, label = "De (Pagador)", modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(8.dp))
        val destinatario = metadata?.get("destinatario")?.jsonPrimitive?.content ?: ""
        CanvasTextField(value = destinatario, onValueChange = onUpdateDestinatario, label = "Para (Destinatário)", modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CanvasButton("Duplicar", { onDuplicateBlock(block) }, variant = CanvasButtonVariant.Secondary, leadingIcon = Icons.Rounded.ContentCopy, modifier = Modifier.weight(1f))
            CanvasButton("Excluir", { onDeleteBlock(block) }, variant = CanvasButtonVariant.Danger, leadingIcon = Icons.Rounded.Delete, modifier = Modifier.weight(1f))
        }
    }
}
