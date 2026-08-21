package com.canvasstudio.ui.block.delegates

import com.canvasstudio.data.local.entity.BlockEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CanvasSearchEngineTest {

    private lateinit var searchEngine: CanvasSearchEngine

    @Before
    fun setup() {
        searchEngine = CanvasSearchEngine()
    }

    @Test
    fun `filterBlocks com query vazia retorna todos os blocos do modulo ativo`() {
        val blocks = listOf(
            BlockEntity(1, 0, "Bloco Texto", "text", 0f, 0f, 200, 100, "{\"text\":\"Ola\"}"),
            BlockEntity(2, 0, "Bloco Imagem", "image", 0f, 0f, 200, 100, "{\"url\":\"http://img\"}")
        )
        val modules = mapOf("text" to true, "image" to true)

        val result = searchEngine.filterBlocks(blocks, "", modules)
        assertEquals(2, result.size)
    }

    @Test
    fun `filterBlocks filtra por visibilidade de modulo`() {
        val blocks = listOf(
            BlockEntity(1, 0, "Texto 1", "text", 0f, 0f, 200, 100, "{\"text\":\"Ola\"}"),
            BlockEntity(2, 0, "Imagem 1", "image", 0f, 0f, 200, 100, "{\"url\":\"http://img\"}")
        )
        val modules = mapOf("text" to true, "image" to false)

        val result = searchEngine.filterBlocks(blocks, "", modules)
        assertEquals(1, result.size)
        assertEquals("Texto 1", result[0].title)
    }

    @Test
    fun `filterBlocks realiza normalizacao NFD de acentos e maiusculas`() {
        val blocks = listOf(
            BlockEntity(1, 0, "Comprovante João", "text", 0f, 0f, 200, 100, "{\"text\":\"Transferência efetuada com SUCESSO\"}"),
            BlockEntity(2, 0, "Outro Bloco", "text", 0f, 0f, 200, 100, "{\"text\":\"Sem relacao\"}")
        )
        val modules = mapOf("text" to true)

        // Busca sem acento "joao" deve achar "João"
        val result1 = searchEngine.filterBlocks(blocks, "joao", modules)
        assertEquals(1, result1.size)
        assertEquals(1L, result1[0].id)

        // Busca "transferencia" sem acento deve achar "Transferência"
        val result2 = searchEngine.filterBlocks(blocks, "transferencia", modules)
        assertEquals(1, result2.size)

        // Busca em maiúsculas
        val result3 = searchEngine.filterBlocks(blocks, "SUCESSO", modules)
        assertEquals(1, result3.size)
    }

    @Test
    fun `filterBlocks suporta busca multi-palavras com AND`() {
        val blocks = listOf(
            BlockEntity(1, 0, "Pix Nubank", "text", 0f, 0f, 200, 100, "{\"text\":\"Pagador: Carlos Eduardo\"}"),
            BlockEntity(2, 0, "Pix Itau", "text", 0f, 0f, 200, 100, "{\"text\":\"Pagador: Carlos Silva\"}")
        )
        val modules = mapOf("text" to true)

        // "pix nubank carlos" deve achar apenas o bloco 1
        val result = searchEngine.filterBlocks(blocks, "pix nubank carlos", modules)
        assertEquals(1, result.size)
        assertEquals("Pix Nubank", result[0].title)
    }

    @Test
    fun `filterBlocks extrai texto de JSON aninhado e tabelas`() {
        val jsonTable = """
            {
                "headers": ["Item", "Preço"],
                "rows": [["Espada Ninja", "500 Ryo"], ["Kunai", "50 Ryo"]]
            }
        """.trimIndent()
        val blocks = listOf(
            BlockEntity(1, 0, "Inventário", "table", 0f, 0f, 200, 100, jsonTable)
        )
        val modules = mapOf("table" to true)

        val result = searchEngine.filterBlocks(blocks, "Kunai", modules)
        assertEquals(1, result.size)
    }

    @Test
    fun `setSearchQuery e applySearch atualizam StateFlows corretamente`() {
        searchEngine.setSearchQuery("teste")
        assertEquals("teste", searchEngine.searchQuery.value)
        assertEquals("", searchEngine.appliedQuery.value)

        searchEngine.applySearch()
        assertEquals("teste", searchEngine.appliedQuery.value)

        searchEngine.setSearchQuery("")
        assertEquals("", searchEngine.searchQuery.value)
        assertEquals("", searchEngine.appliedQuery.value)
    }
}
