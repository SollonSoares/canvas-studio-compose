package com.canvasstudio.ui.block.modules

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

/**
 * Modelo de Entrada de Atributos Shinobi (Naruto RPG).
 */
data class ShinobiInputs(
    val taijutsu: Float = 0f,     // TAI% (0..100)
    val ninjutsu: Float = 0f,     // NIN% (0..100)
    val genjutsu: Float = 0f,     // GEN% (0..100)
    val vigor: Float = 0f,        // VIG+ (0..20)
    val inteligencia: Float = 0f, // INT+ (0..20)
    val chakraMax: Float = 6f     // CHK+ (default 6)
)

/**
 * Notas Finais Calculadas do Status Shinobi (intervalo de 0.5 a 8.0).
 */
data class ShinobiNotas(
    val ninjutsu: Float,
    val inteligencia: Float,
    val chakra: Float,
    val taijutsu: Float,
    val vigor: Float,
    val genjutsu: Float
) {
    /**
     * Ordem oficial dos 6 eixos (sentido horário começando no topo -90°):
     * 0: NIN (Ninjutsu)
     * 1: INT (Inteligência)
     * 2: CHK (Chakra)
     * 3: TAI (Taijutsu)
     * 4: VIG (Vigor)
     * 5: GEN (Genjutsu)
     */
    val asList: List<Float>
        get() = listOf(ninjutsu, inteligencia, chakra, taijutsu, vigor, genjutsu)

    val labels: List<String>
        get() = ShinobiChartCalculator.LABELS

    val media: Float
        get() = (ninjutsu + inteligencia + chakra + taijutsu + vigor + genjutsu) / 6f

    fun formattedMedia(): String {
        return String.format(Locale.US, "%.1f", media)
    }
}

/**
 * Calculadora Oficial de Status Shinobi.
 * Fórmulas originais:
 * - Taijutsu (TAI%): (valor / 10) + 0.5
 * - Ninjutsu (NIN%): (valor / 10) + 0.5
 * - Genjutsu (GEN%): (valor / 10) + 0.5
 * - Vigor (VIG+): valor + 0.5
 * - Inteligência (INT+): valor + 0.5
 * - Chakra Máximo (CHK+): (valor - 6) / 10
 *
 * Teto do Sistema: 8.0 | Intervalo das notas: [0.5 a 8.0] com arredondamento em passos de 0.5
 */
object ShinobiChartCalculator {

    const val TETO_SISTEMA: Float = 8.0f

    val NIVEIS_GUIA: List<Float> = listOf(2.0f, 4.0f, 6.0f, 8.0f)

    val LABELS: List<String> = listOf("NIN", "INT", "CHK", "TAI", "VIG", "GEN")

    fun parseInputs(json: JsonObject): ShinobiInputs {
        val nested = json["inputs"]?.jsonObject
        return ShinobiInputs(
            taijutsu = json["taijutsu"]?.jsonPrimitive?.floatOrNull
                ?: json["tai"]?.jsonPrimitive?.floatOrNull
                ?: json["TAI"]?.jsonPrimitive?.floatOrNull
                ?: nested?.get("taijutsu")?.jsonPrimitive?.floatOrNull
                ?: nested?.get("tai")?.jsonPrimitive?.floatOrNull
                ?: nested?.get("TAI")?.jsonPrimitive?.floatOrNull ?: 0f,
            ninjutsu = json["ninjutsu"]?.jsonPrimitive?.floatOrNull
                ?: json["nin"]?.jsonPrimitive?.floatOrNull
                ?: json["NIN"]?.jsonPrimitive?.floatOrNull
                ?: nested?.get("ninjutsu")?.jsonPrimitive?.floatOrNull
                ?: nested?.get("nin")?.jsonPrimitive?.floatOrNull
                ?: nested?.get("NIN")?.jsonPrimitive?.floatOrNull ?: 0f,
            genjutsu = json["genjutsu"]?.jsonPrimitive?.floatOrNull
                ?: json["gen"]?.jsonPrimitive?.floatOrNull
                ?: json["GEN"]?.jsonPrimitive?.floatOrNull
                ?: nested?.get("genjutsu")?.jsonPrimitive?.floatOrNull
                ?: nested?.get("gen")?.jsonPrimitive?.floatOrNull
                ?: nested?.get("GEN")?.jsonPrimitive?.floatOrNull ?: 0f,
            vigor = json["vigor"]?.jsonPrimitive?.floatOrNull
                ?: json["vig"]?.jsonPrimitive?.floatOrNull
                ?: json["VIG"]?.jsonPrimitive?.floatOrNull
                ?: nested?.get("vigor")?.jsonPrimitive?.floatOrNull
                ?: nested?.get("vig")?.jsonPrimitive?.floatOrNull
                ?: nested?.get("VIG")?.jsonPrimitive?.floatOrNull ?: 0f,
            inteligencia = json["inteligencia"]?.jsonPrimitive?.floatOrNull
                ?: json["int"]?.jsonPrimitive?.floatOrNull
                ?: json["INT"]?.jsonPrimitive?.floatOrNull
                ?: nested?.get("inteligencia")?.jsonPrimitive?.floatOrNull
                ?: nested?.get("int")?.jsonPrimitive?.floatOrNull
                ?: nested?.get("INT")?.jsonPrimitive?.floatOrNull ?: 0f,
            chakraMax = json["chakraMax"]?.jsonPrimitive?.floatOrNull
                ?: json["chakra"]?.jsonPrimitive?.floatOrNull
                ?: json["cha"]?.jsonPrimitive?.floatOrNull
                ?: json["CHK"]?.jsonPrimitive?.floatOrNull
                ?: nested?.get("chakraMax")?.jsonPrimitive?.floatOrNull
                ?: nested?.get("chakra")?.jsonPrimitive?.floatOrNull
                ?: nested?.get("cha")?.jsonPrimitive?.floatOrNull
                ?: nested?.get("CHK")?.jsonPrimitive?.floatOrNull ?: 6f
        )
    }

    fun ajustarNota(nota: Float): Float {
        val arredondado = round(nota * 2f) / 2f
        return max(0.5f, min(TETO_SISTEMA, arredondado))
    }

    fun calcularNotas(inputs: ShinobiInputs): ShinobiNotas {
        val tai = (inputs.taijutsu / 10f) + 0.5f
        val nin = (inputs.ninjutsu / 10f) + 0.5f
        val gen = (inputs.genjutsu / 10f) + 0.5f
        val vig = inputs.vigor + 0.5f
        val int = inputs.inteligencia + 0.5f
        val chk = (inputs.chakraMax - 6f) / 10f

        return ShinobiNotas(
            ninjutsu = ajustarNota(nin),
            inteligencia = ajustarNota(int),
            chakra = ajustarNota(chk),
            taijutsu = ajustarNota(tai),
            vigor = ajustarNota(vig),
            genjutsu = ajustarNota(gen)
        )
    }
}
