package com.canvasstudio.ui.block.delegates

import com.canvasstudio.data.local.entity.BlockEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CanvasAutoOrganizerTest {

    @Test
    fun `organize com lista vazia retorna lista vazia`() {
        val result = CanvasAutoOrganizer.organize(emptyList(), 2000)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `organize com um bloco posiciona no ponto inicial snapado`() {
        val block = BlockEntity(id = 1, projectId = 0, title = "A", type = "text", posX = 500f, posY = 500f, width = 200, height = 150, contentJson = "{}")
        val result = CanvasAutoOrganizer.organize(listOf(block), 2000)

        assertEquals(1, result.size)
        assertEquals(40f, result[0].posX, 0.01f)
        assertEquals(40f, result[0].posY, 0.01f)
    }

    @Test
    fun `organize quebra linha horizontal quando ultrapassa largura maxima`() {
        // Canvas width = 500. Max width = 500 - 40 = 460.
        // Bloco A: startX=40, width=200 -> nextX = 40 + 200 + 20 = 260.
        // Bloco B: startX=260, width=200 -> nextX = 260 + 200 + 20 = 480 (> 460). Quebra de linha!
        // Bloco B deve ir para posX=40, posY=40 + heightA(100) + padding(20) = 160.
        val blockA = BlockEntity(id = 1, projectId = 0, title = "Alpha", type = "text", posX = 0f, posY = 0f, width = 200, height = 100, contentJson = "{}")
        val blockB = BlockEntity(id = 2, projectId = 0, title = "Beta", type = "text", posX = 0f, posY = 0f, width = 200, height = 120, contentJson = "{}")

        val result = CanvasAutoOrganizer.organize(listOf(blockB, blockA), 480)

        assertEquals(2, result.size)
        // Ordenado alfabeticamente: Alpha primeiro, depois Beta
        val sortedAlpha = result[0]
        val sortedBeta = result[1]

        assertEquals("Alpha", sortedAlpha.title)
        assertEquals(40f, sortedAlpha.posX, 0.01f)
        assertEquals(40f, sortedAlpha.posY, 0.01f)

        assertEquals("Beta", sortedBeta.title)
        assertEquals(40f, sortedBeta.posX, 0.01f)
        assertEquals(160f, sortedBeta.posY, 0.01f) // 40 + 100 + 20 = 160
    }

    @Test
    fun `organize alinha blocos na mesma linha quando ha espaco suficiente`() {
        val blockA = BlockEntity(id = 1, projectId = 0, title = "A", type = "text", posX = 0f, posY = 0f, width = 200, height = 100, contentJson = "{}")
        val blockB = BlockEntity(id = 2, projectId = 0, title = "B", type = "text", posX = 0f, posY = 0f, width = 150, height = 100, contentJson = "{}")

        val result = CanvasAutoOrganizer.organize(listOf(blockA, blockB), 2000)

        assertEquals(2, result.size)
        assertEquals(40f, result[0].posX, 0.01f)
        assertEquals(40f, result[0].posY, 0.01f)

        assertEquals(260f, result[1].posX, 0.01f) // 40 + 200 + 20 = 260
        assertEquals(40f, result[1].posY, 0.01f)
    }

    @Test
    fun `organize mantem snap em multiplos de 20`() {
        val block = BlockEntity(id = 1, projectId = 0, title = "Z", type = "text", posX = 123f, posY = 456f, width = 200, height = 100, contentJson = "{}")
        val result = CanvasAutoOrganizer.organize(listOf(block), 2000)

        assertEquals(0, result[0].posX.toInt() % 20)
        assertEquals(0, result[0].posY.toInt() % 20)
    }
}
