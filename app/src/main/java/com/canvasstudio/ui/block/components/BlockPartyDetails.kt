package com.canvasstudio.ui.block.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasstudio.domain.model.CanvasConfig
import com.canvasstudio.ui.theme.CanvasColors
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun BlockPartyDetails(
    metadata: JsonObject?,
    canvasConfig: CanvasConfig,
    colors: CanvasColors
) {
    val pagador = remember(metadata) { metadata?.get("pagador")?.jsonPrimitive?.contentOrNull }
    val destinatario = remember(metadata) { metadata?.get("destinatario")?.jsonPrimitive?.contentOrNull }
    val instituicao = remember(metadata) { metadata?.get("instituicao")?.jsonPrimitive?.contentOrNull }
    val realizadoEm = remember(metadata) { metadata?.get("realizadoEm")?.jsonPrimitive?.contentOrNull }

    val hasPartyInfo = canvasConfig.showPartyDetails && (pagador != null || destinatario != null || instituicao != null)
    val hasDateInfo = realizadoEm != null && realizadoEm.isNotEmpty()

    if (!hasPartyInfo && !hasDateInfo) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        if (canvasConfig.showPartyDetails) {
            if (pagador != null) {
                Text(
                    text = "📤 De: $pagador",
                    color = colors.textMain.copy(alpha = 0.8f),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
            if (destinatario != null) {
                Text(
                    text = "📥 Para: $destinatario",
                    color = colors.textMain.copy(alpha = 0.95f),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
            if (instituicao != null) {
                Text(
                    text = "🏦 Banco: $instituicao",
                    color = colors.textMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1
                )
            }
        }
        if (realizadoEm != null && realizadoEm.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 1.dp)
            ) {
                Icon(
                    Icons.Rounded.Schedule,
                    contentDescription = null,
                    tint = colors.textMuted.copy(alpha = 0.7f),
                    modifier = Modifier.size(9.dp)
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    text = "Realizado em: $realizadoEm",
                    color = colors.textMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}
