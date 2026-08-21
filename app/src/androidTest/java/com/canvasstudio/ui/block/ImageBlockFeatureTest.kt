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
 * Bateria completa de testes de fluxo negocial ponta a ponta (E2E) para o Bloco de Imagem.
 */
@RunWith(AndroidJUnit4::class)
class ImageBlockFeatureTest {

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
    fun cenario01_fluxoCompletoCriarEConfigurarBlocoDeImagem() {
        launchCanvas()

        // 1. Cria o bloco de imagem via menu lateral
        canvasStudioRobot(composeTestRule) {
            openSidebar()
            addImageBlock()
            assertBlockWithTitle("Imagem")
            // 2. Abre a edição no Canvas
            clickEditBlock()
        }

        // 3. Altera o título e adiciona uma URL válida no modal
        editBlockDialogRobot(composeTestRule) {
            assertDialogTitle("Editar Bloco")
            enterTitle("Avatar do Personagem")
            enterImageUrl("https://example.com/naruto.png")
            clickSave()
        }

        // 4. Valida que o bloco atualizado aparece no Canvas
        canvasStudioRobot(composeTestRule) {
            assertBlockWithTitle("Avatar do Personagem")
        }
    }

    @Test
    fun cenario02_fluxoCompletoExclusaoDeBlocoDeImagem() {
        launchCanvas()

        canvasStudioRobot(composeTestRule) {
            openSidebar()
            addImageBlock()
            assertBlockWithTitle("Imagem")
            clickDeleteBlock()
            assertBlockDoesNotExist("Imagem")
        }
    }
}
