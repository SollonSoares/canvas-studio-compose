package com.canvasstudio.ui.block

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule

/**
 * Robot para encapsular interações e asserções do componente ChartBlock (Radar).
 */
class ChartRobot(private val rule: ComposeContentTestRule) {

    fun assertLabelValue(label: String, value: String) {
        rule.onNodeWithContentDescription("$label ($value)", substring = true).assertIsDisplayed()
    }

    fun assertAverage(expectedAverage: String) {
        rule.onNodeWithText("Média: ").assertIsDisplayed()
        rule.onNodeWithText(expectedAverage).assertIsDisplayed()
    }

    fun assertLabelExists(label: String) {
        rule.onNodeWithContentDescription(label, substring = true).assertIsDisplayed()
    }
}

fun chartRobot(rule: ComposeContentTestRule, block: ChartRobot.() -> Unit): ChartRobot {
    return ChartRobot(rule).apply(block)
}
