package com.canvasstudio.features.export_portability

import com.canvasstudio.data.local.entity.BlockEntity
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

class JsonPortabilityTest {

    @Test
    fun `HtmlContentConverter parseHtmlToElements converte tabela HTML em elemento estruturado`() {
        val html = """
            <table>
                <thead>
                    <tr><th>Nome</th><th>Nível</th></tr>
                </thead>
                <tbody>
                    <tr><td>Naruto</td><td>Hokage</td></tr>
                    <tr><td>Sasuke</td><td>Jounin</td></tr>
                </tbody>
            </table>
        """.trimIndent()

        val elements = HtmlContentConverter.parseHtmlToElements(html)

        assertEquals(1, elements.size)
        val tableObj = elements[0].jsonObject
        assertEquals("table", tableObj["type"]?.jsonPrimitive?.content)

        val headers = tableObj["headers"]?.jsonArray?.map { it.jsonPrimitive.content }
        assertEquals(listOf("Nome", "Nível"), headers)

        val rows = tableObj["rows"]?.jsonArray
        assertEquals(2, rows?.size)
    }

    @Test
    fun `HtmlContentConverter cleanHtmlContent remove tags html e preserva formatacao markdown`() {
        val raw = "<p>Olá <b>Mundo</b>!</p>"
        val clean = HtmlContentConverter.cleanHtmlContent(raw)
        assertEquals("Olá **Mundo**!", clean)
    }

    @Test
    fun `JsonBlockExporter exportToJson gera esquema v2 com brand e blocos`() {
        val blocks = listOf(
            BlockEntity(1, 0, "Título 1", "text", 100f, 200f, 300, 150, """{"text": "Conteúdo"}"""),
            BlockEntity(2, 0, "Radar", "chart", 400f, 500f, 250, 250, """{"ninjutsu": 5}""")
        )

        val jsonString = JsonBlockExporter.exportToJson("Meu App", blocks)
        val json = Json.parseToJsonElement(jsonString).jsonObject

        val meta = json["metadata"]?.jsonObject
        assertEquals("2.0.0", meta?.get("versao")?.jsonPrimitive?.content)
        assertEquals("Meu App", meta?.get("brand")?.jsonPrimitive?.content)

        val exportedBlocks = json["blocos"]?.jsonObject
        assertNotNull(exportedBlocks)
        assertEquals(2, exportedBlocks?.size)
    }

    @Test
    fun `JsonBlockParser parseBlocksFromJson deserializa formato v2`() {
        val jsonV2 = """
            {
                "metadata": {
                    "versao": "2.0.0",
                    "brand": "Ficha Shinobi"
                },
                "blocos": {
                    "data_t_1": {
                        "id": 1,
                        "title": "Anotações",
                        "type": "text",
                        "left": "50px",
                        "top": "100px",
                        "width": 200,
                        "height": 180,
                        "campos": [
                            {
                                "html": "Missão concluída",
                                "className": "sub-campo"
                            }
                        ]
                    }
                }
            }
        """.trimIndent()

        val (brand, blocks) = JsonBlockParser.parseBlocksFromJson(jsonV2, projectId = 42)

        assertEquals("Ficha Shinobi", brand)
        assertEquals(1, blocks.size)

        val block = blocks[0]
        assertEquals(42L, block.projectId)
        assertEquals("Anotações", block.title)
        assertEquals("text", block.type)
        assertEquals(50f, block.posX, 0.01f)
        assertEquals(100f, block.posY, 0.01f)

        val content = Json.parseToJsonElement(block.contentJson).jsonObject
        assertEquals("Missão concluída", content["text"]?.jsonPrimitive?.content)
    }

    @Test
    fun `JsonBlockParser parseBlocksFromJson deserializa formato v1 com retrocompatibilidade`() {
        val jsonV1 = """
            {
                "app_brand_title": "Projeto Legado",
                "blocks": [
                    {
                        "id": 10,
                        "title": "Bloco Antigo",
                        "type": "text",
                        "left": "80px",
                        "top": "120px",
                        "width": 300,
                        "height": 200,
                        "text": "Texto v1"
                    }
                ]
            }
        """.trimIndent()

        val (brand, blocks) = JsonBlockParser.parseBlocksFromJson(jsonV1, projectId = 1)

        assertEquals("Projeto Legado", brand)
        assertEquals(1, blocks.size)

        val block = blocks[0]
        assertEquals("Bloco Antigo", block.title)
        assertEquals(80f, block.posX, 0.01f)
        assertEquals(120f, block.posY, 0.01f)
    }
}
