package com.canvasstudio.ui.block

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.canvasstudio.data.local.entity.BlockEntity
import com.canvasstudio.ui.theme.CanvasStudioTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Bateria completa de testes de fluxo negocial ponta a ponta (E2E) do Gráfico Radar.
 */
@RunWith(AndroidJUnit4::class)
class ChartBlockFeatureTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var repository: InMemoryBlockRepository
    private lateinit var preferences: InMemoryPreferencesManager
    private lateinit var viewModel: BlockViewModel

    @Before
    fun setup() {
        repository = InMemoryBlockRepository()
        preferences = InMemoryPreferencesManager()
        viewModel = BlockViewModel(repository, preferences)
    }

    private fun launchCanvas() {
        composeTestRule.setContent {
            CanvasStudioTheme(darkTheme = false) {
                val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
                BlockScreen(
                    uiState = uiState,
                    viewModel = viewModel,
                    onBack = {}
                )
            }
        }
        Thread.sleep(1500L)
    }

    // =========================================================================
    // CENÁRIOS DE FLUXO NEGOCIAL COMPLETO
    // =========================================================================

    @Test
    fun cenario01_fluxoCompletoCriacaoEVisualizacaoRadarPadrao() {
        launchCanvas()

        // 1. Cria o radar pelo fluxo padrão da UI
        canvasStudioRobot(composeTestRule) {
            openSidebar()
            addChartBlock()
            assertBlockWithTitle("Radar Chart")
        }

        // 2. Valida os valores e a média inicial (5) do gráfico no Canvas
        chartRobot(composeTestRule) {
            assertLabelValue("NIN", "5")
            assertLabelValue("INT", "5")
            assertLabelValue("CHK", "5")
            assertAverage("5")
        }
    }

    @Test
    fun cenario02_fluxoCompletoEdicaoValoresCustomizadosSemTetoMaximo() {
        launchCanvas()

        // 1. Cria o bloco de radar
        canvasStudioRobot(composeTestRule) {
            openSidebar()
            addChartBlock()
            clickEditBlock()
        }

        // 2. Edita os atributos com valores superiores a 10 (ex: 25, 100, 50)
        editBlockDialogRobot(composeTestRule) {
            assertDialogTitle("Editar Bloco")
            enterTitle("Status Lendário")
            enterChartAttribute("Ninjutsu", "25")
            enterChartAttribute("Chakra", "100")
            enterChartAttribute("Vigor", "50")
            clickSave()
        }

        // 3. Valida no Canvas que o título foi atualizado e os valores e escala dinâmica estão corretos
        canvasStudioRobot(composeTestRule) {
            assertBlockWithTitle("Status Lendário")
        }

        chartRobot(composeTestRule) {
            assertLabelValue("NIN", "25")
            assertLabelValue("CHK", "100")
            assertLabelValue("VIG", "50")
        }
    }

    @Test
    fun cenario03_fluxoCompletoBotoesIncrementoEDecremento() {
        launchCanvas()

        canvasStudioRobot(composeTestRule) {
            openSidebar()
            addChartBlock()
            clickEditBlock()
        }

        editBlockDialogRobot(composeTestRule) {
            assertDialogTitle("Editar Bloco")
            enterTitle("Status Treinado")
            clickIncrement("Ninjutsu")
            clickDecrement("Taijutsu")
            clickSave()
        }

        canvasStudioRobot(composeTestRule) {
            assertBlockWithTitle("Status Treinado")
        }

        chartRobot(composeTestRule) {
            assertLabelValue("NIN", "6")
            assertLabelValue("TAI", "4")
        }
    }

    @Test
    fun cenario04_resilienciaBlocoCorrompidoCarregadoNoCanvas() {
        // Insere um bloco previamente corrompido no repositório
        val corrompido = BlockEntity(
            id = 99,
            projectId = 0,
            title = "Status Inválido",
            type = "chart",
            posX = 50f,
            posY = 50f,
            width = 300,
            height = 300,
            contentJson = "INVALID_CORRUPTED_JSON_DATA"
        )
        kotlinx.coroutines.runBlocking { repository.insertBlock(corrompido) }

        launchCanvas()

        // Canvas deve ser resiliente e renderizar com valores zerados sem quebrar o app
        canvasStudioRobot(composeTestRule) {
            assertBlockWithTitle("Status Inválido")
        }

        chartRobot(composeTestRule) {
            assertLabelValue("NIN", "0")
            assertAverage("0")
        }
    }
}
