package com.canvasstudio.ui.block.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.canvasstudio.ui.theme.CanvasColors
import kotlin.math.roundToInt

@Composable
fun BoxScope.BlockResizeHandle(
    key: Long,
    width: Int,
    height: Int,
    isSelected: Boolean,
    colors: CanvasColors,
    density: Density,
    onResize: (Int, Int) -> Unit,
    onSelect: () -> Unit,
    onInteractionChange: (Boolean, Int, Int) -> Unit
) {
    val currentOnResize by rememberUpdatedState(onResize)
    val currentOnSelect by rememberUpdatedState(onSelect)
    val currentOnInteractionChange by rememberUpdatedState(onInteractionChange)

    var rawWidth by remember(key) { mutableFloatStateOf(width.toFloat()) }
    var rawHeight by remember(key) { mutableFloatStateOf(height.toFloat()) }

    LaunchedEffect(width, height) {
        rawWidth = width.toFloat()
        rawHeight = height.toFloat()
    }

    Box(
        Modifier
            .align(Alignment.BottomEnd)
            .size(36.dp)
            .pointerInput(key) {
                detectDragGestures(
                    onDragStart = {
                        currentOnSelect()
                        currentOnInteractionChange(true, rawWidth.roundToInt(), rawHeight.roundToInt())
                    },
                    onDragEnd = {
                        val snappedW = ((rawWidth / 10f).roundToInt() * 10).coerceIn(120, 2000)
                        val snappedH = ((rawHeight / 10f).roundToInt() * 10).coerceIn(80, 2000)
                        currentOnInteractionChange(false, snappedW, snappedH)
                        currentOnResize(snappedW, snappedH)
                    },
                    onDragCancel = {
                        currentOnInteractionChange(false, rawWidth.roundToInt(), rawHeight.roundToInt())
                    }
                ) { change, drag ->
                    change.consume()
                    rawWidth += drag.x / density.density
                    rawHeight += drag.y / density.density
                    val snappedW = ((rawWidth / 10f).roundToInt() * 10).coerceIn(120, 2000)
                    val snappedH = ((rawHeight / 10f).roundToInt() * 10).coerceIn(80, 2000)
                    currentOnInteractionChange(true, snappedW, snappedH)
                }
            }
    ) {
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 5.dp, end = 5.dp)
                .size(14.dp)
                .background(if (isSelected) colors.accent else colors.textMuted, CircleShape)
                .border(1.5.dp, colors.bgMain, CircleShape)
        )
    }
}
