package com.canvasstudio.ui.block

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule

/**
 * Robot para automação e testes de fluxo completo de negócio do Canvas Studio (End-to-End).
 * Inclui pausas configuráveis para visualização no dispositivo móvel.
 */
class CanvasStudioRobot(private val rule: ComposeContentTestRule) {

    private fun stepPause(ms: Long = 1200L) {
        Thread.sleep(ms)
    }

    fun openSidebar(): CanvasStudioRobot {
        rule.onNodeWithContentDescription("Menu").performClick()
        stepPause()
        return this
    }

    fun addTextBlock(): CanvasStudioRobot {
        rule.onNodeWithText("Novo Bloco de Texto").performClick()
        stepPause(1500L)
        return this
    }

    fun addChartBlock(): CanvasStudioRobot {
        rule.onNodeWithText("Novo Gráfico Radar").performClick()
        stepPause(1500L)
        return this
    }

    fun addImageBlock(): CanvasStudioRobot {
        rule.onNodeWithText("Inserir Imagem").performClick()
        stepPause(1500L)
        return this
    }

    fun clickFabAdd(): CanvasStudioRobot {
        rule.onNodeWithContentDescription("Adicionar").performClick()
        stepPause(1500L)
        return this
    }

    fun clickEditBlock(index: Int = 0): CanvasStudioRobot {
        rule.onAllNodes(hasContentDescription("Editar Bloco"))[index].performClick()
        stepPause(1200L)
        return this
    }

    fun clickDeleteBlock(index: Int = 0): CanvasStudioRobot {
        rule.onAllNodes(hasContentDescription("Excluir Bloco"))[index].performClick()
        stepPause(1200L)
        return this
    }

    fun searchBlocks(query: String): CanvasStudioRobot {
        rule.onNodeWithText("Buscar blocos...").performTextInput(query)
        stepPause(1500L)
        return this
    }

    fun clearSearch(): CanvasStudioRobot {
        rule.onNodeWithContentDescription("Limpar Busca").performClick()
        stepPause(1000L)
        return this
    }

    fun exportJson(): CanvasStudioRobot {
        rule.onNodeWithText("JSON Export").performClick()
        stepPause()
        return this
    }

    fun importJson(): CanvasStudioRobot {
        rule.onNodeWithText("JSON Import").performClick()
        stepPause()
        return this
    }

    fun exportPdf(): CanvasStudioRobot {
        rule.onNodeWithText("Exportar PDF (Native)").performClick()
        stepPause()
        return this
    }

    fun clearCanvas(): CanvasStudioRobot {
        rule.onNodeWithText("Limpar Tudo").performClick()
        stepPause(1200L)
        return this
    }

    fun autoOrganize(): CanvasStudioRobot {
        rule.onNodeWithText("Auto Organizar").performClick()
        stepPause(1200L)
        return this
    }

    fun openSettings(): CanvasStudioRobot {
        rule.onNodeWithText("Configurações").performClick()
        stepPause()
        return this
    }

    fun toggleDarkMode(): CanvasStudioRobot {
        rule.onNodeWithText("MODO ESCURO").performClick()
        stepPause()
        return this
    }

    fun assertBlockWithTitle(title: String): CanvasStudioRobot {
        rule.onNodeWithText(title).assertIsDisplayed()
        stepPause(800L)
        return this
    }

    fun assertBlockDoesNotExist(title: String): CanvasStudioRobot {
        rule.onNodeWithText(title).assertDoesNotExist()
        stepPause(600L)
        return this
    }

    fun assertContentText(text: String): CanvasStudioRobot {
        rule.onNodeWithText(text, substring = true).assertIsDisplayed()
        stepPause(800L)
        return this
    }

    fun assertFilterBadge(text: String): CanvasStudioRobot {
        rule.onNodeWithText("Filtrando por: \"$text\"").assertIsDisplayed()
        stepPause(800L)
        return this
    }
}

fun canvasStudioRobot(rule: ComposeContentTestRule, block: CanvasStudioRobot.() -> Unit): CanvasStudioRobot {
    return CanvasStudioRobot(rule).apply(block)
}
