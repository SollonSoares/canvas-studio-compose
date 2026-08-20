package com.canvasstudio.ui.block

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.canvasstudio.data.local.entity.BlockEntity
import com.canvasstudio.ui.block.components.SidebarContent
import com.canvasstudio.ui.theme.CanvasColors
import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Testes de fluxos funcionais e não funcionais do Canvas Studio (Workflow e Sidebar).
 */
@RunWith(AndroidJUnit4::class)
class CanvasWorkflowFeatureTest {

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
    fun cenario01_adicaoDeBlocoTextoViaSidebar() {
        var textBlockAdded = false

        composeTestRule.setContent {
            SidebarContent(
                q = "",
                onQ = {},
                onSearch = {},
                onImp = {},
                onExp = {},
                onClr = {},
                onGen = {},
                onOrg = {},
                modules = mapOf("text" to true, "image" to true, "chart" to true),
                onToggleModule = { _, _ -> },
                isDarkMode = false,
                onToggleTheme = {},
                isGridEnabled = true,
                onToggleGrid = {},
                isLocked = false,
                onToggleLock = {},
                onAddTextBlock = { textBlockAdded = true },
                onAddChartBlock = {},
                onAddImageBlock = {},
                onShowSettings = {},
                onExportPdf = {},
                onLoadTemplate = {},
                colors = testColors
            )
        }

        canvasStudioRobot(composeTestRule) {
            addTextBlock()
        }

        assertTrue("Callback onAddTextBlock deve ser acionado", textBlockAdded)
    }

    @Test
    fun cenario02_adicaoDeBlocoGraficoRadarViaSidebar() {
        var chartBlockAdded = false

        composeTestRule.setContent {
            SidebarContent(
                q = "",
                onQ = {},
                onSearch = {},
                onImp = {},
                onExp = {},
                onClr = {},
                onGen = {},
                onOrg = {},
                modules = mapOf("text" to true, "image" to true, "chart" to true),
                onToggleModule = { _, _ -> },
                isDarkMode = false,
                onToggleTheme = {},
                isGridEnabled = true,
                onToggleGrid = {},
                isLocked = false,
                onToggleLock = {},
                onAddTextBlock = {},
                onAddChartBlock = { chartBlockAdded = true },
                onAddImageBlock = {},
                onShowSettings = {},
                onExportPdf = {},
                onLoadTemplate = {},
                colors = testColors
            )
        }

        canvasStudioRobot(composeTestRule) {
            addChartBlock()
        }

        assertTrue("Callback onAddChartBlock deve ser acionado", chartBlockAdded)
    }

    @Test
    fun cenario03_carregamentoDeTemplatePadrao() {
        var templateTriggered = false

        composeTestRule.setContent {
            SidebarContent(
                q = "",
                onQ = {},
                onSearch = {},
                onImp = {},
                onExp = {},
                onClr = {},
                onGen = {},
                onOrg = {},
                modules = mapOf("text" to true, "image" to true, "chart" to true),
                onToggleModule = { _, _ -> },
                isDarkMode = false,
                onToggleTheme = {},
                isGridEnabled = true,
                onToggleGrid = {},
                isLocked = false,
                onToggleLock = {},
                onAddTextBlock = {},
                onAddChartBlock = {},
                onAddImageBlock = {},
                onShowSettings = {},
                onExportPdf = {},
                onLoadTemplate = { templateTriggered = true },
                colors = testColors
            )
        }

        canvasStudioRobot(composeTestRule) {
            loadDefaultTemplate()
        }

        assertTrue("Callback onLoadTemplate deve ser disparado", templateTriggered)
    }

    // =========================================================================
    // CENÁRIOS NÃO FUNCIONAIS
    // =========================================================================

    @Test
    fun cenario04_buscaEFiltragemDeBlocos() {
        var queryCaptured = ""

        composeTestRule.setContent {
            SidebarContent(
                q = queryCaptured,
                onQ = { queryCaptured = it },
                onSearch = {},
                onImp = {},
                onExp = {},
                onClr = {},
                onGen = {},
                onOrg = {},
                modules = mapOf("text" to true, "image" to true, "chart" to true),
                onToggleModule = { _, _ -> },
                isDarkMode = false,
                onToggleTheme = {},
                isGridEnabled = true,
                onToggleGrid = {},
                isLocked = false,
                onToggleLock = {},
                onAddTextBlock = {},
                onAddChartBlock = {},
                onAddImageBlock = {},
                onShowSettings = {},
                onExportPdf = {},
                onLoadTemplate = {},
                colors = testColors
            )
        }

        canvasStudioRobot(composeTestRule) {
            searchBlocks("Ninjutsu")
        }

        assertEquals("Ninjutsu", queryCaptured)
    }
}
