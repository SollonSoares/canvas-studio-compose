package com.canvasstudio.ui.block.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasstudio.data.local.entity.BlockEntity
import com.canvasstudio.ui.block.BlockViewModel
import com.canvasstudio.ui.block.modules.ChartBlock
import com.canvasstudio.ui.block.utils.parseRichText
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
                val elements = remember(metadata, block.contentJson) {
                    metadata?.get("elements")?.jsonArray
                        ?: metadata?.get("campos")?.jsonArray
                        ?: JsonArray(emptyList())
                }

                if (elements.isNotEmpty()) {
                    Box(Modifier.fillMaxSize()) {
                        Column(Modifier.fillMaxWidth()) {
                            elements.forEach { element ->
                                val el = element.jsonObject
                                val type = el["type"]?.jsonPrimitive?.content ?: "text"
                                val className = el["className"]?.jsonPrimitive?.content ?: ""

                                when (type) {
                                    "text", "" -> {
                                        val text = el["value"]?.jsonPrimitive?.content
                                            ?: el["html"]?.jsonPrimitive?.content
                                            ?: ""
                                        val isTitle = className.contains("classe-titulo")
                                        val isNum = className.contains("classe-num")

                                        Text(
                                            text = parseRichText(text),
                                            color = parsedTextColor ?: colors.textMain.copy(if (isTitle) 1f else 0.8f),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = if (isTitle) 4.dp else 2.dp)
                                                .then(if (isTitle) Modifier.drawBehind {
                                                    val strokeWidth = 2.dp.toPx()
                                                    val y = size.height - strokeWidth / 2
                                                    drawLine(colors.accent, Offset(0f, y), Offset(size.width, y), strokeWidth = strokeWidth)
                                                } else Modifier),
                                            fontSize = when {
                                                isTitle -> 17.sp
                                                isNum -> 20.sp
                                                else -> fontSize
                                            },
                                            fontWeight = if (isTitle || isNum || isBold) FontWeight.Bold else FontWeight.Normal,
                                            fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                                            fontFamily = if (isNum) androidx.compose.ui.text.font.FontFamily.Monospace else null,
                                            textAlign = if (isNum) TextAlign.Center else blockTextAlign
                                        )
                                    }
                                    "table" -> {
                                        val headers = el["headers"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
                                        val rows = el["rows"]?.jsonArray?.map { row ->
                                            row.jsonArray.map { it.jsonPrimitive.content }
                                        } ?: emptyList()
                                        CanvasTable(headers, rows, colors)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    val rawText = remember(metadata, block.contentJson) {
                        try {
                            metadata?.get("text")?.jsonPrimitive?.content ?: block.contentJson
                        } catch (e: Exception) {
                            block.contentJson
                        }
                    }
                    var inlineText by remember(rawText) { mutableStateOf(rawText) }

                    Box(Modifier.fillMaxSize()) {
                        BasicTextField(
                            value = inlineText,
                            onValueChange = { inlineText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                                .onFocusChanged {
                                    if (!it.isFocused && inlineText != rawText) {
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
                                        "Escreva aqui...",
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
