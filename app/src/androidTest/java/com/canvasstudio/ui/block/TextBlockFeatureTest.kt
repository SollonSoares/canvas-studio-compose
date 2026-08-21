package com.canvasstudio.ui.block

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.canvasstudio.ui.block.utils.parseRichText
import com.canvasstudio.ui.theme.CanvasStudioTheme
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Bateria de testes de fluxo negocial completo para Blocos de Texto e Markdown.
 */
@RunWith(AndroidJUnit4::class)
class TextBlockFeatureTest {

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
    fun cenario01_fluxoCompletoCriarEditarEFormatarBlocoDeTexto() {
        launchCanvas()

        // 1. Cria um novo bloco de texto no Canvas
        canvasStudioRobot(composeTestRule) {
            openSidebar()
            addTextBlock()
            assertBlockWithTitle("Novo Bloco")
            // 2. Clica no botão de editar no bloco do Canvas
            clickEditBlock()
        }

        // 3. Modifica o título e texto no modal de edição
        editBlockDialogRobot(composeTestRule) {
            assertDialogTitle("Editar Bloco")
            enterTitle("Anotações da Missão")
            enterTextContent("**Objetivo:** Proteger a vila de Konoha.")
            clickSave()
        }

        // 4. Valida que o Canvas exibe o título atualizado e o texto formatado
        canvasStudioRobot(composeTestRule) {
            assertBlockWithTitle("Anotações da Missão")
            assertContentText("Objetivo: Proteger a vila de Konoha.")
        }
    }

    @Test
    fun cenario02_validacaoDoParserMarkdownNegritoEItalico() {
        val richText = parseRichText("Texto com **Negrito** e *Itálico*")
        
        assertEquals("Texto com Negrito e Itálico", richText.text)
        
        val boldSpan = richText.spanStyles.find { it.item.fontWeight == FontWeight.Bold }
        val italicSpan = richText.spanStyles.find { it.item.fontStyle == FontStyle.Italic }
        
        assert(boldSpan != null) { "Estilo de negrito deve ser aplicado" }
        assert(italicSpan != null) { "Estilo de itálico deve ser aplicado" }
    }

    @Test
    fun cenario03_fluxoNegocialCancelamentoDaEdicao() {
        launchCanvas()

        canvasStudioRobot(composeTestRule) {
            openSidebar()
            addTextBlock()
            assertBlockWithTitle("Novo Bloco")
            clickEditBlock()
        }

        editBlockDialogRobot(composeTestRule) {
            assertDialogTitle("Editar Bloco")
            enterTitle("Título Descartado")
            clickCancel()
        }

        // Bloco deve manter o título original sem alterações
        canvasStudioRobot(composeTestRule) {
            assertBlockWithTitle("Novo Bloco")
            assertBlockDoesNotExist("Título Descartado")
        }
    }
}
