package com.canvasstudio.ui.block.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasstudio.data.local.entity.BlockEntity
import com.canvasstudio.ui.block.BlockViewModel
import com.canvasstudio.ui.block.model.HtmlDocumentBridge
import com.canvasstudio.ui.block.model.VisualElement
import com.canvasstudio.ui.block.modules.ChartBlock
import com.canvasstudio.ui.theme.CanvasColors
import kotlinx.serialization.json.*

@Composable
fun BlockContentEditor(
    block: BlockEntity,
    width: Int,
    height: Int,
    isLocked: Boolean,
    colors: CanvasColors,
    metadata: JsonObject?,
    parsedTextColor: Color?,
    fontSize: TextUnit,
    isBold: Boolean,
    isItalic: Boolean,
    blockTextAlign: TextAlign,
    viewModel: BlockViewModel
) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        when (block.type.lowercase()) {
            "chart" -> ChartBlock(
                block.copy(width = width, height = height),
                colors = colors
            )
            "image" -> ImageBlock(
                block.copy(width = width, height = height),
                viewModel = viewModel,
                colors = colors
            )
            else -> {
                // Bloco de Texto / Tabelas / Documentos: EDIÇÃO DIRETA NO BLOCO
                val rawContent = remember(metadata, block.contentJson) {
                    try {
                        val campos = metadata?.get("campos")?.jsonArray ?: metadata?.get("elements")?.jsonArray
                        if (!campos.isNullOrEmpty()) {
                            campos.joinToString("\n") { c ->
                                if (c is JsonObject) {
                                    c["html"]?.jsonPrimitive?.contentOrNull
                                        ?: c["text"]?.jsonPrimitive?.contentOrNull
                                        ?: c["value"]?.jsonPrimitive?.contentOrNull
                                        ?: ""
                                } else {
                                    c.jsonPrimitive.contentOrNull ?: ""
                                }
                            }
                        } else {
                            metadata?.get("text")?.jsonPrimitive?.contentOrNull
                                ?: metadata?.get("html")?.jsonPrimitive?.contentOrNull
                                ?: metadata?.get("value")?.jsonPrimitive?.contentOrNull
                                ?: block.contentJson
                        }
                    } catch (e: Exception) {
                        block.contentJson
                    }
                }

                val elements = remember(rawContent) {
                    mutableStateListOf<VisualElement>().apply {
                        addAll(HtmlDocumentBridge.fromHtml(rawContent))
                    }
                }

                // Sincronização ao alterar conteúdo no próprio bloco
                val onElementChange: () -> Unit = {
                    val updatedHtml = HtmlDocumentBridge.toHtml(elements.toList())
                    viewModel.updateBlockContentText(block, updatedHtml)
                }

                val hasSpecialNodes = elements.any {
                    it is VisualElement.Table || it is VisualElement.Heading || it is VisualElement.Callout || it is VisualElement.Collapsible || it is VisualElement.ListGroup
                }

                val verticalScroll = rememberScrollState()

                if (hasSpecialNodes) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(verticalScroll),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        elements.forEach { element ->
                            when (element) {
                                is VisualElement.Heading -> {
                                    var hText by remember(element.text) { mutableStateOf(element.text) }
                                    BasicTextField(
                                        value = hText,
                                        onValueChange = {
                                            hText = it
                                            element.text = it
                                            onElementChange()
                                        },
                                        textStyle = TextStyle(
                                            color = colors.accent,
                                            fontSize = if (element.level == 1) 16.sp else if (element.level == 2) 14.sp else 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = blockTextAlign
                                        ),
                                        cursorBrush = SolidColor(colors.accent),
                                        enabled = !isLocked,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                is VisualElement.Table -> {
                                    InPlaceBlockTable(
                                        table = element,
                                        isLocked = isLocked,
                                        colors = colors,
                                        onChanged = onElementChange
                                    )
                                }

                                is VisualElement.Collapsible -> {
                                    InPlaceBlockCollapsible(
                                        collapsible = element,
                                        isLocked = isLocked,
                                        fontSize = fontSize,
                                        blockTextAlign = blockTextAlign,
                                        colors = colors,
                                        onChanged = onElementChange
                                    )
                                }

                                is VisualElement.Callout -> {
                                    var cText by remember(element.text) { mutableStateOf(element.text) }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(colors.accent.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                            .border(0.5.dp, colors.accent.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                            .padding(6.dp)
                                    ) {
                                        BasicTextField(
                                            value = cText,
                                            onValueChange = {
                                                cText = it
                                                element.text = it
                                                onElementChange()
                                            },
                                            textStyle = TextStyle(
                                                color = colors.textMain,
                                                fontSize = (fontSize.value - 1f).sp,
                                                textAlign = blockTextAlign
                                            ),
                                            cursorBrush = SolidColor(colors.accent),
                                            enabled = !isLocked,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }

                                is VisualElement.ListGroup -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        element.items.forEachIndexed { itemIdx, itemText ->
                                            var itText by remember(itemText) { mutableStateOf(itemText) }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("• ", color = colors.accent, fontWeight = FontWeight.Bold, fontSize = fontSize)
                                                BasicTextField(
                                                    value = itText,
                                                    onValueChange = {
                                                        itText = it
                                                        element.items[itemIdx] = it
                                                        onElementChange()
                                                    },
                                                    textStyle = TextStyle(
                                                        color = parsedTextColor ?: colors.textMain,
                                                        fontSize = fontSize,
                                                        textAlign = blockTextAlign
                                                    ),
                                                    cursorBrush = SolidColor(colors.accent),
                                                    enabled = !isLocked,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    }
                                }

                                is VisualElement.Paragraph -> {
                                    var pText by remember(element.text) { mutableStateOf(element.text) }
                                    BasicTextField(
                                        value = pText,
                                        onValueChange = {
                                            pText = it
                                            element.text = it
                                            onElementChange()
                                        },
                                        textStyle = TextStyle(
                                            color = parsedTextColor ?: colors.textMain.copy(0.9f),
                                            fontSize = fontSize,
                                            fontWeight = if (isBold || element.isBold) FontWeight.Bold else FontWeight.Normal,
                                            fontStyle = if (isItalic || element.isItalic) FontStyle.Italic else FontStyle.Normal,
                                            textAlign = blockTextAlign,
                                            lineHeight = (fontSize.value * 1.35f).sp
                                        ),
                                        cursorBrush = SolidColor(colors.accent),
                                        enabled = !isLocked,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Texto Simples: Edição direta e fluida em todo o espaço do bloco
                    val cleanInitial = remember(rawContent) {
                        rawContent
                            .replace(Regex("<[^>]*>"), "")
                            .replace("&nbsp;", " ")
                            .replace("&amp;", "&")
                            .replace("&lt;", "<")
                            .replace("&gt;", ">")
                    }
                    var inlineText by remember(cleanInitial) { mutableStateOf(cleanInitial) }

                    Box(Modifier.fillMaxSize()) {
                        BasicTextField(
                            value = inlineText,
                            onValueChange = { inlineText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                                .onFocusChanged {
                                    if (!it.isFocused && inlineText != cleanInitial) {
                                        viewModel.updateBlockContentText(block, inlineText)
                                    }
                                },
                            enabled = !isLocked,
                            textStyle = TextStyle(
                                color = parsedTextColor ?: colors.textMain.copy(0.9f),
                                fontSize = fontSize,
                                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                                fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                                textAlign = blockTextAlign,
                                lineHeight = (fontSize.value * 1.35f).sp
                            ),
                            cursorBrush = SolidColor(colors.accent),
                            decorationBox = { innerTextField ->
                                if (inlineText.isEmpty()) {
                                    Text(
                                        "Escreva seu texto aqui...",
                                        color = colors.textMuted,
                                        fontSize = fontSize,
                                        textAlign = blockTextAlign,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Seção Expansível/Recolhível dentro do bloco:
 * Quando recolhido: resume-se às 3 primeiras linhas de texto.
 * Quando expandido: edição livre e completa do conteúdo.
 */
@Composable
private fun InPlaceBlockCollapsible(
    collapsible: VisualElement.Collapsible,
    isLocked: Boolean,
    fontSize: TextUnit,
    blockTextAlign: TextAlign,
    colors: CanvasColors,
    onChanged: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(collapsible.isExpanded) }
    var summaryText by remember(collapsible.summary) { mutableStateOf(collapsible.summary) }
    var contentText by remember(collapsible.content) { mutableStateOf(collapsible.content) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.accent.copy(alpha = 0.07f), RoundedCornerShape(6.dp))
            .border(0.8.dp, colors.accent.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(6.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            // Cabeçalho com Título e Seta Expansível/Recolhível
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowDown else Icons.Rounded.KeyboardArrowRight,
                        contentDescription = if (isExpanded) "Recolher" else "Expandir",
                        tint = colors.accent,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable {
                                isExpanded = !isExpanded
                                collapsible.isExpanded = isExpanded
                            }
                    )
                    BasicTextField(
                        value = summaryText,
                        onValueChange = {
                            summaryText = it
                            collapsible.summary = it
                            onChanged()
                        },
                        textStyle = TextStyle(
                            color = colors.accent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = blockTextAlign
                        ),
                        cursorBrush = SolidColor(colors.accent),
                        enabled = !isLocked,
                        modifier = Modifier.weight(1f)
                    )
                }

                IconButton(
                    onClick = {
                        isExpanded = !isExpanded
                        collapsible.isExpanded = isExpanded
                    },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Área de Conteúdo
            if (isExpanded) {
                // Expandido: Digitação livre de todo o texto
                BasicTextField(
                    value = contentText,
                    onValueChange = {
                        contentText = it
                        collapsible.content = it
                        onChanged()
                    },
                    textStyle = TextStyle(
                        color = colors.textMain,
                        fontSize = (fontSize.value - 1f).sp,
                        textAlign = blockTextAlign,
                        lineHeight = (fontSize.value * 1.3f).sp
                    ),
                    cursorBrush = SolidColor(colors.accent),
                    enabled = !isLocked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 4.dp, top = 2.dp, bottom = 2.dp)
                )
            } else {
                // Recolhido: Resume-se às 3 primeiras linhas
                val lines = contentText.lines()
                val previewText = lines.take(3).joinToString("\n") + if (lines.size > 3) "..." else ""
                Text(
                    text = previewText.ifBlank { "..." },
                    color = colors.textMuted,
                    fontSize = (fontSize.value - 1.5f).sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = blockTextAlign,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 4.dp)
                        .clickable {
                            isExpanded = true
                            collapsible.isExpanded = true
                        }
                )
            }
        }
    }
}

/**
 * Tabela editável diretamente dentro do bloco no Canvas com botões para adicionar linha e coluna.
 */
@Composable
private fun InPlaceBlockTable(
    table: VisualElement.Table,
    isLocked: Boolean,
    colors: CanvasColors,
    onChanged: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .border(0.8.dp, colors.borderSubtle, RoundedCornerShape(4.dp))
        ) {
            Column {
                // Cabeçalho da tabela
                Row(modifier = Modifier.background(colors.accent.copy(alpha = 0.12f))) {
                    table.headers.forEachIndexed { colIdx, headerText ->
                        var hVal by remember(headerText) { mutableStateOf(headerText) }
                        Box(
                            modifier = Modifier
                                .width(85.dp)
                                .border(0.4.dp, colors.borderSubtle)
                                .padding(horizontal = 4.dp, vertical = 3.dp)
                        ) {
                            BasicTextField(
                                value = hVal,
                                onValueChange = {
                                    hVal = it
                                    table.headers[colIdx] = it
                                    onChanged()
                                },
                                textStyle = TextStyle(
                                    color = colors.accent,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                cursorBrush = SolidColor(colors.accent),
                                enabled = !isLocked,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Linhas de dados da tabela
                table.rows.forEachIndexed { _, row ->
                    Row {
                        table.headers.indices.forEach { colIdx ->
                            val cellVal = row.getOrNull(colIdx) ?: ""
                            var cVal by remember(cellVal) { mutableStateOf(cellVal) }

                            Box(
                                modifier = Modifier
                                    .width(85.dp)
                                    .border(0.4.dp, colors.borderSubtle)
                                    .padding(horizontal = 4.dp, vertical = 3.dp)
                            ) {
                                BasicTextField(
                                    value = cVal,
                                    onValueChange = {
                                        cVal = it
                                        while (row.size <= colIdx) row.add("")
                                        row[colIdx] = it
                                        onChanged()
                                    },
                                    textStyle = TextStyle(
                                        color = colors.textMain,
                                        fontSize = 10.5.sp
                                    ),
                                    cursorBrush = SolidColor(colors.accent),
                                    enabled = !isLocked,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }

        // Ações rápidas da tabela no bloco (+ Linha, + Coluna)
        if (!isLocked) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TableMiniAction("+ Linha", colors) {
                    table.rows.add(MutableList(table.headers.size) { "" })
                    onChanged()
                }
                TableMiniAction("+ Coluna", colors) {
                    table.headers.add("Col ${table.headers.size + 1}")
                    table.rows.forEach { it.add("") }
                    onChanged()
                }
            }
        }
    }
}

@Composable
private fun TableMiniAction(label: String, colors: CanvasColors, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(colors.accent.copy(alpha = 0.1f), RoundedCornerShape(3.dp))
            .border(0.5.dp, colors.accent.copy(alpha = 0.25f), RoundedCornerShape(3.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(label, color = colors.accent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}
