package com.canvasstudio.ui.block.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasstudio.designsystem.components.CanvasTextField
import com.canvasstudio.ui.block.model.HtmlDocumentBridge
import com.canvasstudio.ui.block.model.VisualElement
import com.canvasstudio.ui.theme.CanvasColors

@Composable
fun VisualBlockDocumentEditor(
    rawContent: String,
    onContentChange: (String) -> Unit,
    colors: CanvasColors
) {
    // 1. Carregar elementos visuais a partir do HTML/Texto
    val elements = remember(rawContent) {
        mutableStateListOf<VisualElement>().apply {
            addAll(HtmlDocumentBridge.fromHtml(rawContent))
        }
    }

    // Função de notificação para salvar o HTML resultante
    val syncHtml = {
        val updatedHtml = HtmlDocumentBridge.toHtml(elements.toList())
        onContentChange(updatedHtml)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 2. Barra Superior de Adição de Elementos Visuais
        VisualElementAddToolbar(
            onAddHeading = {
                elements.add(VisualElement.Heading(text = "Novo Título"))
                syncHtml()
            },
            onAddTable = {
                elements.add(VisualElement.Table())
                syncHtml()
            },
            onAddParagraph = {
                elements.add(VisualElement.Paragraph(text = "Novo texto..."))
                syncHtml()
            },
            onAddCallout = {
                elements.add(VisualElement.Callout(text = "Texto de aviso ou destaque..."))
                syncHtml()
            },
            onAddList = {
                elements.add(VisualElement.ListGroup())
                syncHtml()
            },
            colors = colors
        )

        // 3. Renderização e Edição Interativa dos Elementos
        elements.forEachIndexed { index, element ->
            key(element.id) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.bgCard, RoundedCornerShape(8.dp))
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Linha de Cabeçalho do Elemento com Botão de Exclusão
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (element) {
                                    is VisualElement.Heading -> "🏷️ TÍTULO H${element.level}"
                                    is VisualElement.Table -> "📊 TABELA (${element.rows.size} Linhas × ${element.headers.size} Colunas)"
                                    is VisualElement.Callout -> "💡 DESTAQUE"
                                    is VisualElement.Collapsible -> "📂 SEÇÃO EXPANSÍVEL"
                                    is VisualElement.ListGroup -> "• LISTA (${element.items.size} Itens)"
                                    is VisualElement.Paragraph -> "📝 TEXTO"
                                },
                                color = colors.accent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )

                            IconButton(
                                onClick = {
                                    elements.removeAt(index)
                                    syncHtml()
                                },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(Icons.Rounded.Delete, "Remover", tint = colors.danger, modifier = Modifier.size(15.dp))
                            }
                        }

                        // Editor Específico de Cada Tipo
                        when (element) {
                            is VisualElement.Heading -> {
                                HeadingEditor(
                                    heading = element,
                                    onChanged = { syncHtml() },
                                    colors = colors
                                )
                            }
                            is VisualElement.Table -> {
                                TableVisualGridEditor(
                                    table = element,
                                    onChanged = { syncHtml() },
                                    colors = colors
                                )
                            }
                            is VisualElement.Callout -> {
                                CalloutVisualEditor(
                                    callout = element,
                                    onChanged = { syncHtml() },
                                    colors = colors
                                )
                            }
                            is VisualElement.Collapsible -> {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    CanvasTextField(
                                        value = element.summary,
                                        onValueChange = {
                                            element.summary = it
                                            syncHtml()
                                        },
                                        label = "Título da Seção",
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    CanvasTextField(
                                        value = element.content,
                                        onValueChange = {
                                            element.content = it
                                            syncHtml()
                                        },
                                        label = "Conteúdo",
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                            is VisualElement.ListGroup -> {
                                ListGroupVisualEditor(
                                    listGroup = element,
                                    onChanged = { syncHtml() },
                                    colors = colors
                                )
                            }
                            is VisualElement.Paragraph -> {
                                ParagraphVisualEditor(
                                    paragraph = element,
                                    onChanged = { syncHtml() },
                                    colors = colors
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VisualElementAddToolbar(
    onAddHeading: () -> Unit,
    onAddTable: () -> Unit,
    onAddParagraph: () -> Unit,
    onAddCallout: () -> Unit,
    onAddList: () -> Unit,
    colors: CanvasColors
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        AddElementChip("+ Título", Icons.Rounded.Title, colors, onAddHeading)
        AddElementChip("+ Tabela", Icons.Rounded.TableChart, colors, onAddTable)
        AddElementChip("+ Texto", Icons.Rounded.Notes, colors, onAddParagraph)
        AddElementChip("+ Destaque", Icons.Rounded.Lightbulb, colors, onAddCallout)
        AddElementChip("+ Lista", Icons.Rounded.FormatListBulleted, colors, onAddList)
    }
}

@Composable
private fun AddElementChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    colors: CanvasColors,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .background(colors.accent.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
            .border(1.dp, colors.accent.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, null, tint = colors.accent, modifier = Modifier.size(14.dp))
        Text(label, color = colors.accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun HeadingEditor(
    heading: VisualElement.Heading,
    onChanged: () -> Unit,
    colors: CanvasColors
) {
    var text by remember(heading.text) { mutableStateOf(heading.text) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Seletor de Nível H1, H2, H3
        Row(
            modifier = Modifier
                .background(colors.bgMain, RoundedCornerShape(6.dp))
                .padding(2.dp)
        ) {
            listOf(1, 2, 3).forEach { lvl ->
                val isSelected = heading.level == lvl
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) colors.accent else Color.Transparent,
                            RoundedCornerShape(4.dp)
                        )
                        .clickable {
                            heading.level = lvl
                            onChanged()
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        "H$lvl",
                        color = if (isSelected) Color.White else colors.textMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Campo de Texto do Título
        BasicTextField(
            value = text,
            onValueChange = {
                text = it
                heading.text = it
                onChanged()
            },
            textStyle = TextStyle(
                color = colors.accent,
                fontSize = if (heading.level == 1) 16.sp else 14.sp,
                fontWeight = FontWeight.Bold
            ),
            cursorBrush = SolidColor(colors.accent),
            modifier = Modifier
                .weight(1f)
                .background(colors.bgMain, RoundedCornerShape(6.dp))
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(6.dp))
                .padding(8.dp)
        )
    }
}

@Composable
private fun ParagraphVisualEditor(
    paragraph: VisualElement.Paragraph,
    onChanged: () -> Unit,
    colors: CanvasColors
) {
    var text by remember(paragraph.text) { mutableStateOf(paragraph.text) }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            // Botão Negrito
            Box(
                modifier = Modifier
                    .background(if (paragraph.isBold) colors.accent else colors.bgMain, RoundedCornerShape(4.dp))
                    .clickable {
                        paragraph.isBold = !paragraph.isBold
                        onChanged()
                    }
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text("B", color = if (paragraph.isBold) Color.White else colors.textMain, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }

            // Botão Itálico
            Box(
                modifier = Modifier
                    .background(if (paragraph.isItalic) colors.accent else colors.bgMain, RoundedCornerShape(4.dp))
                    .clickable {
                        paragraph.isItalic = !paragraph.isItalic
                        onChanged()
                    }
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text("I", color = if (paragraph.isItalic) Color.White else colors.textMain, fontStyle = FontStyle.Italic, fontSize = 11.sp)
            }
        }

        BasicTextField(
            value = text,
            onValueChange = {
                text = it
                paragraph.text = it
                onChanged()
            },
            textStyle = TextStyle(
                color = colors.textMain,
                fontSize = 12.sp,
                fontWeight = if (paragraph.isBold) FontWeight.Bold else FontWeight.Normal,
                fontStyle = if (paragraph.isItalic) FontStyle.Italic else FontStyle.Normal
            ),
            cursorBrush = SolidColor(colors.accent),
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.bgMain, RoundedCornerShape(6.dp))
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(6.dp))
                .padding(8.dp)
        )
    }
}

@Composable
private fun TableVisualGridEditor(
    table: VisualElement.Table,
    onChanged: () -> Unit,
    colors: CanvasColors
) {
    val scrollState = rememberScrollState()

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // Barra de Ações da Tabela (+ Linha, + Coluna, - Linha, - Coluna)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SmallButton("+ Linha", colors) {
                val newRow = MutableList(table.headers.size) { "" }
                table.rows.add(newRow)
                onChanged()
            }
            SmallButton("+ Coluna", colors) {
                table.headers.add("Col ${table.headers.size + 1}")
                table.rows.forEach { it.add("") }
                onChanged()
            }
            if (table.rows.size > 1) {
                SmallButton("- Linha", colors) {
                    table.rows.removeAt(table.rows.size - 1)
                    onChanged()
                }
            }
            if (table.headers.size > 1) {
                SmallButton("- Coluna", colors) {
                    table.headers.removeAt(table.headers.size - 1)
                    table.rows.forEach { if (it.isNotEmpty()) it.removeAt(it.size - 1) }
                    onChanged()
                }
            }
        }

        // Grade Visual da Tabela com Scroll Horizontal
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(6.dp))
        ) {
            Column {
                // Linha de Cabeçalhos
                Row(modifier = Modifier.background(colors.accent.copy(alpha = 0.15f))) {
                    table.headers.forEachIndexed { colIndex, header ->
                        var hText by remember(header) { mutableStateOf(header) }
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .border(0.5.dp, colors.borderSubtle)
                                .padding(4.dp)
                        ) {
                            BasicTextField(
                                value = hText,
                                onValueChange = {
                                    hText = it
                                    table.headers[colIndex] = it
                                    onChanged()
                                },
                                textStyle = TextStyle(
                                    color = colors.accent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                cursorBrush = SolidColor(colors.accent),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Linhas de Células
                table.rows.forEachIndexed { rowIndex, row ->
                    Row {
                        table.headers.indices.forEach { colIndex ->
                            val cellVal = row.getOrNull(colIndex) ?: ""
                            var cText by remember(cellVal) { mutableStateOf(cellVal) }

                            Box(
                                modifier = Modifier
                                    .width(100.dp)
                                    .border(0.5.dp, colors.borderSubtle)
                                    .padding(4.dp)
                            ) {
                                BasicTextField(
                                    value = cText,
                                    onValueChange = {
                                        cText = it
                                        while (row.size <= colIndex) row.add("")
                                        row[colIndex] = it
                                        onChanged()
                                    },
                                    textStyle = TextStyle(
                                        color = colors.textMain,
                                        fontSize = 11.sp
                                    ),
                                    cursorBrush = SolidColor(colors.accent),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalloutVisualEditor(
    callout: VisualElement.Callout,
    onChanged: () -> Unit,
    colors: CanvasColors
) {
    var text by remember(callout.text) { mutableStateOf(callout.text) }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Seletor de Estilo
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("info" to "Azul", "warning" to "Amarelo", "success" to "Verde", "dark" to "Escuro").forEach { (type, label) ->
                val isSel = callout.styleType == type
                Box(
                    modifier = Modifier
                        .background(if (isSel) colors.accent else colors.bgMain, RoundedCornerShape(4.dp))
                        .clickable {
                            callout.styleType = type
                            onChanged()
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(label, color = if (isSel) Color.White else colors.textMuted, fontSize = 10.sp)
                }
            }
        }

        BasicTextField(
            value = text,
            onValueChange = {
                text = it
                callout.text = it
                onChanged()
            },
            textStyle = TextStyle(color = colors.textMain, fontSize = 11.5.sp),
            cursorBrush = SolidColor(colors.accent),
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.bgMain, RoundedCornerShape(6.dp))
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(6.dp))
                .padding(8.dp)
        )
    }
}

@Composable
private fun ListGroupVisualEditor(
    listGroup: VisualElement.ListGroup,
    onChanged: () -> Unit,
    colors: CanvasColors
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        listGroup.items.forEachIndexed { i, item ->
            var itemText by remember(item) { mutableStateOf(item) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("•", color = colors.accent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                BasicTextField(
                    value = itemText,
                    onValueChange = {
                        itemText = it
                        listGroup.items[i] = it
                        onChanged()
                    },
                    textStyle = TextStyle(color = colors.textMain, fontSize = 11.5.sp),
                    cursorBrush = SolidColor(colors.accent),
                    modifier = Modifier
                        .weight(1f)
                        .background(colors.bgMain, RoundedCornerShape(4.dp))
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(4.dp))
                        .padding(6.dp)
                )
                IconButton(
                    onClick = {
                        listGroup.items.removeAt(i)
                        onChanged()
                    },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(Icons.Rounded.Close, "Remover", tint = colors.danger, modifier = Modifier.size(12.dp))
                }
            }
        }

        SmallButton("+ Adicionar Item", colors) {
            listGroup.items.add("Novo item")
            onChanged()
        }
    }
}

@Composable
private fun SmallButton(label: String, colors: CanvasColors, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(colors.accent.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
            .border(1.dp, colors.accent.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(label, color = colors.accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}
