package com.canvasstudio.ui.block

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.canvasstudio.ui.theme.CanvasStudioTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Testes de Fluxo Negocial Ponta a Ponta (E2E) para o Canvas Studio (Palco, Menus e Ações).
 */
@RunWith(AndroidJUnit4::class)
class CanvasWorkflowFeatureTest {

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
            val isDarkMode = false
            CanvasStudioTheme(darkTheme = isDarkMode) {
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
    fun cenario01_fluxoCompletoCriarEExcluirBlocoDeTexto() {
        launchCanvas()

        canvasStudioRobot(composeTestRule) {
            // 1. Abre a barra lateral
            openSidebar()
            // 2. Clica em Novo Bloco de Texto
            addTextBlock()
            // 3. Verifica que o bloco padrão foi inserido no Canvas
            assertBlockWithTitle("Novo Bloco")
            // 4. Clica no botão de excluir do bloco no Canvas
            clickDeleteBlock()
            // 5. Verifica que o bloco foi removido do Canvas
            assertBlockDoesNotExist("Novo Bloco")
        }
    }

    @Test
    fun cenario02_fluxoCompletoCriarBlocoRadarEOrganizar() {
        launchCanvas()

        canvasStudioRobot(composeTestRule) {
            // 1. Abre a barra lateral
            openSidebar()
            // 2. Clica em Novo Gráfico Radar
            addChartBlock()
            // 3. Valida presença do Radar no Canvas
            assertBlockWithTitle("Radar Chart")
            // 4. Abre o menu e executa Auto Organizar
            openSidebar()
            autoOrganize()
            // 5. Bloco continua presente e organizado
            assertBlockWithTitle("Radar Chart")
        }
    }

    @Test
    fun cenario03_fluxoCompletoCriacaoViaBotaoFlutuanteFab() {
        launchCanvas()

        canvasStudioRobot(composeTestRule) {
            // 1. Clica no FAB '+' de criação rápida no Canvas
            clickFabAdd()
            // 2. Valida que o bloco foi adicionado ao palco
            assertBlockWithTitle("Novo Bloco")
        }
    }

    @Test
    fun cenario04_fluxoCompletoBuscaEFiltragemEmTempoReal() {
        launchCanvas()

        canvasStudioRobot(composeTestRule) {
            // 1. Cria um bloco de texto e um gráfico radar
            openSidebar()
            addTextBlock()
            openSidebar()
            addChartBlock()

            // 2. Abre a busca e pesquisa por "Radar"
            openSidebar()
            searchBlocks("Radar")

            // 3. Verifica indicador de filtro ativo e isolamento do bloco
            assertFilterBadge("Radar")
            assertBlockWithTitle("Radar Chart")
        }
    }

    @Test
    fun cenario05_fluxoCompletoLimparCanvas() {
        launchCanvas()

        canvasStudioRobot(composeTestRule) {
            // 1. Adiciona múltiplos blocos
            openSidebar()
            addTextBlock()
            openSidebar()
            addChartBlock()

            // 2. Abre o menu e aciona Limpar Tudo
            openSidebar()
            clearCanvas()

            // 3. Valida que nenhum bloco permanece no Canvas
            assertBlockDoesNotExist("Novo Bloco")
            assertBlockDoesNotExist("Radar Chart")
        }
    }
}
