package com.canvasstudio.ui.block

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule

/**
 * Robot para encapsular ações e asserções no diálogo de edição de blocos (EditBlockDialog).
 * Utiliza performTextReplacement para inserção atômica e confiável de valores.
 */
class EditBlockDialogRobot(private val rule: ComposeContentTestRule) {

    private fun stepPause(ms: Long = 1200L) {
        Thread.sleep(ms)
    }

    fun assertDialogTitle(expectedTitle: String = "Editar Bloco"): EditBlockDialogRobot {
        rule.waitForIdle()
        rule.onNodeWithText(expectedTitle).assertIsDisplayed()
        stepPause(800L)
        return this
    }

    fun selectType(type: String): EditBlockDialogRobot {
        val formattedType = type.replaceFirstChar { it.uppercase() }
        rule.onNodeWithText(formattedType).performClick()
        rule.waitForIdle()
        stepPause()
        return this
    }

    fun enterTitle(newTitle: String): EditBlockDialogRobot {
        rule.onNodeWithContentDescription("Campo Título").performTextReplacement(newTitle)
        rule.waitForIdle()
        stepPause()
        return this
    }

    fun enterChartAttribute(label: String, value: String): EditBlockDialogRobot {
        rule.onNodeWithContentDescription("Campo $label").performTextReplacement(value)
        rule.waitForIdle()
        stepPause(800L)
        return this
    }

    fun clickIncrement(label: String): EditBlockDialogRobot {
        rule.onNodeWithContentDescription("Aumentar $label").performClick()
        rule.waitForIdle()
        stepPause(800L)
        return this
    }

    fun clickDecrement(label: String): EditBlockDialogRobot {
        rule.onNodeWithContentDescription("Diminuir $label").performClick()
        rule.waitForIdle()
        stepPause(800L)
        return this
    }

    fun enterTextContent(text: String): EditBlockDialogRobot {
        rule.onNodeWithContentDescription("Campo Conteúdo Texto").performTextReplacement(text)
        rule.waitForIdle()
        stepPause()
        return this
    }

    fun enterImageUrl(url: String): EditBlockDialogRobot {
        rule.onNodeWithContentDescription("Campo URL Imagem").performTextReplacement(url)
        rule.waitForIdle()
        stepPause()
        return this
    }

    fun clickSave(): EditBlockDialogRobot {
        rule.onNodeWithText("Salvar").performClick()
        rule.waitForIdle()
        stepPause(1500L)
        return this
    }

    fun clickCancel(): EditBlockDialogRobot {
        rule.onNodeWithText("Cancelar").performClick()
        rule.waitForIdle()
        stepPause(1000L)
        return this
    }
}

fun editBlockDialogRobot(rule: ComposeContentTestRule, block: EditBlockDialogRobot.() -> Unit): EditBlockDialogRobot {
    return EditBlockDialogRobot(rule).apply(block)
}
