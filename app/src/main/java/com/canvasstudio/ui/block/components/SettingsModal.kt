package com.canvasstudio.ui.block.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasstudio.designsystem.CanvasTheme
import com.canvasstudio.designsystem.components.*
import com.canvasstudio.designsystem.tokens.CanvasDimens
import com.canvasstudio.domain.model.CanvasConfig
import com.canvasstudio.ui.theme.CanvasColors

@Composable
fun SettingsModal(
    title: String, 
    onTitleChange: (String) -> Unit, 
    canvasDimensions: Pair<Int, Int>,
    onDimensionsChange: (Int, Int) -> Unit,
    modules: Map<String, Boolean>, 
    onToggleModule: (String, Boolean) -> Unit, 
    config: CanvasConfig = CanvasConfig(),
    onToggleAutoOcr: (Boolean) -> Unit = {},
    onToggleFinancialBadges: (Boolean) -> Unit = {},
    onTogglePartyDetails: (Boolean) -> Unit = {},
    onToggleFitAspectRatio: (Boolean) -> Unit = {},
    onDismiss: () -> Unit, 
    colors: CanvasColors = CanvasTheme.colors
) {
    val presets = listOf(
        Triple("Square (Std)", 2000, 2000),
        Triple("Full HD", 1920, 1080),
        Triple("4K Ultra", 3840, 2160),
        Triple("Mobile", 1080, 1920)
    )

    CanvasModal(
        title = "Configurações do Canvas",
        onDismiss = onDismiss,
        confirmButton = {
            CanvasButton(
                onClick = onDismiss,
                variant = CanvasButtonVariant.Primary
            ) {
                Text("Concluir", fontWeight = FontWeight.Bold)
            }
        }
    ) {
        // 1. Título do Projeto
        CanvasSectionHeader("Título do Projeto")
        Spacer(Modifier.height(CanvasDimens.spaceXs))
        CanvasTextField(
            value = title,
            onValueChange = onTitleChange,
            placeholder = "Nome do projeto..."
        )
        
        CanvasDivider()

        // 2. Dimensões do Canvas
        CanvasSectionHeader("Dimensões do Canvas")
        Spacer(Modifier.height(CanvasDimens.spaceXs))
        
        presets.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CanvasDimens.spaceSm)
            ) {
                row.forEach { (name, w, h) ->
                    val isSelected = canvasDimensions.first == w && canvasDimensions.second == h
                    OutlinedButton(
                        onClick = { onDimensionsChange(w, h) },
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, if (isSelected) colors.accent else colors.borderSubtle),
                        shape = CanvasDimens.shapeMd,
                        colors = ButtonDefaults.outlinedButtonColors(
                            backgroundColor = if (isSelected) colors.accent.copy(alpha = 0.12f) else Color.Transparent
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = CanvasDimens.spaceXs)
                        ) {
                            Text(
                                text = name, 
                                color = if (isSelected) colors.accent else colors.textMain, 
                                fontSize = 11.sp, 
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${w}x${h}", 
                                color = if (isSelected) colors.accent.copy(0.8f) else colors.textMuted, 
                                fontSize = 9.5.sp
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(CanvasDimens.spaceSm))
        }

        CanvasDivider()

        // 3. IA & Comprovantes Bancários (OCR)
        CanvasSectionHeader("IA & Comprovantes (OCR)")
        Spacer(Modifier.height(CanvasDimens.spaceXs))
        
        CanvasToggle(
            label = "OCR On-Device Automático",
            description = "Extrai valores, datas e partes via Google ML Kit",
            checked = config.autoOcrEnabled,
            onCheckedChange = onToggleAutoOcr
        )
        
        CanvasToggle(
            label = "Badges Financeiros nos Cards",
            description = "Exibe tags visuais de R$ e PIX no topo do bloco",
            checked = config.showFinancialBadges,
            onCheckedChange = onToggleFinancialBadges
        )

        CanvasToggle(
            label = "Detalhes de Origem/Destino",
            description = "Exibe De (Pagador) e Para (Destinatário)",
            checked = config.showPartyDetails,
            onCheckedChange = onTogglePartyDetails
        )

        CanvasToggle(
            label = "Preservar Proporção Original",
            description = "Ajusta escala automática sem cortes (Fit)",
            checked = config.fitOriginalAspectRatio,
            onCheckedChange = onToggleFitAspectRatio
        )

        CanvasDivider()
        
        // 4. Módulos de Blocos Habilitados
        CanvasSectionHeader("Módulos de Blocos")
        Spacer(Modifier.height(CanvasDimens.spaceXs))
        modules.forEach { (type, enabled) ->
            val (displayName, desc) = when (type.lowercase()) {
                "text" -> Pair("Bloco de Texto & Notas", "Suporte a markdown, listas e tabelas")
                "image" -> Pair("Bloco de Imagem & Comprovantes", "Importação de galeria e comprovantes bancários")
                "chart" -> Pair("Bloco de Gráfico Radar", "Diagramas radiais com status de múltiplos eixos")
                else -> Pair(type.replaceFirstChar { it.uppercase() }, "")
            }
            CanvasToggle(
                label = displayName,
                description = desc.ifEmpty { null },
                checked = enabled,
                onCheckedChange = { onToggleModule(type, it) }
            )
        }
    }
}
