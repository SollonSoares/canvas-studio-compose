package com.canvasstudio.features.export_portability

import com.canvasstudio.data.local.entity.BlockEntity
import kotlinx.serialization.json.*

object JsonBlockParser {

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
            "chart" -> parseChartContent(obj)
            "text" -> parseTextContent(obj)
            "image" -> parseImageContent(obj)
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

    private fun parseChartContent(obj: JsonObject): String {
        val status = obj["status"]?.jsonObject ?: obj["inputs"]?.jsonObject ?: obj
        return buildJsonObject {
            put("ninjutsu", status["ninjutsu"]?.jsonPrimitive?.floatOrNull ?: status["nin"]?.jsonPrimitive?.floatOrNull ?: 4f)
            put("inteligencia", status["inteligencia"]?.jsonPrimitive?.floatOrNull ?: status["int"]?.jsonPrimitive?.floatOrNull ?: 4f)
            put("chakra", status["chakra"]?.jsonPrimitive?.floatOrNull ?: status["cha"]?.jsonPrimitive?.floatOrNull ?: status["chakraMax"]?.jsonPrimitive?.floatOrNull ?: 4f)
            put("taijutsu", status["taijutsu"]?.jsonPrimitive?.floatOrNull ?: status["tai"]?.jsonPrimitive?.floatOrNull ?: 4f)
            put("vigor", status["vigor"]?.jsonPrimitive?.floatOrNull ?: status["vig"]?.jsonPrimitive?.floatOrNull ?: 4f)
            put("genjutsu", status["genjutsu"]?.jsonPrimitive?.floatOrNull ?: status["gen"]?.jsonPrimitive?.floatOrNull ?: 4f)
        }.toString()
    }

    private fun parseTextContent(obj: JsonObject): String {
        val campos = obj["campos"]?.jsonArray
        val elements = mutableListOf<JsonElement>()

        if (campos != null && campos.isNotEmpty()) {
            campos.forEach { campo ->
                val html = campo.jsonObject["html"]?.jsonPrimitive?.content ?: ""
                elements.addAll(HtmlContentConverter.parseHtmlToElements(html))
            }
        } else {
            val html = obj["text"]?.jsonPrimitive?.content ?: obj["html"]?.jsonPrimitive?.content ?: ""
            elements.addAll(HtmlContentConverter.parseHtmlToElements(html))
        }

        val flatText = elements.joinToString("\n") {
            val el = it.jsonObject
            if (el["type"]?.jsonPrimitive?.content == "text") el["value"]?.jsonPrimitive?.content ?: "" else ""
        }

        return buildJsonObject {
            put("elements", JsonArray(elements))
            put("text", flatText)
            put("titleSize", 13)
            put("align", "left")
        }.toString()
    }

    private fun parseImageContent(obj: JsonObject): String {
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

        return buildJsonObject {
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
}
