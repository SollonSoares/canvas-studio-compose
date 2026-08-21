package com.canvasstudio.ui.block.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.canvasstudio.designsystem.components.CanvasToggle
import com.canvasstudio.domain.model.CanvasConfig
import com.canvasstudio.ui.theme.CanvasColors

@Composable
fun SettingsTogglesSection(
    modules: Map<String, Boolean>,
    onToggleModule: (String, Boolean) -> Unit,
    config: CanvasConfig,
    onToggleAutoOcr: (Boolean) -> Unit,
    onToggleFinancialBadges: (Boolean) -> Unit,
    onTogglePartyDetails: (Boolean) -> Unit,
    onToggleFitAspectRatio: (Boolean) -> Unit,
    colors: CanvasColors
) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
        TextSectionHeader("MÓDULOS & PLUGINS DO SISTEMA", colors)
        CanvasToggle(label = "Módulo de Texto & Markdown", checked = modules["text"] ?: true, onCheckedChange = { onToggleModule("text", it) })
        CanvasToggle(label = "Módulo de Imagens & Mídia", checked = modules["image"] ?: true, onCheckedChange = { onToggleModule("image", it) })
        CanvasToggle(label = "Módulo de Charts Radar", checked = modules["chart"] ?: true, onCheckedChange = { onToggleModule("chart", it) })

        Spacer(Modifier.height(12.dp))
        TextSectionHeader("INTELIGÊNCIA & COMPROVANTES", colors)
        CanvasToggle(label = "Auto OCR ao importar mídias", checked = config.autoOcrEnabled, onCheckedChange = onToggleAutoOcr)
        CanvasToggle(label = "Badges Financeiros (PIX / Valor)", checked = config.showFinancialBadges, onCheckedChange = onToggleFinancialBadges)
        CanvasToggle(label = "Exibir De / Para / Banco no Bloco", checked = config.showPartyDetails, onCheckedChange = onTogglePartyDetails)
        CanvasToggle(label = "Ajustar proporção original da imagem", checked = config.fitOriginalAspectRatio, onCheckedChange = onToggleFitAspectRatio)
    }
}
