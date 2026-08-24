package com.canvasstudio.features.export_portability

import com.canvasstudio.data.local.entity.BlockEntity
import com.canvasstudio.ui.block.modules.ShinobiChartCalculator
import kotlinx.serialization.json.*

object JsonBlockParser {

    fun parseBlocksFromJson(jsonString: String, projectId: Long): Pair<String?, List<BlockEntity>> {
        val json = Json { ignoreUnknownKeys = true }
        val jsonElement = json.parseToJsonElement(jsonString)
        val newBlocks = mutableListOf<BlockEntity>()

        val rootObj = jsonElement.jsonObject
        val brand = rootObj["metadata"]?.jsonObject?.get("brand")?.jsonPrimitive?.contentOrNull
            ?: rootObj["app_brand_title"]?.jsonPrimitive?.contentOrNull

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

        val posX = obj["left"]?.jsonPrimitive?.contentOrNull?.replace("px", "")?.toFloatOrNull()
            ?: obj["posX"]?.jsonPrimitive?.floatOrNull ?: 100f
        val posY = obj["top"]?.jsonPrimitive?.contentOrNull?.replace("px", "")?.toFloatOrNull()
            ?: obj["posY"]?.jsonPrimitive?.floatOrNull ?: 100f

        return BlockEntity(
            projectId = projectId,
            title = title,
            type = type,
            posX = posX,
            posY = posY,
            width = obj["width"]?.jsonPrimitive?.contentOrNull?.replace("px", "")?.toIntOrNull()
                ?: obj["width"]?.jsonPrimitive?.intOrNull ?: 220,
            height = obj["height"]?.jsonPrimitive?.contentOrNull?.replace("px", "")?.toIntOrNull()
                ?: obj["height"]?.jsonPrimitive?.intOrNull ?: 180,
            contentJson = contentJson
        )
    }

    private fun parseChartContent(obj: JsonObject): String {
        val inputs = ShinobiChartCalculator.parseInputs(obj)
        return buildJsonObject {
            put("taijutsu", inputs.taijutsu)
            put("ninjutsu", inputs.ninjutsu)
            put("genjutsu", inputs.genjutsu)
            put("vigor", inputs.vigor)
            put("inteligencia", inputs.inteligencia)
            put("chakraMax", inputs.chakraMax)
            putJsonObject("inputs") {
                put("taijutsu", inputs.taijutsu)
                put("ninjutsu", inputs.ninjutsu)
                put("genjutsu", inputs.genjutsu)
                put("vigor", inputs.vigor)
                put("inteligencia", inputs.inteligencia)
                put("chakraMax", inputs.chakraMax)
            }
        }.toString()
    }

    private fun parseTextContent(obj: JsonObject): String {
        val campos = obj["campos"]?.jsonArray
        val rawHtml = if (!campos.isNullOrEmpty()) {
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
            obj["html"]?.jsonPrimitive?.contentOrNull
                ?: obj["text"]?.jsonPrimitive?.contentOrNull
                ?: obj["value"]?.jsonPrimitive?.contentOrNull
                ?: ""
        }

        return buildJsonObject {
            put("text", rawHtml)
            put("html", rawHtml)
            put("fontSize", obj["fontSize"]?.jsonPrimitive?.intOrNull ?: 13)
            put("align", obj["align"]?.jsonPrimitive?.contentOrNull ?: "left")
            if (campos != null) {
                put("campos", campos)
            }
        }.toString()
    }

    private fun parseImageContent(obj: JsonObject): String {
        val url = obj["url"]?.jsonPrimitive?.contentOrNull ?: obj["src"]?.jsonPrimitive?.contentOrNull ?: ""
        val imgId = obj["imgId"]?.jsonPrimitive?.contentOrNull ?: obj["id"]?.jsonPrimitive?.contentOrNull ?: ""

        return buildJsonObject {
            put("url", url)
            if (imgId.isNotEmpty()) put("imgId", imgId)
        }.toString()
    }
}
