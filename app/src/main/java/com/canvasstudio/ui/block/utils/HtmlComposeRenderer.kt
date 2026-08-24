package com.canvasstudio.ui.block.utils

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasstudio.ui.theme.CanvasColors
import java.util.regex.Pattern

/**
 * Representa os nós parseados de um conteúdo HTML/Texto Rico.
 */
sealed class HtmlNode {
    data class Heading(val level: Int, val text: String) : HtmlNode()
    data class Paragraph(val text: String) : HtmlNode()
    data class Table(val headers: List<String>, val rows: List<List<String>>) : HtmlNode()
    data class CalloutBox(val htmlContent: String, val bgColor: Color?, val borderColor: Color?) : HtmlNode()
    data class PreBlock(val content: String) : HtmlNode()
    data class ListItem(val text: String, val isOrdered: Boolean, val index: Int) : HtmlNode()
}

object HtmlParser {

    /**
     * Converte uma string HTML em uma lista de nós estruturados para renderização Compose e PDF.
     */
    fun parse(rawHtml: String): List<HtmlNode> {
        if (rawHtml.isBlank()) return emptyList()
        val nodes = mutableListOf<HtmlNode>()

        // 1. Verificar se contém tabelas
        val tablePattern = Pattern.compile("(?is)<table.*?>(.*?)</table>")
        val tableMatcher = tablePattern.matcher(rawHtml)

        var lastEnd = 0
        while (tableMatcher.find()) {
            val beforeTable = rawHtml.substring(lastEnd, tableMatcher.start()).trim()
            if (beforeTable.isNotEmpty()) {
                nodes.addAll(parseBlocks(beforeTable))
            }

            val tableContent = tableMatcher.group(1) ?: ""
            nodes.add(parseTable(tableContent))
            lastEnd = tableMatcher.end()
        }

        val afterTables = rawHtml.substring(lastEnd).trim()
        if (afterTables.isNotEmpty()) {
            nodes.addAll(parseBlocks(afterTables))
        }

        return if (nodes.isEmpty()) listOf(HtmlNode.Paragraph(rawHtml)) else nodes
    }

    private fun parseBlocks(content: String): List<HtmlNode> {
        val blocks = mutableListOf<HtmlNode>()
        val normalized = content
            .replace(Regex("(?i)<ul>|</ul>|<ol>|</ol>"), "\n")
            .replace(Regex("(?i)</li>"), "\n")
        val lines = normalized.split(Regex("(?i)<br\\s*/?>|\\n"))

        lines.forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty()) return@forEach

            when {
                trimmed.startsWith("<h1", ignoreCase = true) -> {
                    blocks.add(HtmlNode.Heading(1, cleanHtmlTags(trimmed)))
                }
                trimmed.startsWith("<h2", ignoreCase = true) -> {
                    blocks.add(HtmlNode.Heading(2, cleanHtmlTags(trimmed)))
                }
                trimmed.startsWith("<h3", ignoreCase = true) -> {
                    blocks.add(HtmlNode.Heading(3, cleanHtmlTags(trimmed)))
                }
                trimmed.startsWith("<pre", ignoreCase = true) -> {
                    blocks.add(HtmlNode.PreBlock(cleanHtmlTags(trimmed)))
                }
                trimmed.startsWith("<li", ignoreCase = true) || trimmed.startsWith("•") || trimmed.startsWith("- ") -> {
                    blocks.add(HtmlNode.ListItem(cleanHtmlTags(trimmed).removePrefix("•").removePrefix("-").trim(), false, 0))
                }
                trimmed.contains("style=", ignoreCase = true) && trimmed.startsWith("<div", ignoreCase = true) -> {
                    val bg = extractColorFromStyle(trimmed, "background")
                    val border = extractColorFromStyle(trimmed, "border")
                    blocks.add(HtmlNode.CalloutBox(cleanHtmlTags(trimmed), bg, border))
                }
                else -> {
                    blocks.add(HtmlNode.Paragraph(trimmed))
                }
            }
        }

        return blocks
    }

    private fun parseTable(tableHtml: String): HtmlNode.Table {
        val headers = mutableListOf<String>()
        val rows = mutableListOf<List<String>>()

        // Extrair headers (th)
        val thPattern = Pattern.compile("(?is)<th.*?>(.*?)</th>")
        val thMatcher = thPattern.matcher(tableHtml)
        while (thMatcher.find()) {
            headers.add(cleanHtmlTags(thMatcher.group(1) ?: ""))
        }

        // Extrair linhas (tr)
        val trPattern = Pattern.compile("(?is)<tr.*?>(.*?)</tr>")
        val trMatcher = trPattern.matcher(tableHtml)
        while (trMatcher.find()) {
            val trContent = trMatcher.group(1) ?: ""
            val tdPattern = Pattern.compile("(?is)<td.*?>(.*?)</td>")
            val tdMatcher = tdPattern.matcher(trContent)
            val rowCells = mutableListOf<String>()
            while (tdMatcher.find()) {
                rowCells.add(cleanHtmlTags(tdMatcher.group(1) ?: ""))
            }
            if (rowCells.isNotEmpty()) {
                rows.add(rowCells)
            }
        }

        return HtmlNode.Table(headers, rows)
    }

    fun cleanHtmlTags(html: String): String {
        return html.replace(Regex("<[^>]*>"), "").trim()
    }

    private fun extractColorFromStyle(styleStr: String, property: String): Color? {
        val pattern = Pattern.compile("(?i)$property\\s*:\\s*(#[0-9a-fA-F]{3,8}|rgba?\\([^)]+\\)|[a-zA-Z]+)")
        val matcher = pattern.matcher(styleStr)
        if (matcher.find()) {
            val colorStr = matcher.group(1) ?: return null
            return try {
                if (colorStr.startsWith("#")) {
                    Color(AndroidColor.parseColor(colorStr))
                } else null
            } catch (e: Exception) {
                null
            }
        }
        return null
    }

    /**
     * Converte HTML inline em AnnotatedString do Compose com estilos visuais ricos.
     */
    fun toAnnotatedString(html: String, defaultColor: Color): AnnotatedString {
        val clean = html.replace("<br>", "\n").replace("<br/>", "\n").replace("<br />", "\n")
        
        return buildAnnotatedString {
            var i = 0
            var bold = false
            var italic = false
            var underline = false

            while (i < clean.length) {
                if (clean.startsWith("<b>", i) || clean.startsWith("<strong>", i)) {
                    bold = true
                    i += if (clean.startsWith("<b>", i)) 3 else 8
                } else if (clean.startsWith("</b>", i) || clean.startsWith("</strong>", i)) {
                    bold = false
                    i += if (clean.startsWith("</b>", i)) 4 else 9
                } else if (clean.startsWith("<i>", i) || clean.startsWith("<em>", i)) {
                    italic = true
                    i += if (clean.startsWith("<i>", i)) 3 else 4
                } else if (clean.startsWith("</i>", i) || clean.startsWith("</em>", i)) {
                    italic = false
                    i += if (clean.startsWith("</i>", i)) 4 else 5
                } else if (clean.startsWith("<u>", i)) {
                    underline = true
                    i += 3
                } else if (clean.startsWith("</u>", i)) {
                    underline = false
                    i += 4
                } else if (clean[i] == '<') {
                    val endTag = clean.indexOf('>', i)
                    if (endTag != -1) i = endTag + 1 else { append(clean[i]); i++ }
                } else {
                    withStyle(
                        SpanStyle(
                            color = defaultColor,
                            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
                            fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
                            textDecoration = if (underline) TextDecoration.Underline else TextDecoration.None
                        )
                    ) {
                        append(clean[i])
                    }
                    i++
                }
            }
        }
    }
}

/**
 * Renderizador Nativo Jetpack Compose de HTML / Tabelas / Textos Ricos.
 */
@Composable
fun HtmlComposeRenderer(
    htmlContent: String,
    modifier: Modifier = Modifier,
    colors: CanvasColors,
    fontSize: TextUnit = 13.sp,
    textAlign: TextAlign = TextAlign.Start,
    defaultTextColor: Color = colors.textMain
) {
    val nodes = remember(htmlContent) { HtmlParser.parse(htmlContent) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        nodes.forEach { node ->
            when (node) {
                is HtmlNode.Heading -> {
                    val headSize = when (node.level) {
                        1 -> 18.sp
                        2 -> 15.sp
                        else -> 13.5.sp
                    }
                    Text(
                        text = node.text,
                        color = colors.accent,
                        fontSize = headSize,
                        fontWeight = FontWeight.Bold,
                        textAlign = textAlign,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                is HtmlNode.Table -> {
                    ComposeHtmlTable(node, colors)
                }

                is HtmlNode.CalloutBox -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(node.bgColor ?: colors.accent.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                            .border(1.dp, node.borderColor ?: colors.accent.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = HtmlParser.toAnnotatedString(node.htmlContent, defaultTextColor),
                            fontSize = fontSize,
                            color = defaultTextColor
                        )
                    }
                }

                is HtmlNode.PreBlock -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = node.content,
                            color = Color(0xFF38BDF8),
                            fontFamily = FontFamily.Monospace,
                            fontSize = (fontSize.value - 1.5f).sp
                        )
                    }
                }

                is HtmlNode.ListItem -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("• ", color = colors.accent, fontSize = fontSize, fontWeight = FontWeight.Bold)
                        Text(
                            text = HtmlParser.toAnnotatedString(node.text, defaultTextColor),
                            fontSize = fontSize,
                            color = defaultTextColor
                        )
                    }
                }

                is HtmlNode.Paragraph -> {
                    Text(
                        text = HtmlParser.toAnnotatedString(node.text, defaultTextColor),
                        fontSize = fontSize,
                        textAlign = textAlign,
                        color = defaultTextColor,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun ComposeHtmlTable(table: HtmlNode.Table, colors: CanvasColors) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(6.dp))
    ) {
        Column(modifier = Modifier.wrapContentWidth()) {
            // Header
            if (table.headers.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .background(colors.accent.copy(alpha = 0.12f))
                        .padding(vertical = 4.dp)
                ) {
                    table.headers.forEach { header ->
                        Box(
                            modifier = Modifier
                                .widthIn(min = 70.dp, max = 180.dp)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = header,
                                color = colors.accent,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Rows
            table.rows.forEachIndexed { index, row ->
                val bg = if (index % 2 == 0) Color.Transparent else colors.bgCard.copy(alpha = 0.5f)
                Row(
                    modifier = Modifier
                        .background(bg)
                        .padding(vertical = 4.dp)
                ) {
                    row.forEach { cell ->
                        Box(
                            modifier = Modifier
                                .widthIn(min = 70.dp, max = 180.dp)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = cell,
                                color = colors.textMain,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Utilitários para gerar templates HTML padronizados compatíveis com a versão Web.
 */
object HtmlTemplates {

    fun generateTableHtml(rows: Int = 2, cols: Int = 3): String {
        return buildString {
            append("""<table style="width: 100%; border-collapse: collapse; margin: 6px 0; font-size: 11px;">""")
            append("<thead><tr>")
            for (c in 1..cols) {
                append("""<th style="border: 1px solid rgba(128,128,128,0.3); padding: 4px 6px; background-color: rgba(2,132,199,0.15); color: #0284c7; font-weight: bold;">Col $c</th>""")
            }
            append("</tr></thead><tbody>")
            for (r in 1..rows) {
                append("<tr>")
                for (c in 1..cols) {
                    append("""<td style="border: 1px solid rgba(128,128,128,0.2); padding: 4px 6px;">Valor $r.$c</td>""")
                }
                append("</tr>")
            }
            append("</tbody></table><div><br></div>")
        }
    }

    fun generateHeadingHtml(text: String = "Novo Título", level: Int = 2): String {
        return "<h$level style=\"color: #0284c7; margin: 4px 0;\">$text</h$level><div><br></div>"
    }

    fun generateCalloutHtml(text: String = "Texto destacado..."): String {
        return """<div style="background: rgba(2,132,199,0.1); border: 1px solid rgba(2,132,199,0.4); border-radius: 6px; padding: 6px 8px; margin: 4px 0; font-size: 12px;">$text</div><div><br></div>"""
    }

    fun generatePreHtml(text: String = "Código ou descrição técnica..."): String {
        return """<pre style="background: #1e293b; color: #38bdf8; padding: 6px 8px; border-radius: 6px; font-family: monospace; font-size: 11px; margin: 4px 0;">$text</pre><div><br></div>"""
    }
}
