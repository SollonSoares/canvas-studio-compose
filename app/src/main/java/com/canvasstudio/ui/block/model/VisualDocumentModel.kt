package com.canvasstudio.ui.block.model

import java.util.regex.Pattern

/**
 * Modelo de documento visual puro para edição WYSIWYG de blocos.
 * O usuário edita apenas objetos e textos visuais; o HTML é gerado automaticamente por trás.
 */
sealed class VisualElement {
    abstract val id: String

    data class Heading(
        override val id: String = java.util.UUID.randomUUID().toString(),
        var text: String = "",
        var level: Int = 2 // 1 = H1, 2 = H2, 3 = H3
    ) : VisualElement()

    data class Paragraph(
        override val id: String = java.util.UUID.randomUUID().toString(),
        var text: String = "",
        var isBold: Boolean = false,
        var isItalic: Boolean = false
    ) : VisualElement()

    data class Table(
        override val id: String = java.util.UUID.randomUUID().toString(),
        var headers: MutableList<String> = mutableListOf("Coluna 1", "Coluna 2", "Coluna 3"),
        var rows: MutableList<MutableList<String>> = mutableListOf(
            mutableListOf("Item 1.1", "Item 1.2", "Item 1.3"),
            mutableListOf("Item 2.1", "Item 2.2", "Item 2.3")
        )
    ) : VisualElement()

    data class Callout(
        override val id: String = java.util.UUID.randomUUID().toString(),
        var text: String = "",
        var styleType: String = "info" // "info", "warning", "success", "dark"
    ) : VisualElement()

    data class Collapsible(
        override val id: String = java.util.UUID.randomUUID().toString(),
        var summary: String = "Seção Expansível",
        var content: String = "Linha 1 do texto detalhado.\nLinha 2 do texto detalhado.\nLinha 3 do texto detalhado.\nLinha 4 do texto expandido...",
        var isExpanded: Boolean = true
    ) : VisualElement()

    data class ListGroup(
        override val id: String = java.util.UUID.randomUUID().toString(),
        var items: MutableList<String> = mutableListOf("Item 1", "Item 2")
    ) : VisualElement()
}

/**
 * Bridge bidirecional entre HTML (Web Studio / JSON) e VisualElement (Compose UI).
 */
object HtmlDocumentBridge {

    /**
     * Converte HTML bruto em elementos visuais limpos para a UI.
     * O usuário nunca vê tags HTML.
     */
    fun fromHtml(rawHtml: String): List<VisualElement> {
        if (rawHtml.isBlank()) {
            return listOf(VisualElement.Paragraph(text = ""))
        }

        val unescaped = rawHtml
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&#39;", "'")
            .replace("&nbsp;", " ")

        val elements = mutableListOf<VisualElement>()

        // 1. Processar Tabelas e Details / Collapsibles
        val blockPattern = Pattern.compile("(?is)(<table.*?>(.*?)</table>|<details.*?>(.*?)</details>)")
        val matcher = blockPattern.matcher(unescaped)
        var lastEnd = 0

        while (matcher.find()) {
            val before = unescaped.substring(lastEnd, matcher.start()).trim()
            if (before.isNotEmpty()) {
                elements.addAll(parseNonTableContent(before))
            }

            val fullMatch = matcher.group(1) ?: ""
            if (fullMatch.startsWith("<table", ignoreCase = true)) {
                val tableInner = matcher.group(2) ?: ""
                elements.add(parseTableElement(tableInner))
            } else if (fullMatch.startsWith("<details", ignoreCase = true)) {
                val detailsInner = matcher.group(3) ?: ""
                val summaryMatcher = Pattern.compile("(?is)<summary.*?>(.*?)</summary>").matcher(detailsInner)
                val summary = if (summaryMatcher.find()) cleanText(summaryMatcher.group(1) ?: "") else "Seção"
                val contentHtml = detailsInner.replace(Regex("(?is)<summary.*?>.*?</summary>"), "")
                val content = cleanText(contentHtml)
                elements.add(VisualElement.Collapsible(summary = summary, content = content, isExpanded = true))
            }

            lastEnd = matcher.end()
        }

        val after = unescaped.substring(lastEnd).trim()
        if (after.isNotEmpty()) {
            elements.addAll(parseNonTableContent(after))
        }

        return if (elements.isEmpty()) listOf(VisualElement.Paragraph(text = cleanText(unescaped))) else elements
    }

    private fun parseNonTableContent(content: String): List<VisualElement> {
        val list = mutableListOf<VisualElement>()
        val normalized = content
            .replace(Regex("(?i)<ul>|</ul>|<ol>|</ol>"), "\n")
            .replace(Regex("(?i)</li>"), "\n")
        val lines = normalized.split(Regex("(?i)<br\\s*/?>|\\n"))

        lines.forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty()) return@forEach

            when {
                trimmed.startsWith("<h1", ignoreCase = true) -> {
                    list.add(VisualElement.Heading(text = cleanText(trimmed), level = 1))
                }
                trimmed.startsWith("<h2", ignoreCase = true) -> {
                    list.add(VisualElement.Heading(text = cleanText(trimmed), level = 2))
                }
                trimmed.startsWith("<h3", ignoreCase = true) -> {
                    list.add(VisualElement.Heading(text = cleanText(trimmed), level = 3))
                }
                trimmed.contains("style=", ignoreCase = true) && trimmed.startsWith("<div", ignoreCase = true) -> {
                    list.add(VisualElement.Callout(text = cleanText(trimmed)))
                }
                trimmed.startsWith("<pre", ignoreCase = true) -> {
                    list.add(VisualElement.Callout(text = cleanText(trimmed), styleType = "dark"))
                }
                trimmed.startsWith("<li", ignoreCase = true) || trimmed.startsWith("•") || trimmed.startsWith("- ") -> {
                    val itemText = cleanText(trimmed).removePrefix("•").removePrefix("-").trim()
                    val last = list.lastOrNull()
                    if (last is VisualElement.ListGroup) {
                        last.items.add(itemText)
                    } else {
                        list.add(VisualElement.ListGroup(items = mutableListOf(itemText)))
                    }
                }
                else -> {
                    list.add(VisualElement.Paragraph(text = cleanText(trimmed)))
                }
            }
        }

        return list
    }

    private fun parseTableElement(tableHtml: String): VisualElement.Table {
        val headers = mutableListOf<String>()
        val rows = mutableListOf<MutableList<String>>()

        val thPattern = Pattern.compile("(?is)<th.*?>(.*?)</th>")
        val thMatcher = thPattern.matcher(tableHtml)
        while (thMatcher.find()) {
            headers.add(cleanText(thMatcher.group(1) ?: ""))
        }

        val trPattern = Pattern.compile("(?is)<tr.*?>(.*?)</tr>")
        val trMatcher = trPattern.matcher(tableHtml)
        while (trMatcher.find()) {
            val trContent = trMatcher.group(1) ?: ""
            val tdPattern = Pattern.compile("(?is)<td.*?>(.*?)</td>")
            val tdMatcher = tdPattern.matcher(trContent)
            val rowCells = mutableListOf<String>()
            while (tdMatcher.find()) {
                rowCells.add(cleanText(tdMatcher.group(1) ?: ""))
            }
            if (rowCells.isNotEmpty()) {
                rows.add(rowCells)
            }
        }

        if (headers.isEmpty() && rows.isNotEmpty()) {
            val first = rows.removeAt(0)
            headers.addAll(first)
        }

        if (headers.isEmpty()) {
            headers.addAll(listOf("Coluna 1", "Coluna 2"))
        }
        if (rows.isEmpty()) {
            rows.add(MutableList(headers.size) { "" })
        }

        return VisualElement.Table(
            headers = headers,
            rows = rows
        )
    }

    /**
     * Converte a lista de elementos visuais editados no Compose de volta para HTML limpo e padronizado.
     * Nunca quebra a formatação.
     */
    fun toHtml(elements: List<VisualElement>): String {
        return buildString {
            elements.forEach { el ->
                when (el) {
                    is VisualElement.Heading -> {
                        append("""<h${el.level} style="color: #0284c7; margin: 6px 0; font-weight: bold;">${escapeHtml(el.text)}</h${el.level}><div><br></div>""")
                    }
                    is VisualElement.Paragraph -> {
                        if (el.text.isNotBlank()) {
                            val styledText = when {
                                el.isBold && el.isItalic -> "<b><i>${escapeHtml(el.text)}</i></b>"
                                el.isBold -> "<b>${escapeHtml(el.text)}</b>"
                                el.isItalic -> "<i>${escapeHtml(el.text)}</i>"
                                else -> escapeHtml(el.text)
                            }
                            append("<div>$styledText</div>")
                        }
                    }
                    is VisualElement.Table -> {
                        append("""<table style="color: rgb(240, 242, 248); width: 100%; border-collapse: collapse; font-family: sans-serif; font-size: 11px; margin: 6px 0;">""")
                        append("""<thead><tr style="background: rgba(45, 45, 45, 0.9); color: rgb(79, 195, 247);">""")
                        el.headers.forEach { header ->
                            append("""<th style="border: 1px solid rgba(255, 255, 255, 0.15); padding: 4px 6px; text-align: left;">${escapeHtml(header)}</th>""")
                        }
                        append("</tr></thead><tbody>")
                        el.rows.forEach { row ->
                            append("<tr>")
                            el.headers.indices.forEach { colIdx ->
                                val cellVal = row.getOrNull(colIdx) ?: ""
                                append("""<td style="border: 1px solid rgba(255, 255, 255, 0.15); padding: 4px 6px; background-color: rgba(30, 30, 30, 0.6);">${escapeHtml(cellVal)}</td>""")
                            }
                            append("</tr>")
                        }
                        append("</tbody></table><div><br></div>")
                    }
                    is VisualElement.Callout -> {
                        val bg = when (el.styleType) {
                            "warning" -> "rgba(234,179,8,0.15)"
                            "success" -> "rgba(34,197,94,0.15)"
                            "dark" -> "#1e293b"
                            else -> "rgba(2,132,199,0.12)"
                        }
                        val border = when (el.styleType) {
                            "warning" -> "rgba(234,179,8,0.4)"
                            "success" -> "rgba(34,197,94,0.4)"
                            "dark" -> "#334155"
                            else -> "rgba(2,132,199,0.35)"
                        }
                        val textColor = if (el.styleType == "dark") "#38bdf8" else "#f0f2f8"

                        append("""<div style="background: $bg; border: 1px solid $border; border-radius: 6px; padding: 6px 10px; margin: 6px 0; color: $textColor; font-size: 12px;">${escapeHtml(el.text)}</div><div><br></div>""")
                    }
                    is VisualElement.Collapsible -> {
                        val summaryHtml = escapeHtml(el.summary.ifBlank { "Seção" })
                        val contentHtml = escapeHtml(el.content).replace("\n", "<br>")
                        append("""<details style="background: rgba(2,132,199,0.08); border: 1px solid rgba(2,132,199,0.3); border-radius: 6px; padding: 6px 10px; margin: 6px 0;"><summary style="cursor: pointer; font-weight: bold; color: #0284c7;">$summaryHtml</summary><div style="margin-top: 4px; font-size: 12px;">$contentHtml</div></details><div><br></div>""")
                    }
                    is VisualElement.ListGroup -> {
                        append("""<ul style="margin: 6px 0; padding-left: 18px;">""")
                        el.items.forEach { item ->
                            if (item.isNotBlank()) {
                                append("""<li>${escapeHtml(item)}</li>""")
                            }
                        }
                        append("</ul><div><br></div>")
                    }
                }
            }
        }
    }

    private fun cleanText(html: String): String {
        return html
            .replace(Regex("(?i)<br\\s*/?>"), "\n")
            .replace(Regex("(?i)</div>"), "\n")
            .replace(Regex("(?i)</p>"), "\n")
            .replace(Regex("(?i)</li>"), "\n")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace(Regex("<[^>]*>"), "")
            .trim()
    }

    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
    }
}
