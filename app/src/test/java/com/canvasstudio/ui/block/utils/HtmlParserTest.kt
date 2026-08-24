package com.canvasstudio.ui.block.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HtmlParserTest {

    @Test
    fun `parse extrai tabelas HTML com cabecalhos e linhas corretamente`() {
        val tableHtml = """
            <h2>Tabela de Atributos</h2>
            <table>
                <thead>
                    <tr><th>Atributo</th><th>Valor</th><th>Bônus</th></tr>
                </thead>
                <tbody>
                    <tr><td>Força</td><td>18</td><td>+4</td></tr>
                    <tr><td>Destreza</td><td>14</td><td>+2</td></tr>
                </tbody>
            </table>
        """.trimIndent()

        val nodes = HtmlParser.parse(tableHtml)
        assertTrue(nodes.isNotEmpty())

        val heading = nodes.filterIsInstance<HtmlNode.Heading>().firstOrNull()
        assertEquals(2, heading?.level)
        assertEquals("Tabela de Atributos", heading?.text)

        val table = nodes.filterIsInstance<HtmlNode.Table>().firstOrNull()
        assertEquals(3, table?.headers?.size)
        assertEquals("Atributo", table?.headers?.get(0))
        assertEquals("Valor", table?.headers?.get(1))
        assertEquals("Bônus", table?.headers?.get(2))

        assertEquals(2, table?.rows?.size)
        assertEquals(listOf("Força", "18", "+4"), table?.rows?.get(0))
        assertEquals(listOf("Destreza", "14", "+2"), table?.rows?.get(1))
    }

    @Test
    fun `parse identifica headings e callout boxes`() {
        val html = """
            <h1>Título Principal</h1>
            <div style="background: #0284c7; border: 1px solid #38bdf8;">Aviso importante!</div>
            <pre>val x = 10</pre>
            <ul><li>Item A</li><li>Item B</li></ul>
        """.trimIndent()

        val nodes = HtmlParser.parse(html)
        assertTrue(nodes.any { it is HtmlNode.Heading && it.level == 1 })
        assertTrue(nodes.any { it is HtmlNode.CalloutBox })
        assertTrue(nodes.any { it is HtmlNode.PreBlock })
        assertTrue(nodes.any { it is HtmlNode.ListItem })
    }

    @Test
    fun `cleanHtmlTags remove todas as tags mantendo texto puro`() {
        val raw = "<b>Texto</b> em <i>itálico</i> com <a href='#'>link</a>"
        val clean = HtmlParser.cleanHtmlTags(raw)
        assertEquals("Texto em itálico com link", clean)
    }
}
