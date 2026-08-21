package com.canvasstudio.ui.block.delegates

import com.canvasstudio.data.local.entity.BlockEntity
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

class BlockPropertyUpdaterTest {

    private val baseBlock = BlockEntity(
        id = 1,
        projectId = 0,
        title = "Bloco Teste",
        type = "text",
        posX = 100f,
        posY = 100f,
        width = 200,
        height = 150,
        contentJson = """{"text": "Texto original", "fontSize": 13}"""
    )

    @Test
    fun `updateValue insere valor e valorFormatted quando positivo`() {
        val updated = BlockPropertyUpdater.updateValue(baseBlock, 250.50f)
        val json = Json.parseToJsonElement(updated.contentJson).jsonObject

        assertEquals(250.50f, json["valor"]?.jsonPrimitive?.float)
        assertTrue(json["valorFormatted"]?.jsonPrimitive?.content?.contains("250,50") == true)
        assertEquals("Texto original", json["text"]?.jsonPrimitive?.content)
    }

    @Test
    fun `updateValue remove valor quando nulo ou menor igual a zero`() {
        val blockWithValue = BlockPropertyUpdater.updateValue(baseBlock, 100f)
        val cleared = BlockPropertyUpdater.updateValue(blockWithValue, null)
        val json = Json.parseToJsonElement(cleared.contentJson).jsonObject

        assertNull(json["valor"])
        assertNull(json["valorFormatted"])
    }

    @Test
    fun `updateRealizadoEm insere ou remove data`() {
        val updated = BlockPropertyUpdater.updateRealizadoEm(baseBlock, "21/08/2026")
        val json1 = Json.parseToJsonElement(updated.contentJson).jsonObject
        assertEquals("21/08/2026", json1["realizadoEm"]?.jsonPrimitive?.content)

        val cleared = BlockPropertyUpdater.updateRealizadoEm(updated, "")
        val json2 = Json.parseToJsonElement(cleared.contentJson).jsonObject
        assertNull(json2["realizadoEm"])
    }

    @Test
    fun `updatePartyField atualiza pagador e destinatario de forma independente`() {
        var block = BlockPropertyUpdater.updatePartyField(baseBlock, "pagador", "João Silva")
        block = BlockPropertyUpdater.updatePartyField(block, "destinatario", "Maria Santos")

        val json = Json.parseToJsonElement(block.contentJson).jsonObject
        assertEquals("João Silva", json["pagador"]?.jsonPrimitive?.content)
        assertEquals("Maria Santos", json["destinatario"]?.jsonPrimitive?.content)
    }

    @Test
    fun `updateContentText atualiza texto e remove array elements`() {
        val blockWithElements = baseBlock.copy(
            contentJson = """{"text": "Antigo", "elements": [{"type": "text", "value": "x"}]}"""
        )
        val updated = BlockPropertyUpdater.updateContentText(blockWithElements, "Novo Texto")
        val json = Json.parseToJsonElement(updated.contentJson).jsonObject

        assertEquals("Novo Texto", json["text"]?.jsonPrimitive?.content)
        assertNull(json["elements"])
    }

    @Test
    fun `updateFormatting atualiza parametros e preserva outros nós`() {
        val updated = BlockPropertyUpdater.updateFormatting(
            block = baseBlock,
            fontSize = 16,
            isBold = true,
            isItalic = true,
            align = "center",
            textColor = "#FF0000"
        )
        val json = Json.parseToJsonElement(updated.contentJson).jsonObject

        assertEquals(16, json["fontSize"]?.jsonPrimitive?.int)
        assertEquals(true, json["isBold"]?.jsonPrimitive?.boolean)
        assertEquals(true, json["isItalic"]?.jsonPrimitive?.boolean)
        assertEquals("center", json["align"]?.jsonPrimitive?.content)
        assertEquals("#FF0000", json["textColor"]?.jsonPrimitive?.content)
        assertEquals("Texto original", json["text"]?.jsonPrimitive?.content)
    }

    @Test
    fun `updateChartAttribute atualiza valor numerico`() {
        val chartBlock = baseBlock.copy(type = "chart", contentJson = """{"ninjutsu": 5.0}""")
        val updated = BlockPropertyUpdater.updateChartAttribute(chartBlock, "taijutsu", 8.5f)
        val json = Json.parseToJsonElement(updated.contentJson).jsonObject

        assertEquals(8.5f, json["taijutsu"]?.jsonPrimitive?.float)
    }
}
