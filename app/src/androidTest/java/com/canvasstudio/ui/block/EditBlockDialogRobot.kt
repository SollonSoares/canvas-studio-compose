package com.canvasstudio.ui.block

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule

/**
 * Robot para encapsular ações e asserções no diálogo de edição de blocos (EditBlockDialog).
 */
class EditBlockDialogRobot(private val rule: ComposeContentTestRule) {

    fun assertDialogTitle(expectedTitle: String = "Editar Bloco") {
        rule.onNodeWithText(expectedTitle).assertIsDisplayed()
    }

    fun selectType(type: String) {
        val formattedType = type.replaceFirstChar { it.uppercase() }
        rule.onNodeWithText(formattedType).performClick()
    }

    fun enterTitle(newTitle: String) {
        rule.onNodeWithText("Título").assertIsDisplayed()
        // Localiza campo de texto abaixo do label Título
        rule.onAllNodes(hasSetTextAction())[0].performTextClearance()
        rule.onAllNodes(hasSetTextAction())[0].performTextInput(newTitle)
    }

    fun enterChartAttribute(label: String, value: String) {
        rule.onNodeWithText(label).assertIsDisplayed()
        // Localiza os campos de texto do modal
        rule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(value)
    }

    fun clickIncrement(label: String) {
        rule.onNodeWithContentDescription("Aumentar").performClick()
    }

    fun clickDecrement(label: String) {
        rule.onNodeWithContentDescription("Diminuir").performClick()
    }

    fun enterTextContent(text: String) {
        rule.onNodeWithText("Use **negrito** ou *itálico*").performTextInput(text)
    }

    fun enterImageUrl(url: String) {
        rule.onNodeWithText("URL da imagem").performTextInput(url)
    }

    fun clickSave() {
        rule.onNodeWithText("Salvar").performClick()
    }

    fun clickCancel() {
        rule.onNodeWithText("Cancelar").performClick()
    }
}

fun editBlockDialogRobot(rule: ComposeContentTestRule, block: EditBlockDialogRobot.() -> Unit): EditBlockDialogRobot {
    return EditBlockDialogRobot(rule).apply(block)
}
