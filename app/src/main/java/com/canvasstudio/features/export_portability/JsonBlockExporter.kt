package com.canvasstudio.features.export_portability

import com.canvasstudio.data.local.entity.BlockEntity
import com.canvasstudio.ui.block.modules.ShinobiChartCalculator
import kotlinx.serialization.json.*

object JsonBlockExporter {

    fun exportToJson(brandTitle: String, blocks: List<BlockEntity>): String {
        val json = Json { prettyPrint = true }
        val root = buildJsonObject {
            putJsonObject("metadata") {
                put("versao", "2.0.0")
                put("timestamp", System.currentTimeMillis())
                put("brand", brandTitle)
            }

            val blocksObj = buildJsonObject {
                blocks.forEach { block ->
                    val blockKey = "data_${block.type.firstOrNull() ?: 't'}_${block.id}_${System.currentTimeMillis() % 100000}"
                    put(blockKey, buildBlockJson(block))
                }
            }

            put("blocos", blocksObj)
            put("workspaceData", blocksObj)
        }
        return json.encodeToString(JsonObject.serializer(), root)
    }

    private fun buildBlockJson(block: BlockEntity): JsonObject {
        return buildJsonObject {
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

            when (block.type.lowercase()) {
                "image" -> {
                    val url = content?.get("url")?.jsonPrimitive?.contentOrNull ?: ""
                    put("url", url)
                    content?.get("imgId")?.jsonPrimitive?.contentOrNull?.let { put("imgId", it) }
                }
                "text" -> {
                    val html = content?.get("html")?.jsonPrimitive?.contentOrNull
                        ?: content?.get("text")?.jsonPrimitive?.contentOrNull
                        ?: ""

                    val finalHtml = if (html.contains("<") && html.contains(">")) {
                        html
                    } else {
                        html
                            .replace(Regex("\\*\\*(.*?)\\*\\*"), "<b>$1</b>")
                            .replace(Regex("\\*(.*?)\\*"), "<i>$1</i>")
                            .replace("\n", "<br>")
                    }

                    putJsonArray("campos") {
                        addJsonObject {
                            put("html", finalHtml)
                            put("className", "sub-campo")
                        }
                    }
                }
                "chart" -> {
                    val inputs = ShinobiChartCalculator.parseInputs(content ?: buildJsonObject {})
                    val inputsObj = buildJsonObject {
                        put("taijutsu", inputs.taijutsu)
                        put("ninjutsu", inputs.ninjutsu)
                        put("genjutsu", inputs.genjutsu)
                        put("vigor", inputs.vigor)
                        put("inteligencia", inputs.inteligencia)
                        put("chakraMax", inputs.chakraMax)
                    }
                    put("inputs", inputsObj)
                    put("status", inputsObj)
                }
            }
        }
    }
}
