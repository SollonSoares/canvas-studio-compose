package com.canvasstudio.ui.block

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule

/**
 * Robot para encapsular ações e asserções no diálogo de edição de blocos (EditBlockDialog).
 * Inclui pausas de execução para acompanhamento visual das etapas no dispositivo.
 */
class EditBlockDialogRobot(private val rule: ComposeContentTestRule) {

    private fun stepPause(ms: Long = 1200L) {
        Thread.sleep(ms)
    }

    fun assertDialogTitle(expectedTitle: String = "Editar Bloco"): EditBlockDialogRobot {
        rule.onNodeWithText(expectedTitle).assertIsDisplayed()
        stepPause(800L)
        return this
    }

    fun selectType(type: String): EditBlockDialogRobot {
        val formattedType = type.replaceFirstChar { it.uppercase() }
        rule.onNodeWithText(formattedType).performClick()
        stepPause()
        return this
    }

    fun enterTitle(newTitle: String): EditBlockDialogRobot {
        rule.onNodeWithText("Título").assertIsDisplayed()
        rule.onAllNodes(hasSetTextAction())[0].performTextClearance()
        stepPause(400L)
        rule.onAllNodes(hasSetTextAction())[0].performTextInput(newTitle)
        stepPause()
        return this
    }

    fun enterChartAttribute(label: String, value: String): EditBlockDialogRobot {
        rule.onNodeWithContentDescription("Campo $label").performTextClearance()
        stepPause(300L)
        rule.onNodeWithContentDescription("Campo $label").performTextInput(value)
        stepPause(800L)
        return this
    }

    fun clickIncrement(label: String): EditBlockDialogRobot {
        rule.onNodeWithContentDescription("Aumentar $label").performClick()
        stepPause(800L)
        return this
    }

    fun clickDecrement(label: String): EditBlockDialogRobot {
        rule.onNodeWithContentDescription("Diminuir $label").performClick()
        stepPause(800L)
        return this
    }

    fun enterTextContent(text: String): EditBlockDialogRobot {
        rule.onAllNodes(hasSetTextAction())[1].performTextClearance()
        stepPause(300L)
        rule.onAllNodes(hasSetTextAction())[1].performTextInput(text)
        stepPause()
        return this
    }

    fun enterImageUrl(url: String): EditBlockDialogRobot {
        rule.onAllNodes(hasSetTextAction())[1].performTextClearance()
        stepPause(300L)
        rule.onAllNodes(hasSetTextAction())[1].performTextInput(url)
        stepPause()
        return this
    }

    fun clickSave(): EditBlockDialogRobot {
        rule.onNodeWithText("Salvar").performClick()
        stepPause(1500L)
        return this
    }

    fun clickCancel(): EditBlockDialogRobot {
        rule.onNodeWithText("Cancelar").performClick()
        stepPause(1000L)
        return this
    }
}

fun editBlockDialogRobot(rule: ComposeContentTestRule, block: EditBlockDialogRobot.() -> Unit): EditBlockDialogRobot {
    return EditBlockDialogRobot(rule).apply(block)
}
