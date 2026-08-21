package com.canvasstudio.ui.block

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule

/**
 * Robot para automação e testes de fluxo completo de negócio do Canvas Studio (End-to-End).
 * Utiliza waitUntil e pausas configuráveis para máxima estabilidade no dispositivo físico.
 */
class CanvasStudioRobot(private val rule: ComposeContentTestRule) {

    private fun stepPause(ms: Long = 1200L) {
        Thread.sleep(ms)
    }

    fun openSidebar(): CanvasStudioRobot {
        rule.waitForIdle()
        rule.onNodeWithContentDescription("Menu").performClick()
        rule.waitForIdle()
        stepPause(1000L)
        return this
    }

    fun addTextBlock(): CanvasStudioRobot {
        rule.waitForIdle()
        rule.onNodeWithText("Novo Bloco de Texto").performScrollTo().performClick()
        rule.waitForIdle()
        stepPause(1500L)
        return this
    }

    fun addChartBlock(): CanvasStudioRobot {
        rule.waitForIdle()
        rule.onNodeWithText("Novo Gráfico Radar").performScrollTo().performClick()
        rule.waitForIdle()
        stepPause(1500L)
        return this
    }

    fun addImageBlock(): CanvasStudioRobot {
        rule.waitForIdle()
        rule.onNodeWithText("Inserir Imagem").performScrollTo().performClick()
        rule.waitForIdle()
        stepPause(1500L)
        return this
    }

    fun clickFabAdd(): CanvasStudioRobot {
        rule.waitForIdle()
        rule.onNodeWithContentDescription("Adicionar").performClick()
        rule.waitForIdle()
        stepPause(1500L)
        return this
    }

    fun clickEditBlock(index: Int = 0): CanvasStudioRobot {
        rule.waitForIdle()
        rule.waitUntil(5000) {
            rule.onAllNodes(hasContentDescription("Editar Bloco")).fetchSemanticsNodes().isNotEmpty()
        }
        rule.onAllNodes(hasContentDescription("Editar Bloco"))[index].performClick()
        rule.waitForIdle()
        stepPause(1200L)
        return this
    }

    fun clickDeleteBlock(index: Int = 0): CanvasStudioRobot {
        rule.waitForIdle()
        rule.waitUntil(5000) {
            rule.onAllNodes(hasContentDescription("Excluir Bloco")).fetchSemanticsNodes().isNotEmpty()
        }
        rule.onAllNodes(hasContentDescription("Excluir Bloco"))[index].performClick()
        rule.waitForIdle()
        stepPause(1200L)
        return this
    }

    fun searchBlocks(query: String): CanvasStudioRobot {
        rule.waitForIdle()
        rule.onNodeWithText("Buscar blocos...").performScrollTo().performTextInput(query)
        stepPause(800L)
        rule.onNodeWithContentDescription("Buscar").performClick()
        rule.waitForIdle()
        stepPause(1500L)
        return this
    }

    fun clearSearch(): CanvasStudioRobot {
        rule.waitForIdle()
        rule.onNodeWithContentDescription("Limpar Busca").performClick()
        rule.waitForIdle()
        stepPause(1000L)
        return this
    }

    fun exportJson(): CanvasStudioRobot {
        rule.waitForIdle()
        rule.onNodeWithText("JSON Export").performScrollTo().performClick()
        rule.waitForIdle()
        stepPause()
        return this
    }

    fun importJson(): CanvasStudioRobot {
        rule.waitForIdle()
        rule.onNodeWithText("JSON Import").performScrollTo().performClick()
        rule.waitForIdle()
        stepPause()
        return this
    }

    fun exportPdf(): CanvasStudioRobot {
        rule.waitForIdle()
        rule.onNodeWithText("Exportar PDF (Native)").performScrollTo().performClick()
        rule.waitForIdle()
        stepPause()
        return this
    }

    fun clearCanvas(): CanvasStudioRobot {
        rule.waitForIdle()
        rule.onNodeWithText("Limpar Tudo").performScrollTo().performClick()
        rule.waitForIdle()
        stepPause(1200L)
        return this
    }

    fun autoOrganize(): CanvasStudioRobot {
        rule.waitForIdle()
        rule.onNodeWithText("Auto Organizar").performScrollTo().performClick()
        rule.waitForIdle()
        stepPause(1200L)
        return this
    }

    fun openSettings(): CanvasStudioRobot {
        rule.waitForIdle()
        rule.onNodeWithText("Configurações").performScrollTo().performClick()
        rule.waitForIdle()
        stepPause()
        return this
    }

    fun toggleDarkMode(): CanvasStudioRobot {
        rule.waitForIdle()
        rule.onNodeWithText("MODO ESCURO").performScrollTo().performClick()
        rule.waitForIdle()
        stepPause()
        return this
    }

    fun assertBlockWithTitle(title: String): CanvasStudioRobot {
        rule.waitForIdle()
        rule.waitUntil(5000) {
            rule.onAllNodes(hasText(title)).fetchSemanticsNodes().isNotEmpty()
        }
        rule.onAllNodes(hasText(title)).onFirst().assertIsDisplayed()
        stepPause(800L)
        return this
    }

    fun assertBlockDoesNotExist(title: String): CanvasStudioRobot {
        rule.waitForIdle()
        rule.onNodeWithText(title).assertDoesNotExist()
        stepPause(600L)
        return this
    }

    fun assertContentText(text: String): CanvasStudioRobot {
        rule.waitForIdle()
        rule.waitUntil(5000) {
            rule.onAllNodes(hasText(text, substring = true)).fetchSemanticsNodes().isNotEmpty()
        }
        rule.onAllNodes(hasText(text, substring = true)).onFirst().assertIsDisplayed()
        stepPause(800L)
        return this
    }

    fun assertFilterBadge(text: String): CanvasStudioRobot {
        rule.waitForIdle()
        rule.waitUntil(5000) {
            rule.onAllNodes(hasText("Filtrando por: \"$text\"")).fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Filtrando por: \"$text\"").assertIsDisplayed()
        stepPause(800L)
        return this
    }
}

fun canvasStudioRobot(rule: ComposeContentTestRule, block: CanvasStudioRobot.() -> Unit): CanvasStudioRobot {
    return CanvasStudioRobot(rule).apply(block)
}
