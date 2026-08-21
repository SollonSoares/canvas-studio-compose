package com.canvasstudio.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import com.canvasstudio.data.local.entity.BlockEntity
import com.canvasstudio.designsystem.tokens.DarkCanvasColors
import com.canvasstudio.domain.model.CanvasConfig
import com.canvasstudio.ui.block.components.BlockHeader
import com.canvasstudio.ui.block.components.BlockPartyDetails
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class DraggableBlockUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `BlockHeader exibe badges PIX e valor financeiro formatado`() {
        val metadata = buildJsonObject {
            put("isPix", true)
            put("valorFormatted", "R$ 350,00")
        }
        val block = BlockEntity(1, 0, "Pix de Teste", "text", 0f, 0f, 200, 100, metadata.toString())
        var duplicateClicked = false
        var deleteClicked = false

        composeTestRule.setContent {
            BlockHeader(
                key = 1L,
                block = block,
                isSelected = false,
                isLocked = false,
                colors = DarkCanvasColors,
                metadata = metadata,
                canvasConfig = CanvasConfig(showFinancialBadges = true),
                density = Density(1f),
                onMove = { _, _ -> },
                onTitleChange = {},
                onDuplicate = { duplicateClicked = true },
                onDelete = { deleteClicked = true },
                onInteractionChange = { _, _, _ -> }
            )
        }

        // 1. Verifica badges
        composeTestRule.onNodeWithText("PIX").assertIsDisplayed()
        composeTestRule.onNodeWithText("R$ 350,00").assertIsDisplayed()

        // 2. Clica em duplicar
        composeTestRule.onNodeWithContentDescription("Duplicar").performClick()
        assertTrue(duplicateClicked)

        // 3. Clica em excluir
        composeTestRule.onNodeWithContentDescription("Excluir Bloco").performClick()
        assertTrue(deleteClicked)
    }

    @Test
    fun `BlockPartyDetails exibe campos De, Para e Banco`() {
        val metadata = buildJsonObject {
            put("pagador", "Carlos Silva")
            put("destinatario", "Maria Souza")
            put("instituicao", "Nubank")
            put("realizadoEm", "21/08/2026")
        }

        composeTestRule.setContent {
            BlockPartyDetails(
                metadata = metadata,
                canvasConfig = CanvasConfig(showPartyDetails = true),
                colors = DarkCanvasColors
            )
        }

        composeTestRule.onNodeWithText("📤 De: Carlos Silva").assertIsDisplayed()
        composeTestRule.onNodeWithText("📥 Para: Maria Souza").assertIsDisplayed()
        composeTestRule.onNodeWithText("🏦 Banco: Nubank").assertIsDisplayed()
        composeTestRule.onNodeWithText("Realizado em: 21/08/2026").assertIsDisplayed()
    }
}
