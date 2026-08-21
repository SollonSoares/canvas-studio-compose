package com.canvasstudio.ui.block.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasstudio.data.local.entity.BlockEntity
import com.canvasstudio.designsystem.CanvasTheme
import com.canvasstudio.ui.block.BlockViewModel
import com.canvasstudio.ui.theme.CanvasColors
import kotlinx.serialization.json.*
import kotlin.math.roundToInt

@Composable
fun DraggableBlock(
    key: Long,
    block: BlockEntity,
    onMove: (Float, Float) -> Unit,
    onResize: (Int, Int) -> Unit,
    onDelete: () -> Unit,
    onSelect: () -> Unit,
    isSelected: Boolean,
    colors: CanvasColors,
    viewModel: BlockViewModel
) {
    var offsetX by remember(key) { mutableFloatStateOf(block.posX) }
    var offsetY by remember(key) { mutableFloatStateOf(block.posY) }
    var width by remember(key) { mutableIntStateOf(block.width) }
    var height by remember(key) { mutableIntStateOf(block.height) }
    var isInteracting by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val isLocked by viewModel.isLocked.collectAsState()
    val canvasConfig by viewModel.canvasConfig.collectAsState()

    LaunchedEffect(block.posX, block.posY, block.width, block.height) {
        if (!isInteracting) {
            offsetX = block.posX
            offsetY = block.posY
            width = block.width
            height = block.height
        }
    }

    val metadata = remember(block.contentJson) {
        try { Json.parseToJsonElement(block.contentJson).jsonObject } catch (e: Exception) { null }
    }

    val parsedTextColor = remember(metadata) {
        val hex = metadata?.get("textColor")?.jsonPrimitive?.content
        if (!hex.isNullOrBlank()) {
            try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { null }
        } else null
    }

    val fontSize = (metadata?.get("fontSize")?.jsonPrimitive?.intOrNull
        ?: metadata?.get("titleSize")?.jsonPrimitive?.intOrNull ?: 13).sp
    val isBold = metadata?.get("isBold")?.jsonPrimitive?.booleanOrNull ?: false
    val isItalic = metadata?.get("isItalic")?.jsonPrimitive?.booleanOrNull ?: false
    val alignStr = metadata?.get("align")?.jsonPrimitive?.content ?: "left"
    val blockTextAlign = when (alignStr.lowercase()) {
        "center" -> TextAlign.Center
        "right" -> TextAlign.End
        else -> TextAlign.Start
    }

    val borderColor = when {
        isSelected -> colors.accent
        isInteracting && !isLocked -> colors.accent.copy(alpha = 0.6f)
        else -> colors.border
    }
    val borderWidth = if (isSelected) 2.dp else CanvasTheme.shapes.borderWidth

    Box(
        modifier = Modifier
            .offset { IntOffset((offsetX * density.density).roundToInt(), (offsetY * density.density).roundToInt()) }
            .size(width.dp, height.dp)
            .background(colors.bgCard, CanvasTheme.shapes.shapeMd)
            .border(borderWidth, borderColor, CanvasTheme.shapes.shapeMd)
            .pointerInput(key) {
                detectTapGestures(
                    onDoubleTap = { onSelect() }
                )
            }
    ) {
        Column {
            BlockHeader(
                key = key,
                block = block,
                isSelected = isSelected,
                isLocked = isLocked,
                colors = colors,
                metadata = metadata,
                canvasConfig = canvasConfig,
                density = density,
                onMove = onMove,
                onTitleChange = { viewModel.updateBlock(block.copy(title = it)) },
                onDuplicate = { viewModel.duplicateBlock(block) },
                onDelete = onDelete,
                onInteractionChange = { active, x, y ->
                    isInteracting = active
                    offsetX = x
                    offsetY = y
                }
            )

            BlockPartyDetails(metadata, canvasConfig, colors)

            Box(Modifier.weight(1f).fillMaxWidth()) {
                BlockContentEditor(
                    block = block,
                    width = width,
                    height = height,
                    isLocked = isLocked,
                    colors = colors,
                    metadata = metadata,
                    parsedTextColor = parsedTextColor,
                    fontSize = fontSize,
                    isBold = isBold,
                    isItalic = isItalic,
                    blockTextAlign = blockTextAlign,
                    viewModel = viewModel
                )
            }
        }

        if (!isLocked) {
            BlockResizeHandle(
                key = key,
                width = width,
                height = height,
                isSelected = isSelected,
                colors = colors,
                density = density,
                onResize = onResize,
                onSelect = onSelect,
                onInteractionChange = { active, w, h ->
                    isInteracting = active
                    width = w
                    height = h
                }
            )
        }

        if (isInteracting) {
            Surface(
                modifier = Modifier.align(Alignment.TopCenter).offset(y = (-25).dp),
                color = colors.accent,
                shape = RoundedCornerShape(4.dp),
                elevation = 4.dp
            ) {
                Text(
                    text = "${offsetX.toInt()}, ${offsetY.toInt()} | ${width}x${height}",
                    color = Color.White,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
