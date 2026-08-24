package com.canvasstudio.ui.block.modules

import org.junit.Assert.assertEquals
import org.junit.Test

class ShinobiChartCalculatorTest {

    @Test
    fun `calcularNotas com valores zerados respeita limites minimos`() {
        val inputs = ShinobiInputs(
            taijutsu = 0f,
            ninjutsu = 0f,
            genjutsu = 0f,
            vigor = 0f,
            inteligencia = 0f,
            chakraMax = 6f
        )
        val notas = ShinobiChartCalculator.calcularNotas(inputs)

        assertEquals(0.5f, notas.ninjutsu, 0.01f)
        assertEquals(0.5f, notas.inteligencia, 0.01f)
        assertEquals(0.5f, notas.chakra, 0.01f)
        assertEquals(0.5f, notas.taijutsu, 0.01f)
        assertEquals(0.5f, notas.vigor, 0.01f)
        assertEquals(0.5f, notas.genjutsu, 0.01f)
        assertEquals("0.5", notas.formattedMedia())
    }

    @Test
    fun `calcularNotas aplica formulas originais e arredondamento correto`() {
        // NIN%: (50 / 10) + 0.5 = 5.5
        // INT+: 3 + 0.5 = 3.5
        // CHK+: (26 - 6) / 10 = 2.0
        // TAI%: (70 / 10) + 0.5 = 7.5
        // VIG+: 4 + 0.5 = 4.5
        // GEN%: (30 / 10) + 0.5 = 3.5
        val inputs = ShinobiInputs(
            ninjutsu = 50f,
            inteligencia = 3f,
            chakraMax = 26f,
            taijutsu = 70f,
            vigor = 4f,
            genjutsu = 30f
        )
        val notas = ShinobiChartCalculator.calcularNotas(inputs)

        assertEquals(5.5f, notas.ninjutsu, 0.01f)
        assertEquals(3.5f, notas.inteligencia, 0.01f)
        assertEquals(2.0f, notas.chakra, 0.01f)
        assertEquals(7.5f, notas.taijutsu, 0.01f)
        assertEquals(4.5f, notas.vigor, 0.01f)
        assertEquals(3.5f, notas.genjutsu, 0.01f)

        // Soma: 5.5 + 3.5 + 2.0 + 7.5 + 4.5 + 3.5 = 26.5 / 6 = 4.4166... -> 4.4
        assertEquals("4.4", notas.formattedMedia())
    }

    @Test
    fun `calcularNotas respeita teto maximo de 8_0`() {
        val inputs = ShinobiInputs(
            ninjutsu = 100f, // (100/10)+0.5 = 10.5 -> clamp 8.0
            inteligencia = 20f, // 20+0.5 = 20.5 -> clamp 8.0
            chakraMax = 100f,
            taijutsu = 100f,
            vigor = 20f,
            genjutsu = 100f
        )
        val notas = ShinobiChartCalculator.calcularNotas(inputs)

        assertEquals(8.0f, notas.ninjutsu, 0.01f)
        assertEquals(8.0f, notas.inteligencia, 0.01f)
        assertEquals(8.0f, notas.chakra, 0.01f)
        assertEquals(8.0f, notas.taijutsu, 0.01f)
        assertEquals(8.0f, notas.vigor, 0.01f)
        assertEquals(8.0f, notas.genjutsu, 0.01f)
        assertEquals("8.0", notas.formattedMedia())
    }
}
