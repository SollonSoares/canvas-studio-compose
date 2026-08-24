package com.canvasstudio.ui.block.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HtmlDocumentBridgeTest {

    @Test
    fun `fromHtml converte tabela HTML em VisualElement Table sem expor tags`() {
        val html = """
            <h2>Tabela de Monstro</h2>
            <table>
                <thead>
                    <tr><th>Nível</th><th>HP</th><th>Ataque</th></tr>
                </thead>
                <tbody>
                    <tr><td>1</td><td>100</td><td>15</td></tr>
                    <tr><td>2</td><td>250</td><td>35</td></tr>
                </tbody>
            </table>
        """.trimIndent()

        val elements = HtmlDocumentBridge.fromHtml(html)
        assertTrue(elements.isNotEmpty())

        val heading = elements.filterIsInstance<VisualElement.Heading>().firstOrNull()
        assertEquals("Tabela de Monstro", heading?.text)
        assertEquals(2, heading?.level)

        val table = elements.filterIsInstance<VisualElement.Table>().firstOrNull()
        assertEquals(3, table?.headers?.size)
        assertEquals(listOf("Nível", "HP", "Ataque"), table?.headers)
        assertEquals(2, table?.rows?.size)
        assertEquals(listOf("1", "100", "15"), table?.rows?.get(0))
        assertEquals(listOf("2", "250", "35"), table?.rows?.get(1))
    }

    @Test
    fun `toHtml serializa elementos visuais para HTML valido compativel com web studio`() {
        val elements = listOf(
            VisualElement.Heading(text = "Seção 1", level = 2),
            VisualElement.Paragraph(text = "Texto explicativo", isBold = true),
            VisualElement.Table(
                headers = mutableListOf("Chave", "Valor"),
                rows = mutableListOf(mutableListOf("Status", "Ativo"))
            )
        )

        val html = HtmlDocumentBridge.toHtml(elements)
        assertTrue(html.contains("<h2"))
        assertTrue(html.contains("Seção 1</h2>"))
        assertTrue(html.contains("<b>Texto explicativo</b>"))
        assertTrue(html.contains("<table"))
        assertTrue(html.contains("Chave</th>"))
        assertTrue(html.contains("Status</td>"))
    }
}
