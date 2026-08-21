package com.canvasstudio.features.export_portability

import kotlinx.serialization.json.*

object HtmlContentConverter {

    fun parseHtmlToElements(html: String): List<JsonElement> {
        val elements = mutableListOf<JsonElement>()
        val tableRegex = Regex("<table[^>]*>(.*?)</table>", RegexOption.DOT_MATCHES_ALL)
        var lastIndex = 0

        for (match in tableRegex.findAll(html)) {
            val textBefore = html.substring(lastIndex, match.range.first)
            if (textBefore.replace(Regex("<[^>]*>"), "").replace("&nbsp;", " ").isNotBlank()) {
                elements.add(buildJsonObject {
                    put("type", "text")
                    put("value", cleanHtmlContent(textBefore))
                })
            }

            val tableHtml = match.groupValues[1]
            val headers = mutableListOf<String>()
            val rows = mutableListOf<JsonArray>()

            Regex("<th[^>]*>(.*?)</th>", RegexOption.DOT_MATCHES_ALL).findAll(tableHtml).forEach {
                headers.add(cleanHtmlContent(it.groupValues[1]))
            }

            Regex("<tr[^>]*>(.*?)</tr>", RegexOption.DOT_MATCHES_ALL).findAll(tableHtml).forEach {
                val rowHtml = it.groupValues[1]
                val cells = mutableListOf<JsonPrimitive>()
                val tdMatches = Regex("<td[^>]*>(.*?)</td>", RegexOption.DOT_MATCHES_ALL).findAll(rowHtml).toList()
                if (tdMatches.isNotEmpty()) {
                    tdMatches.forEach { td ->
                        cells.add(JsonPrimitive(cleanHtmlContent(td.groupValues[1])))
                    }
                    rows.add(JsonArray(cells))
                }
            }

            elements.add(buildJsonObject {
                put("type", "table")
                putJsonArray("headers") { headers.forEach { add(it) } }
                putJsonArray("rows") { rows.forEach { add(it) } }
            })

            lastIndex = match.range.last + 1
        }

        val textAfter = html.substring(lastIndex)
        if (textAfter.replace(Regex("<[^>]*>"), "").replace("&nbsp;", " ").isNotBlank()) {
            elements.add(buildJsonObject {
                put("type", "text")
                put("value", cleanHtmlContent(textAfter))
            })
        }

        return elements
    }

    fun cleanHtmlContent(html: String): String {
        return html
            .replace(Regex("<span[^>]*font-size:\\s*(\\d+)px[^>]*>(.*?)</span>")) {
                "[size=${it.groupValues[1]}]${it.groupValues[2]}[/size]"
            }
            .replace(Regex("<b[^>]*>"), "**").replace("</b>", "**")
            .replace(Regex("<strong[^>]*>"), "**").replace("</strong>", "**")
            .replace(Regex("<i[^>]*>"), "*").replace("</i>", "*")
            .replace(Regex("<em[^>]*>"), "*").replace("</em>", "*")
            .replace(Regex("<u[^>]*>"), "<u>").replace("</u>", "</u>")
            .replace("<br>", "\n")
            .replace(Regex("<pre[^>]*>"), "\n> ").replace("</pre>", "\n")
            .replace(Regex("<div[^>]*>"), "").replace("</div>", "\n")
            .replace(Regex("<p[^>]*>"), "").replace("</p>", "\n")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace(Regex("<[^>]*>"), "")
            .trim()
    }
}
