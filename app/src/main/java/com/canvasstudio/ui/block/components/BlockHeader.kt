package com.canvasstudio.ui.block.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasstudio.data.local.entity.BlockEntity
import com.canvasstudio.designsystem.CanvasTheme
import com.canvasstudio.designsystem.components.CanvasBadge
import com.canvasstudio.designsystem.components.CanvasBadgeVariant
import com.canvasstudio.domain.model.CanvasConfig
import com.canvasstudio.ui.theme.CanvasColors
import kotlinx.serialization.json.*
import kotlin.math.roundToInt

@Composable
fun BlockHeader(
    key: Long,
    block: BlockEntity,
    isSelected: Boolean,
    isLocked: Boolean,
    colors: CanvasColors,
    metadata: JsonObject?,
    canvasConfig: CanvasConfig,
    density: Density,
    onMove: (Float, Float) -> Unit,
    onTitleChange: (String) -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onInteractionChange: (Boolean, Float, Float) -> Unit
) {
    val currentBlock by rememberUpdatedState(block)
    val currentOnMove by rememberUpdatedState(onMove)
    val currentOnInteractionChange by rememberUpdatedState(onInteractionChange)

    var dragX by remember(key) { mutableFloatStateOf(block.posX) }
    var dragY by remember(key) { mutableFloatStateOf(block.posY) }

    LaunchedEffect(block.posX, block.posY) {
        dragX = block.posX
        dragY = block.posY
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) colors.accent.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.05f))
            .padding(horizontal = 8.dp, vertical = 5.dp)
            .pointerInput(key, isLocked) {
                if (isLocked) return@pointerInput
                detectDragGestures(
                    onDragStart = {
                        dragX = currentBlock.posX
                        dragY = currentBlock.posY
                        currentOnInteractionChange(true, dragX, dragY)
                    },
                    onDragEnd = {
                        val snap = 10f
                        val snappedX = ((dragX / snap).roundToInt() * snap).coerceAtLeast(0f)
                        val snappedY = ((dragY / snap).roundToInt() * snap).coerceAtLeast(0f)
                        currentOnInteractionChange(false, snappedX, snappedY)
                        currentOnMove(snappedX, snappedY)
                    },
                    onDragCancel = {
                        currentOnInteractionChange(false, currentBlock.posX, currentBlock.posY)
                    }
                ) { change, drag ->
                    change.consume()
                    val snap = 10f
                    dragX += drag.x / density.density
                    dragY += drag.y / density.density
                    val snappedX = ((dragX / snap).roundToInt() * snap).coerceAtLeast(0f)
                    val snappedY = ((dragY / snap).roundToInt() * snap).coerceAtLeast(0f)
                    currentOnInteractionChange(true, snappedX, snappedY)
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (CanvasTheme.isTeenage) {
            Box(Modifier.padding(end = 4.dp).size(5.dp).background(if (isSelected) colors.accent else colors.accentVariant.copy(alpha = 0.6f), androidx.compose.foundation.shape.CircleShape))
        }

        Box(
            modifier = Modifier.size(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.DragIndicator, "Mover bloco", tint = if (isSelected) colors.accent else colors.textMain.copy(alpha = 0.45f), modifier = Modifier.size(18.dp))
        }

        var currentBlockTitle by remember(block.title) { mutableStateOf(block.title) }
        BasicTextField(
            value = currentBlockTitle,
            onValueChange = { currentBlockTitle = it },
            modifier = Modifier.weight(1f).padding(vertical = 2.dp).onFocusChanged { if (!it.isFocused && currentBlockTitle != block.title) onTitleChange(currentBlockTitle) },
            enabled = !isLocked,
            textStyle = TextStyle(color = if (isSelected) colors.accent else colors.textMain, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = CanvasTheme.fontFamily),
            cursorBrush = SolidColor(colors.accent),
            singleLine = true,
            decorationBox = { inner ->
                if (currentBlockTitle.isEmpty()) Text("Título...", color = colors.textMuted, fontSize = 12.sp, fontFamily = CanvasTheme.fontFamily)
                inner()
            }
        )

        val isPix = remember(metadata) { metadata?.get("isPix")?.jsonPrimitive?.booleanOrNull == true || block.title.contains("PIX", ignoreCase = true) }
        if (isPix && canvasConfig.showFinancialBadges) {
            CanvasBadge(text = "PIX", variant = CanvasBadgeVariant.Pix, modifier = Modifier.padding(horizontal = 2.dp))
        }

        val valorFormatted = remember(metadata) {
            metadata?.get("valorFormatted")?.jsonPrimitive?.content ?: metadata?.get("valor")?.jsonPrimitive?.floatOrNull?.let {
                "R$ " + String.format(java.util.Locale("pt", "BR"), "%,.2f", it)
            }
        }
        if (valorFormatted != null && canvasConfig.showFinancialBadges) {
            CanvasBadge(text = valorFormatted, variant = CanvasBadgeVariant.Financial, modifier = Modifier.padding(horizontal = 2.dp))
        }

        if (!isLocked) {
            IconButton(onClick = onDuplicate, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Rounded.ContentCopy, "Duplicar", tint = colors.textMain.copy(0.35f), modifier = Modifier.size(13.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, "Excluir Bloco", tint = colors.textMuted, modifier = Modifier.size(14.dp))
            }
        }
    }
}
