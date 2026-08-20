package com.canvasstudio.ui.block

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.canvasstudio.data.local.entity.BlockEntity
import com.canvasstudio.ui.block.components.DraggableBlock
import com.canvasstudio.ui.theme.CanvasColors
import androidx.compose.ui.graphics.Color
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Bateria completa de testes funcionais e não funcionais para o Bloco de Imagem.
 */
@RunWith(AndroidJUnit4::class)
class ImageBlockFeatureTest {

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
    fun cenario01_renderizacaoBlocoImagemComUrlValida() {
        val imageBlock = BlockEntity(
            id = 20,
            projectId = 1,
            title = "Foto do Personagem",
            type = "image",
            posX = 100f,
            posY = 100f,
            width = 250,
            height = 250,
            contentJson = """{"url": "https://example.com/ninja.png"}"""
        )

        composeTestRule.setContent {
            com.canvasstudio.ui.block.dialogs.EditBlockDialog(
                block = imageBlock,
                onDismiss = {},
                onConfirm = {},
                onLiveUpdate = {},
                colors = testColors
            )
        }

        composeTestRule.onNodeWithText("Foto do Personagem").assertIsDisplayed()
        composeTestRule.onNodeWithText("https://example.com/ninja.png").assertIsDisplayed()
    }

    // =========================================================================
    // CENÁRIOS NÃO FUNCIONAIS
    // =========================================================================

    @Test
    fun cenario02_estadoVazioUrlAusente() {
        val emptyImageBlock = BlockEntity(
            id = 21,
            projectId = 1,
            title = "Imagem Sem URL",
            type = "image",
            posX = 100f,
            posY = 100f,
            width = 250,
            height = 250,
            contentJson = """{"url": ""}"""
        )

        composeTestRule.setContent {
            com.canvasstudio.ui.block.dialogs.EditBlockDialog(
                block = emptyImageBlock,
                onDismiss = {},
                onConfirm = {},
                onLiveUpdate = {},
                colors = testColors
            )
        }

        composeTestRule.onNodeWithText("Imagem Sem URL").assertIsDisplayed()
        composeTestRule.onNodeWithText("URL da imagem").assertIsDisplayed()
    }
}
