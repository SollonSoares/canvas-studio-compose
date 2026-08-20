package com.canvasstudio.ui.block

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.canvasstudio.data.local.entity.BlockEntity
import com.canvasstudio.ui.block.components.DraggableBlock
import com.canvasstudio.ui.block.utils.parseRichText
import com.canvasstudio.ui.theme.CanvasColors
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Bateria completa de testes funcionais e não funcionais para Blocos de Texto e Markdown.
 */
@RunWith(AndroidJUnit4::class)
class TextBlockFeatureTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testColors = CanvasColors(
        bgMain = Color.White,
        bgMenu = Color(0xFFF5F5F7),
        bgCard = Color.White,
        bgInput = Color.LightGray.copy(alpha = 0.1f),
        bgButton = Color.LightGray.copy(alpha = 0.2f),
        bgButtonHover = Color.LightGray.copy(alpha = 0.3f),
        accent = Color(0xFF0071E3),
        canvasGrid = Color.LightGray,
        textMain = Color.Black,
        textSecondary = Color.DarkGray,
        textMuted = Color.Gray,
        danger = Color(0xFFFF3B30),
        border = Color.LightGray,
        borderSubtle = Color.LightGray.copy(alpha = 0.5f)
    )

    // =========================================================================
    // CENÁRIOS FUNCIONAIS
    // =========================================================================

    @Test
    fun cenario01_renderizacaoMarkdownNegritoEItalico() {
        val richText = parseRichText("Texto com **Negrito** e *Itálico*")
        
        assertEquals("Texto com Negrito e Itálico", richText.text)
        
        val boldSpan = richText.spanStyles.find { it.item.fontWeight == FontWeight.Bold }
        val italicSpan = richText.spanStyles.find { it.item.fontStyle == FontStyle.Italic }
        
        assert(boldSpan != null) { "Estilo de negrito deve ser aplicado" }
        assert(italicSpan != null) { "Estilo de itálico deve ser aplicado" }
    }

    @Test
    fun cenario02_renderizacaoBlocoDeTextoNaUi() {
        val textBlock = BlockEntity(
            id = 10,
            projectId = 1,
            title = "Anotações Shinobi",
            type = "text",
            posX = 50f,
            posY = 50f,
            width = 250,
            height = 200,
            contentJson = """{"text": "**Missão:** Proteger a vila de Konoha.", "titleSize": 13, "align": "left"}"""
        )

        val richText = parseRichText("**Missão:** Proteger a vila de Konoha.")

        composeTestRule.setContent {
            androidx.compose.material.Text(text = richText)
        }

        composeTestRule.onNodeWithText("Missão: Proteger a vila de Konoha.").assertIsDisplayed()
    }

    // =========================================================================
    // CENÁRIOS NÃO FUNCIONAIS
    // =========================================================================

    @Test
    fun cenario03_resilienciaTextoVazioOuSemTags() {
        val emptyRichText = parseRichText("")
        assertEquals("", emptyRichText.text)

        val plainText = parseRichText("Texto simples sem qualquer tag de formatação")
        assertEquals("Texto simples sem qualquer tag de formatação", plainText.text)
        assertEquals(0, plainText.spanStyles.size)
    }

    @Test
    fun cenario04_resilienciaTextoMuitoLongo() {
        val longContent = "Parágrafo de teste. ".repeat(500)
        val richText = parseRichText(longContent)
        
        assertEquals(longContent.length, richText.text.length)
    }
}
