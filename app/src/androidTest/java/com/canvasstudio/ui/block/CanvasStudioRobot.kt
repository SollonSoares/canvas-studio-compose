package com.canvasstudio.ui.block

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule

/**
 * Robot para automação e testes de fluxo completo do Canvas Studio.
 */
class CanvasStudioRobot(private val rule: ComposeContentTestRule) {

    fun openSidebar() {
        rule.onNodeWithContentDescription("Menu").performClick()
    }

    fun addTextBlock() {
        rule.onNodeWithText("Novo Bloco de Texto").performClick()
    }

    fun addChartBlock() {
        rule.onNodeWithText("Novo Gráfico Radar").performClick()
    }

    fun addImageBlock() {
        rule.onNodeWithText("Inserir Imagem").performClick()
    }

    fun loadDefaultTemplate() {
        rule.onNodeWithText("Carregar Ficha RPG (Padrão)").performClick()
    }

    fun openSettings() {
        rule.onNodeWithText("Configurações").performClick()
    }

    fun toggleDarkMode() {
        rule.onNodeWithContentDescription("Tema").performClick()
    }

    fun assertBlockWithTitle(title: String) {
        rule.onNodeWithText(title).assertIsDisplayed()
    }

    fun assertBrandTitle(title: String) {
        rule.onNodeWithText(title).assertIsDisplayed()
    }

    fun searchBlocks(query: String) {
        rule.onNodeWithText("Buscar blocos...").performTextInput(query)
    }
}

fun canvasStudioRobot(rule: ComposeContentTestRule, block: CanvasStudioRobot.() -> Unit): CanvasStudioRobot {
    return CanvasStudioRobot(rule).apply(block)
}
