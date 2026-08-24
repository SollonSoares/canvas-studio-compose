package com.canvasstudio.ui.block.delegates

import com.canvasstudio.data.local.entity.BlockEntity
import com.canvasstudio.ui.block.utils.ReceiptAnalyzer
import kotlinx.serialization.json.*

object BlockPropertyUpdater {

    fun updateValue(block: BlockEntity, newValue: Float?): BlockEntity {
        val mutableMap = parseContentToMap(block.contentJson)
        if (newValue != null && newValue > 0f) {
            mutableMap["valor"] = JsonPrimitive(newValue)
            mutableMap["valorFormatted"] = JsonPrimitive(ReceiptAnalyzer.formatCurrency(newValue))
        } else {
            mutableMap.remove("valor")
            mutableMap.remove("valorFormatted")
        }
        return block.copy(contentJson = JsonObject(mutableMap).toString())
    }

    fun updateRealizadoEm(block: BlockEntity, newDate: String): BlockEntity {
        val mutableMap = parseContentToMap(block.contentJson)
        if (newDate.isNotBlank()) {
            mutableMap["realizadoEm"] = JsonPrimitive(newDate)
        } else {
            mutableMap.remove("realizadoEm")
        }
        return block.copy(contentJson = JsonObject(mutableMap).toString())
    }

    fun updatePartyField(block: BlockEntity, key: String, value: String): BlockEntity {
        val mutableMap = parseContentToMap(block.contentJson)
        if (value.isNotBlank()) {
            mutableMap[key] = JsonPrimitive(value)
        } else {
            mutableMap.remove(key)
        }
        return block.copy(contentJson = JsonObject(mutableMap).toString())
    }

    fun updateContentText(block: BlockEntity, newText: String): BlockEntity {
        return try {
            val mutableMap = parseContentToMap(block.contentJson)
            mutableMap["text"] = JsonPrimitive(newText)
            mutableMap["html"] = JsonPrimitive(newText)
            mutableMap.remove("elements")
            mutableMap.remove("campos")
            block.copy(contentJson = JsonObject(mutableMap).toString())
        } catch (e: Exception) {
            block.copy(contentJson = buildJsonObject { put("text", newText); put("html", newText) }.toString())
        }
    }

    fun updateFormatting(
        block: BlockEntity,
        fontSize: Int? = null,
        isBold: Boolean? = null,
        isItalic: Boolean? = null,
        align: String? = null,
        textColor: String? = null
    ): BlockEntity {
        val mutableMap = parseContentToMap(block.contentJson)
        fontSize?.let { mutableMap["fontSize"] = JsonPrimitive(it) }
        isBold?.let { mutableMap["isBold"] = JsonPrimitive(it) }
        isItalic?.let { mutableMap["isItalic"] = JsonPrimitive(it) }
        align?.let { mutableMap["align"] = JsonPrimitive(it) }
        textColor?.let {
            if (it.isEmpty()) mutableMap.remove("textColor") else mutableMap["textColor"] = JsonPrimitive(it)
        }
        return block.copy(contentJson = JsonObject(mutableMap).toString())
    }

    fun updateChartAttribute(block: BlockEntity, attribute: String, value: Float): BlockEntity {
        val mutableMap = parseContentToMap(block.contentJson)
        mutableMap[attribute] = JsonPrimitive(value)
        val inputs = (mutableMap["inputs"] as? JsonObject)?.toMutableMap()
        if (inputs != null) {
            inputs[attribute] = JsonPrimitive(value)
            mutableMap["inputs"] = JsonObject(inputs)
        }
        return block.copy(contentJson = JsonObject(mutableMap).toString())
    }

    fun updateImageUrl(block: BlockEntity, url: String): BlockEntity {
        val mutableMap = parseContentToMap(block.contentJson)
        mutableMap["url"] = JsonPrimitive(url)
        return block.copy(contentJson = JsonObject(mutableMap).toString())
    }

    private fun parseContentToMap(contentJson: String): MutableMap<String, JsonElement> {
        return try {
            Json.parseToJsonElement(contentJson).jsonObject.toMutableMap()
        } catch (e: Exception) {
            mutableMapOf()
        }
    }
}
