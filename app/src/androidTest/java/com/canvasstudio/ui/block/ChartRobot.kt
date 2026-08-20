package com.canvasstudio.ui.block

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule

/**
 * Robot para encapsular interações e asserções do componente ChartBlock (Radar).
 * Inclui delays de execução para acompanhamento visual das etapas no dispositivo.
 */
class ChartRobot(private val rule: ComposeContentTestRule) {

    private fun stepPause(ms: Long = 1000L) {
        Thread.sleep(ms)
    }

    fun assertLabelValue(label: String, value: String): ChartRobot {
        rule.onNodeWithContentDescription("$label ($value)", substring = true).assertIsDisplayed()
        stepPause(600L)
        return this
    }

    fun assertAverage(expectedAverage: String): ChartRobot {
        rule.onNodeWithText("Média: ").assertIsDisplayed()
        rule.onNodeWithText(expectedAverage).assertIsDisplayed()
        stepPause(800L)
        return this
    }

    fun assertLabelExists(label: String): ChartRobot {
        rule.onNodeWithContentDescription(label, substring = true).assertIsDisplayed()
        stepPause(600L)
        return this
    }
}

fun chartRobot(rule: ComposeContentTestRule, block: ChartRobot.() -> Unit): ChartRobot {
    return ChartRobot(rule).apply(block)
}
