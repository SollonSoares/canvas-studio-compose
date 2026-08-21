package com.canvasstudio.features.export_portability

import com.canvasstudio.data.local.entity.BlockEntity
import kotlinx.serialization.json.*

object JsonPortabilityService {

    fun exportToJson(brandTitle: String, blocks: List<BlockEntity>): String {
        val json = Json { prettyPrint = true }
        val root = buildJsonObject {
            putJsonObject("metadata") {
                put("versao", "2.0.0")
                put("timestamp", System.currentTimeMillis())
                put("brand", brandTitle)
            }
            
            putJsonObject("blocos") {
                blocks.forEach { block ->
                    val blockKey = "data_t_${block.id}_${System.currentTimeMillis() % 100000}"
                    put(blockKey, buildJsonObject {
                        put("top", "${block.posY.toInt()}px")
                        put("left", "${block.posX.toInt()}px")
                        put("width", block.width)
                        put("height", block.height)
                        put("type", block.type)
                        put("title", block.title)
                        
                        val content = try {
                            Json.parseToJsonElement(block.contentJson).jsonObject
                        } catch (e: Exception) {
                            null
                        }

                        val valor = content?.get("valor")?.jsonPrimitive?.floatOrNull
                        val valorFormatted = content?.get("valorFormatted")?.jsonPrimitive?.contentOrNull
                        val realizadoEm = content?.get("realizadoEm")?.jsonPrimitive?.contentOrNull
                        val isPix = content?.get("isPix")?.jsonPrimitive?.booleanOrNull == true || block.title.contains("PIX", true)
                        val pagador = content?.get("pagador")?.jsonPrimitive?.contentOrNull ?: content?.get("de")?.jsonPrimitive?.contentOrNull
                        val destinatario = content?.get("destinatario")?.jsonPrimitive?.contentOrNull ?: content?.get("para")?.jsonPrimitive?.contentOrNull
                        val instituicao = content?.get("instituicao")?.jsonPrimitive?.contentOrNull ?: content?.get("banco")?.jsonPrimitive?.contentOrNull
                        val rawText = content?.get("rawText")?.jsonPrimitive?.contentOrNull ?: ""

                        when (block.type.lowercase()) {
                            "image" -> {
                                val url = content?.get("url")?.jsonPrimitive?.content ?: ""
                                put("url", url)
                                content?.get("imgId")?.jsonPrimitive?.content?.let { put("imgId", it) }

                                putJsonArray("campos") {
                                    addJsonObject {
                                        val sb = StringBuilder()
                                        if (valorFormatted != null) sb.append("<div><b>Valor:</b> $valorFormatted</div>")
                                        if (!realizadoEm.isNullOrBlank()) sb.append("<div><b>Realizado em:</b> $realizadoEm</div>")
                                        if (!pagador.isNullOrBlank()) sb.append("<div><b>De (Pagador):</b> $pagador</div>")
                                        if (!destinatario.isNullOrBlank()) sb.append("<div><b>Para (Destinatário):</b> $destinatario</div>")
                                        if (!instituicao.isNullOrBlank()) sb.append("<div><b>Instituição:</b> $instituicao</div>")
                                        if (rawText.isNotBlank()) sb.append("<div style=\"margin-top:4px;\"><b>Texto OCR:</b><br><pre>${rawText.replace("<", "&lt;").replace(">", "&gt;")}</pre></div>")
                                        
                                        put("html", if (sb.isNotEmpty()) sb.toString() else "<div><i>Comprovante</i></div>")
                                        put("className", "sub-campo")
                                        if (valor != null) put("valor", valor)
                                        if (valorFormatted != null) put("valorFormatted", valorFormatted)
                                        if (!realizadoEm.isNullOrBlank()) put("realizadoEm", realizadoEm)
                                        if (isPix) put("isPix", true)
                                        if (!pagador.isNullOrBlank()) {
                                            put("de", pagador)
                                            put("pagador", pagador)
                                        }
                                        if (!destinatario.isNullOrBlank()) {
                                            put("para", destinatario)
                                            put("destinatario", destinatario)
                                        }
                                        if (!instituicao.isNullOrBlank()) {
                                            put("instituicao", instituicao)
                                            put("banco", instituicao)
                                        }
                                        if (rawText.isNotBlank()) put("rawText", rawText)
                                    }
                                }
                            }
                            "text" -> {
                                val text = content?.get("text")?.jsonPrimitive?.content ?: ""
                                val html = text
                                    .replace(Regex("\\*\\*(.*?)\\*\\*"), "<b>$1</b>")
                                    .replace(Regex("\\*(.*?)\\*"), "<i>$1</i>")
                                    .replace("\n", "<br>")
                                
                                putJsonArray("campos") {
                                    addJsonObject {
                                        put("html", html)
                                        put("className", "sub-campo")
                                        if (valor != null) put("valor", valor)
                                        if (valorFormatted != null) put("valorFormatted", valorFormatted)
                                        if (!realizadoEm.isNullOrBlank()) put("realizadoEm", realizadoEm)
                                        if (isPix) put("isPix", true)
                                        if (!pagador.isNullOrBlank()) put("de", pagador)
                                        if (!destinatario.isNullOrBlank()) put("para", destinatario)
                                        if (!instituicao.isNullOrBlank()) put("instituicao", instituicao)
                                        if (rawText.isNotBlank()) put("rawText", rawText)
                                    }
                                }
                            }
                            "chart" -> {
                                put("status", content ?: buildJsonObject {})
                            }
                        }
                    })
                }
            }
        }
        return json.encodeToString(JsonObject.serializer(), root)
    }

    fun parseBlocksFromJson(jsonString: String, projectId: Long): Pair<String?, List<BlockEntity>> {
        val json = Json { ignoreUnknownKeys = true }
        val jsonElement = json.parseToJsonElement(jsonString)
        val newBlocks = mutableListOf<BlockEntity>()

        val rootObj = jsonElement.jsonObject
        val brand = rootObj["metadata"]?.jsonObject?.get("brand")?.jsonPrimitive?.content
            ?: rootObj["app_brand_title"]?.jsonPrimitive?.content

        fun extractBlocks(element: JsonElement) {
            when (element) {
                is JsonArray -> {
                    element.forEach { 
                        if (it is JsonObject && it.containsKey("type")) {
                            newBlocks.add(parseBlockObject(it, projectId))
                        } else {
                            extractBlocks(it)
                        }
                    }
                }
                is JsonObject -> {
                    if (element.containsKey("type") && (element.containsKey("left") || element.containsKey("top") || element.containsKey("posX"))) {
                        newBlocks.add(parseBlockObject(element, projectId))
                    } else {
                        element.values.forEach { extractBlocks(it) }
                    }
                }
                else -> {}
            }
        }

        extractBlocks(jsonElement)
        return Pair(brand, newBlocks)
    }

    private fun parseBlockObject(obj: JsonObject, projectId: Long): BlockEntity {
        val type = obj["type"]?.jsonPrimitive?.content ?: "text"
        val title = obj["title"]?.jsonPrimitive?.content ?: "Bloco"
        
        val contentJson = when (type.lowercase()) {
            "chart" -> {
                val status = obj["status"]?.jsonObject ?: obj["inputs"]?.jsonObject ?: obj
                buildJsonObject {
                    put("ninjutsu", status["ninjutsu"]?.jsonPrimitive?.floatOrNull ?: status["nin"]?.jsonPrimitive?.floatOrNull ?: 4f)
                    put("inteligencia", status["inteligencia"]?.jsonPrimitive?.floatOrNull ?: status["int"]?.jsonPrimitive?.floatOrNull ?: 4f)
                    put("chakra", status["chakra"]?.jsonPrimitive?.floatOrNull ?: status["cha"]?.jsonPrimitive?.floatOrNull ?: status["chakraMax"]?.jsonPrimitive?.floatOrNull ?: 4f)
                    put("taijutsu", status["taijutsu"]?.jsonPrimitive?.floatOrNull ?: status["tai"]?.jsonPrimitive?.floatOrNull ?: 4f)
                    put("vigor", status["vigor"]?.jsonPrimitive?.floatOrNull ?: status["vig"]?.jsonPrimitive?.floatOrNull ?: 4f)
                    put("genjutsu", status["genjutsu"]?.jsonPrimitive?.floatOrNull ?: status["gen"]?.jsonPrimitive?.floatOrNull ?: 4f)
                }.toString()
            }
            "text" -> {
                val campos = obj["campos"]?.jsonArray
                val elements = mutableListOf<JsonElement>()
                
                if (campos != null && campos.isNotEmpty()) {
                    campos.forEach { campo ->
                        val html = campo.jsonObject["html"]?.jsonPrimitive?.content ?: ""
                        elements.addAll(parseHtmlToElements(html))
                    }
                } else {
                    val html = obj["text"]?.jsonPrimitive?.content ?: obj["html"]?.jsonPrimitive?.content ?: ""
                    elements.addAll(parseHtmlToElements(html))
                }
                
                val flatText = elements.joinToString("\n") { 
                    val el = it.jsonObject
                    if (el["type"]?.jsonPrimitive?.content == "text") el["value"]?.jsonPrimitive?.content ?: "" else ""
                }

                buildJsonObject {
                    put("elements", JsonArray(elements))
                    put("text", flatText)
                    put("titleSize", 13)
                    put("align", "left")
                }.toString()
            }
            "image" -> {
                val url = obj["url"]?.jsonPrimitive?.content ?: obj["src"]?.jsonPrimitive?.content ?: ""
                val imgId = obj["imgId"]?.jsonPrimitive?.content ?: obj["id"]?.jsonPrimitive?.content ?: ""
                
                val firstCampo = obj["campos"]?.jsonArray?.firstOrNull()?.jsonObject
                val comp = firstCampo ?: obj["comprovante"]?.jsonObject ?: obj
                val valor = comp["valor"]?.jsonPrimitive?.floatOrNull ?: obj["valor"]?.jsonPrimitive?.floatOrNull
                val valorFormatted = comp["valorFormatted"]?.jsonPrimitive?.contentOrNull ?: obj["valorFormatted"]?.jsonPrimitive?.contentOrNull
                val realizadoEm = comp["realizadoEm"]?.jsonPrimitive?.contentOrNull ?: obj["realizadoEm"]?.jsonPrimitive?.contentOrNull
                val isPix = comp["isPix"]?.jsonPrimitive?.booleanOrNull ?: obj["isPix"]?.jsonPrimitive?.booleanOrNull ?: false
                val pagador = comp["pagador"]?.jsonPrimitive?.contentOrNull ?: comp["de"]?.jsonPrimitive?.contentOrNull ?: obj["de"]?.jsonPrimitive?.contentOrNull
                val destinatario = comp["destinatario"]?.jsonPrimitive?.contentOrNull ?: comp["para"]?.jsonPrimitive?.contentOrNull ?: obj["para"]?.jsonPrimitive?.contentOrNull
                val instituicao = comp["instituicao"]?.jsonPrimitive?.contentOrNull ?: comp["banco"]?.jsonPrimitive?.contentOrNull ?: obj["instituicao"]?.jsonPrimitive?.contentOrNull
                val rawText = comp["rawText"]?.jsonPrimitive?.contentOrNull ?: obj["rawText"]?.jsonPrimitive?.contentOrNull ?: ""

                buildJsonObject { 
                    put("url", url) 
                    if (imgId.isNotEmpty()) put("imgId", imgId)
                    if (valor != null) put("valor", valor)
                    if (valorFormatted != null) put("valorFormatted", valorFormatted)
                    if (!realizadoEm.isNullOrBlank()) put("realizadoEm", realizadoEm)
                    if (isPix) put("isPix", true)
                    if (!pagador.isNullOrBlank()) put("pagador", pagador)
                    if (!destinatario.isNullOrBlank()) put("destinatario", destinatario)
                    if (!instituicao.isNullOrBlank()) put("instituicao", instituicao)
                    if (rawText.isNotBlank()) put("rawText", rawText)
                }.toString()
            }
            else -> obj.toString()
        }

        val posX = obj["left"]?.jsonPrimitive?.content?.replace("px", "")?.toFloatOrNull() 
            ?: obj["posX"]?.jsonPrimitive?.floatOrNull ?: 100f
        val posY = obj["top"]?.jsonPrimitive?.content?.replace("px", "")?.toFloatOrNull() 
            ?: obj["posY"]?.jsonPrimitive?.floatOrNull ?: 100f

        return BlockEntity(
            projectId = projectId,
            title = title,
            type = type,
            posX = posX,
            posY = posY,
            width = obj["width"]?.jsonPrimitive?.content?.replace("px", "")?.toIntOrNull() 
                ?: obj["width"]?.jsonPrimitive?.intOrNull ?: 220,
            height = obj["height"]?.jsonPrimitive?.content?.replace("px", "")?.toIntOrNull() 
                ?: obj["height"]?.jsonPrimitive?.intOrNull ?: 180,
            contentJson = contentJson
        )
    }

    private fun parseHtmlToElements(html: String): List<JsonElement> {
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

    private fun cleanHtmlContent(html: String): String {
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
