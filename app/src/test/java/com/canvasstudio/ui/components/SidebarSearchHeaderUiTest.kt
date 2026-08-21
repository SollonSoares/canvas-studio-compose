package com.canvasstudio.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.canvasstudio.designsystem.tokens.DarkCanvasColors
import com.canvasstudio.ui.block.components.SidebarSearchHeader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class SidebarSearchHeaderUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `SidebarSearchHeader renderiza input e aciona busca ao clicar no botao`() {
        var query = ""
        var searchClicked = false

        composeTestRule.setContent {
            SidebarSearchHeader(
                query = query,
                onQueryChange = { query = it },
                onSearch = { searchClicked = true },
                colors = DarkCanvasColors
            )
        }

        // 1. Verifica se o placeholder e o botão Buscar estão na tela
        composeTestRule.onNodeWithText("Buscar blocos...").assertIsDisplayed()
        composeTestRule.onNodeWithText("Buscar").assertIsDisplayed()

        // 2. Clica no botão Buscar
        composeTestRule.onNodeWithText("Buscar").performClick()
        assertTrue(searchClicked)
    }

    @Test
    fun `SidebarSearchHeader exibe botao limpar quando ha texto digitado`() {
        var query = "comprovante"
        var cleared = false

        composeTestRule.setContent {
            SidebarSearchHeader(
                query = query,
                onQueryChange = {
                    query = it
                    if (it.isEmpty()) cleared = true
                },
                onSearch = {},
                colors = DarkCanvasColors
            )
        }

        // Verifica botão de limpar
        composeTestRule.onNodeWithContentDescription("Limpar").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Limpar").performClick()

        assertTrue(cleared)
        assertEquals("", query)
    }
}
