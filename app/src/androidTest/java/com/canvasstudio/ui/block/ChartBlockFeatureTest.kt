package com.canvasstudio.ui.block

import androidx.compose.runtime.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.canvasstudio.data.local.entity.BlockEntity
import com.canvasstudio.ui.block.dialogs.EditBlockDialog
import com.canvasstudio.ui.block.modules.ChartBlock
import com.canvasstudio.ui.theme.CanvasColors
import androidx.compose.ui.graphics.Color
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Bateria completa de testes funcionais e não funcionais do ChartBlock (Radar).
 */
@RunWith(AndroidJUnit4::class)
class ChartBlockFeatureTest {

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
    fun cenario01_renderizacaoBasicaValoresPadrao() {
        val block = BlockEntity(
            id = 1,
            projectId = 1,
            title = "Status Shinobi",
            type = "chart",
            posX = 0f,
            posY = 0f,
            width = 300,
            height = 300,
            contentJson = """{
                "ninjutsu": 5,
                "inteligencia": 5,
                "chakra": 5,
                "taijutsu": 5,
                "vigor": 5,
                "genjutsu": 5
            }"""
        )

        composeTestRule.setContent {
            ChartBlock(block = block, colors = testColors)
        }

        chartRobot(composeTestRule) {
            assertLabelValue("NIN", "5")
            assertLabelValue("INT", "5")
            assertLabelValue("CHK", "5")
            assertLabelValue("TAI", "5")
            assertLabelValue("VIG", "5")
            assertLabelValue("GEN", "5")
            assertAverage("5")
        }
    }

    @Test
    fun cenario02_valoresCustomizadosSemTetoMaximo() {
        val block = BlockEntity(
            id = 2,
            projectId = 1,
            title = "Status Lendário",
            type = "chart",
            posX = 0f,
            posY = 0f,
            width = 300,
            height = 300,
            contentJson = """{
                "ninjutsu": 25,
                "inteligencia": 30,
                "chakra": 100,
                "taijutsu": 15,
                "vigor": 50,
                "genjutsu": 20
            }"""
        )

        composeTestRule.setContent {
            ChartBlock(block = block, colors = testColors)
        }

        chartRobot(composeTestRule) {
            assertLabelValue("NIN", "25")
            assertLabelValue("INT", "30")
            assertLabelValue("CHK", "100")
            assertLabelValue("TAI", "15")
            assertLabelValue("VIG", "50")
            assertLabelValue("GEN", "20")
            assertAverage("40")
        }
    }

    @Test
    fun cenario03_edicaoDeAtributosNoModal() {
        var currentBlock by mutableStateOf(
            BlockEntity(
                id = 3,
                projectId = 1,
                title = "Status Editável",
                type = "chart",
                posX = 0f,
                posY = 0f,
                width = 300,
                height = 300,
                contentJson = """{"ninjutsu": 4, "inteligencia": 4, "chakra": 4, "taijutsu": 4, "vigor": 4, "genjutsu": 4}"""
            )
        )

        composeTestRule.setContent {
            EditBlockDialog(
                block = currentBlock,
                onDismiss = {},
                onConfirm = { currentBlock = it },
                onLiveUpdate = { currentBlock = it },
                colors = testColors
            )
        }

        editBlockDialogRobot(composeTestRule) {
            assertDialogTitle("Editar Bloco")
            clickSave()
        }
    }

    // =========================================================================
    // CENÁRIOS NÃO FUNCIONAIS & RESILIÊNCIA
    // =========================================================================

    @Test
    fun cenario04_resilienciaJsonInvalidoOuVazio() {
        val blockCorrompido = BlockEntity(
            id = 4,
            projectId = 1,
            title = "Status Inválido",
            type = "chart",
            posX = 0f,
            posY = 0f,
            width = 300,
            height = 300,
            contentJson = "INVALID_JSON_CONTENT_NOT_FORMATTED"
        )

        composeTestRule.setContent {
            ChartBlock(block = blockCorrompido, colors = testColors)
        }

        chartRobot(composeTestRule) {
            assertLabelValue("NIN", "0")
            assertLabelValue("INT", "0")
            assertAverage("0")
        }
    }

    @Test
    fun cenario05_valoresDecimaisEZero() {
        val blockDecimais = BlockEntity(
            id = 5,
            projectId = 1,
            title = "Status Decimais",
            type = "chart",
            posX = 0f,
            posY = 0f,
            width = 300,
            height = 300,
            contentJson = """{
                "ninjutsu": 7.5,
                "inteligencia": 0,
                "chakra": 12.3,
                "taijutsu": 8.0,
                "vigor": 0,
                "genjutsu": 3.2
            }"""
        )

        composeTestRule.setContent {
            ChartBlock(block = blockDecimais, colors = testColors)
        }

        chartRobot(composeTestRule) {
            assertLabelValue("NIN", "7.5")
            assertLabelValue("INT", "0")
            assertLabelValue("TAI", "8")
        }
    }
}
