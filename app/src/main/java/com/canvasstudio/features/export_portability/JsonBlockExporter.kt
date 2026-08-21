package com.canvasstudio.features.export_portability

import com.canvasstudio.data.local.entity.BlockEntity
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

            putJsonObject("blocos") {
                blocks.forEach { block ->
                    val blockKey = "data_t_${block.id}_${System.currentTimeMillis() % 100000}"
                    put(blockKey, buildBlockJson(block))
                }
            }
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
        }
    }
}
