package com.canvasstudio.ui.block.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasstudio.data.local.entity.BlockEntity
import com.canvasstudio.designsystem.components.CanvasButton
import com.canvasstudio.designsystem.components.CanvasButtonVariant
import com.canvasstudio.designsystem.components.CanvasTextField
import com.canvasstudio.ui.block.dialogs.EditBlockChartInputs
import com.canvasstudio.ui.block.modules.ShinobiChartCalculator
import com.canvasstudio.ui.theme.CanvasColors
import kotlinx.serialization.json.*

@Composable
fun SidebarBlockInspector(
    block: BlockEntity,
    onDeselect: () -> Unit,
    onUpdateTitle: (String) -> Unit,
    onUpdateContentText: (String) -> Unit = {},
    onUpdateTextFormatting: (Int?, Boolean?, Boolean?, String?, String?) -> Unit,
    onInsertTable: () -> Unit = {},
    onInsertCallout: () -> Unit = {},
    onInsertCollapsible: () -> Unit = {},
    onInsertList: () -> Unit = {},
    onUpdateChartAttribute: (String, Float) -> Unit,
    onUpdateImageUrl: (String) -> Unit,
    onUpdateValue: (Float?) -> Unit = {},
    onUpdateRealizadoEm: (String) -> Unit = {},
    onUpdatePagador: (String) -> Unit = {},
    onUpdateDestinatario: (String) -> Unit = {},
    onUpdateInstituicao: (String) -> Unit = {},
    onDuplicateBlock: (BlockEntity) -> Unit,
    onDeleteBlock: (BlockEntity) -> Unit,
    colors: CanvasColors
) {
    val metadata = remember(block.contentJson) {
        try { Json.parseToJsonElement(block.contentJson).jsonObject } catch (e: Exception) { null }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Cabeçalho de Navegação & Fechar
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

        // 2. Card de Identificação do Bloco
        BlockMetadataCard(block, colors)

        // 3. Título do Bloco
        CanvasTextField(
            value = block.title,
            onValueChange = onUpdateTitle,
            label = "Título do Bloco",
            modifier = Modifier.fillMaxWidth()
        )

        // 4. Seção Específica por Tipo
        when (block.type.lowercase()) {
            "text" -> {
                TextSectionHeader("FORMATAÇÃO DO TEXTO", colors)

                val fontSize = metadata?.get("fontSize")?.jsonPrimitive?.intOrNull ?: 13
                val isBold = metadata?.get("isBold")?.jsonPrimitive?.booleanOrNull ?: false
                val isItalic = metadata?.get("isItalic")?.jsonPrimitive?.booleanOrNull ?: false
                val align = metadata?.get("align")?.jsonPrimitive?.content ?: "left"
                val textColor = metadata?.get("textColor")?.jsonPrimitive?.content

                // Formatação: Tamanho / Nível
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    LevelChip("Normal", fontSize == 13, colors) { onUpdateTextFormatting(13, null, null, null, null) }
                    LevelChip("H1", fontSize == 18, colors) { onUpdateTextFormatting(18, true, null, null, null) }
                    LevelChip("H2", fontSize == 15, colors) { onUpdateTextFormatting(15, true, null, null, null) }
                    LevelChip("H3", fontSize == 13 && isBold, colors) { onUpdateTextFormatting(13, true, null, null, null) }
                }

                // Formatação: Negrito e Itálico
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StyleToggleChip("B", isBold, colors) { onUpdateTextFormatting(null, !isBold, null, null, null) }
                    StyleToggleChip("I", isItalic, colors, isItalic = true) { onUpdateTextFormatting(null, null, !isItalic, null, null) }
                }

                // Alinhamento
                SidebarAlignmentSelector(align, { onUpdateTextFormatting(null, null, null, it, null) }, colors)

                // Cores
                SidebarColorSelector(textColor, { onUpdateTextFormatting(null, null, null, null, it) }, colors)

                Spacer(Modifier.height(4.dp))
                TextSectionHeader("INSERIR ESTRUTURA NO BLOCO", colors)

                // Inserção de Estrutura
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    StructureButton("+ Tabela", Icons.Rounded.TableChart, colors, onInsertTable)
                    StructureButton("+ Destaque", Icons.Rounded.Lightbulb, colors, onInsertCallout)
                    StructureButton("+ Expansível", Icons.Rounded.UnfoldMore, colors, onInsertCollapsible)
                    StructureButton("+ Lista", Icons.AutoMirrored.Rounded.FormatListBulleted, colors, onInsertList)
                }
            }

            "chart" -> {
                TextSectionHeader("ATRIBUTOS SHINOBI (0.5 a 8.0)", colors)

                val chartInputs = remember(block.contentJson) {
                    ShinobiChartCalculator.parseInputs(metadata ?: buildJsonObject {})
                }

                EditBlockChartInputs(
                    ninjutsu = chartInputs.ninjutsu,
                    onNinjutsuChange = { onUpdateChartAttribute("ninjutsu", it) },
                    inteligencia = chartInputs.inteligencia,
                    onInteligenciaChange = { onUpdateChartAttribute("inteligencia", it) },
                    chakra = chartInputs.chakraMax,
                    onChakraChange = { onUpdateChartAttribute("chakraMax", it) },
                    taijutsu = chartInputs.taijutsu,
                    onTaijutsuChange = { onUpdateChartAttribute("taijutsu", it) },
                    vigor = chartInputs.vigor,
                    onVigorChange = { onUpdateChartAttribute("vigor", it) },
                    genjutsu = chartInputs.genjutsu,
                    onGenjutsuChange = { onUpdateChartAttribute("genjutsu", it) },
                    colors = colors
                )
            }

            "image" -> {
                TextSectionHeader("CONFIGURAÇÃO DA IMAGEM", colors)
                val url = metadata?.get("url")?.jsonPrimitive?.content ?: ""
                CanvasTextField(
                    value = url,
                    onValueChange = onUpdateImageUrl,
                    label = "URL ou Caminho da Imagem",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        // 5. Botões de Ação
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CanvasButton("Duplicar", { onDuplicateBlock(block) }, variant = CanvasButtonVariant.Secondary, leadingIcon = Icons.Rounded.ContentCopy, modifier = Modifier.weight(1f))
            CanvasButton("Excluir", { onDeleteBlock(block) }, variant = CanvasButtonVariant.Danger, leadingIcon = Icons.Rounded.Delete, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun LevelChip(label: String, isSelected: Boolean, colors: CanvasColors, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(if (isSelected) colors.accent else colors.bgCard, RoundedCornerShape(6.dp))
            .border(1.dp, if (isSelected) colors.accent else colors.borderSubtle, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else colors.textMain,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StyleToggleChip(label: String, isSelected: Boolean, colors: CanvasColors, isItalic: Boolean = false, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(if (isSelected) colors.accent else colors.bgCard, RoundedCornerShape(6.dp))
            .border(1.dp, if (isSelected) colors.accent else colors.borderSubtle, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else colors.textMain,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal
        )
    }
}

@Composable
private fun StructureButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    colors: CanvasColors,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .background(colors.accent.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
            .border(1.dp, colors.accent.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, null, tint = colors.accent, modifier = Modifier.size(13.dp))
        Text(label, color = colors.accent, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BlockMetadataCard(block: BlockEntity, colors: CanvasColors) {
    val typeIcon = when (block.type.lowercase()) {
        "image" -> "🖼️ IMAGEM"
        "chart" -> "🥋 SHINOBI"
        else -> "📄 TEXTO & NOTAS"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.accent.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
            .border(1.dp, colors.accent.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Bloco #${block.id}", color = colors.accent, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                Text(typeIcon, color = colors.accent, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Posição: (${block.posX.toInt()}, ${block.posY.toInt()})", color = colors.textMuted, fontSize = 10.5.sp)
                Text("Tamanho: ${block.width} × ${block.height} dp", color = colors.textMuted, fontSize = 10.5.sp)
            }
        }
    }
}
