package com.canvasstudio.ui.block.components

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasstudio.data.local.entity.BlockEntity
import com.canvasstudio.ui.block.BlockViewModel
import com.canvasstudio.ui.block.utils.parseRichText
import com.canvasstudio.ui.theme.CanvasColors
import kotlinx.serialization.json.*
import kotlin.math.roundToInt

@Composable
fun DraggableBlock(
    key: Long, block: BlockEntity, 
    onMove: (Float, Float) -> Unit, onResize: (Int, Int) -> Unit, 
    onDelete: () -> Unit, onEdit: () -> Unit, currentScale: () -> Float,
    colors: CanvasColors,
    viewModel: BlockViewModel
) {
    val updatedOnMove by rememberUpdatedState(onMove)
    val updatedOnResize by rememberUpdatedState(onResize)
    val updatedScale by rememberUpdatedState(currentScale)

    var offsetX by remember(key) { mutableFloatStateOf(block.posX) }
    var offsetY by remember(key) { mutableFloatStateOf(block.posY) }
    var width by remember(key) { mutableIntStateOf(block.width) }
    var height by remember(key) { mutableIntStateOf(block.height) }
    var rawOffsetX by remember(key) { mutableFloatStateOf(block.posX) }
    var rawOffsetY by remember(key) { mutableFloatStateOf(block.posY) }
    var rawWidth by remember(key) { mutableFloatStateOf(block.width.toFloat()) }
    var rawHeight by remember(key) { mutableFloatStateOf(block.height.toFloat()) }
    var isInteracting by remember { mutableStateOf(false) }
    
    var isEditingTitle by remember { mutableStateOf(false) }
    var tempTitle by remember(block.title) { mutableStateOf(block.title) }
    val focusRequester = remember { FocusRequester() }

    val density = LocalDensity.current
    
    LaunchedEffect(block.posX, block.posY, block.width, block.height) {
        if (!isInteracting) {
            offsetX = block.posX
            offsetY = block.posY
            width = block.width
            height = block.height
            rawOffsetX = block.posX
            rawOffsetY = block.posY
            rawWidth = block.width.toFloat()
            rawHeight = block.height.toFloat()
        }
    }

    val metadata = remember(block.contentJson) {
        try { Json.parseToJsonElement(block.contentJson).jsonObject } catch (e: Exception) { null }
    }

    val isLocked by viewModel.isLocked.collectAsState()

    Box(
        modifier = Modifier.offset { 
            IntOffset((offsetX * density.density).roundToInt(), (offsetY * density.density).roundToInt()) 
        }.size(width.dp, height.dp)
            .background(colors.bgCard, RoundedCornerShape(10.dp))
            .border(
                1.dp,
                if (isInteracting && !isLocked) colors.accent else colors.border,
                RoundedCornerShape(10.dp)
            )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(Modifier.weight(1f).pointerInput(key, isLocked) {
                    if (isLocked) return@pointerInput
                    detectDragGestures(
                        onDragStart = { 
                            isInteracting = true 
                            rawOffsetX = offsetX
                            rawOffsetY = offsetY
                        },
                        onDragEnd = { 
                            isInteracting = false
                            updatedOnMove(offsetX, offsetY) 
                        },
                        onDragCancel = { isInteracting = false }
                    ) { change, drag ->
                        change.consume()
                        val s = updatedScale().coerceAtLeast(0.1f)
                        val snap = 10f
                        rawOffsetX += drag.x / (s * density.density)
                        rawOffsetY += drag.y / (s * density.density)
                        offsetX = ((rawOffsetX / snap).roundToInt() * snap).coerceAtLeast(0f)
                        offsetY = ((rawOffsetY / snap).roundToInt() * snap).coerceAtLeast(0f)
                    }
                }, verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DragIndicator, 
                        null, 
                        tint = colors.textMain.copy(alpha = 0.4f), 
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    
                    if (isEditingTitle) {
                        BasicTextField(
                            value = tempTitle,
                            onValueChange = { tempTitle = it },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester)
                                .onFocusChanged { 
                                    if (!it.isFocused && isEditingTitle) {
                                        isEditingTitle = false
                                        if (tempTitle != block.title) {
                                            viewModel.updateBlock(block.copy(title = tempTitle))
                                        }
                                    }
                                },
                            textStyle = TextStyle(
                                color = colors.textMain,
                                fontSize = (metadata?.get("titleSize")?.jsonPrimitive?.intOrNull ?: 12).sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            cursorBrush = SolidColor(colors.accent),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    isEditingTitle = false
                                    if (tempTitle != block.title) {
                                        viewModel.updateBlock(block.copy(title = tempTitle))
                                    }
                                }
                            )
                        )
                        LaunchedEffect(Unit) {
                            focusRequester.requestFocus()
                        }
                    } else {
                        Text(
                            text = block.title, 
                            color = colors.textMain, 
                            fontSize = (metadata?.get("titleSize")?.jsonPrimitive?.intOrNull ?: 12).sp, 
                            fontWeight = FontWeight.SemiBold, 
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.clickable {
                                if (!isLocked) {
                                    isEditingTitle = true
                                }
                            }
                        )
                    }
                }
                IconButton(onClick = { if (!isLocked) onEdit() }, enabled = !isLocked, modifier = Modifier.size(24.dp)) { 
                    Icon(Icons.Default.Edit, null, tint = if(isLocked) colors.textMuted else colors.textMain.copy(0.4f), modifier = Modifier.size(14.dp)) 
                }
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = { if (!isLocked) onDelete() }, enabled = !isLocked, modifier = Modifier.size(24.dp)) { 
                    Icon(Icons.Default.Close, null, tint = if(isLocked) colors.textMuted else colors.textMain.copy(0.4f), modifier = Modifier.size(14.dp)) 
                }
            }
            Box(Modifier.weight(1f).padding(horizontal = 4.dp).clickable(enabled = !isLocked) { onEdit() }) {
                when (block.type.lowercase()) {
                    "chart" -> com.canvasstudio.ui.block.modules.ChartBlock(
                        block.copy(width = width, height = height), 
                        colors = colors
                    )
                    "image" -> ImageBlock(
                        block.copy(width = width, height = height),
                        viewModel = viewModel
                    )
                    else -> {
                        val elements = remember(metadata, block.contentJson) {
                            metadata?.get("elements")?.jsonArray 
                                ?: metadata?.get("campos")?.jsonArray 
                                ?: JsonArray(emptyList())
                        }
                        
                        Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                            Column(Modifier.fillMaxWidth()) {
                                if (elements.isNotEmpty()) {
                                    elements.forEach { element ->
                                        val el = element.jsonObject
                                        val type = el["type"]?.jsonPrimitive?.content ?: "text"
                                        val className = el["className"]?.jsonPrimitive?.content ?: ""
                                        
                                        when (type) {
                                            "text", "" -> {
                                                val text = el["value"]?.jsonPrimitive?.content 
                                                    ?: el["html"]?.jsonPrimitive?.content 
                                                    ?: ""
                                                
                                                val isTitle = className.contains("classe-titulo")
                                                val isNum = className.contains("classe-num")
                                                
                                                Text(
                                                    text = parseRichText(text),
                                                    color = colors.textMain.copy(if (isTitle) 1f else 0.8f),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = if (isTitle) 4.dp else 2.dp)
                                                        .then(if (isTitle) Modifier.drawBehind {
                                                            val strokeWidth = 2.dp.toPx()
                                                            val y = size.height - strokeWidth / 2
                                                            drawLine(
                                                                colors.accent,
                                                                Offset(0f, y),
                                                                Offset(size.width, y),
                                                                strokeWidth = strokeWidth
                                                            )
                                                        } else Modifier),
                                                    fontSize = when {
                                                        isTitle -> 17.sp
                                                        isNum -> 20.sp
                                                        else -> 13.sp
                                                    },
                                                    fontWeight = if (isTitle || isNum) FontWeight.Bold else FontWeight.Normal,
                                                    fontFamily = if (isNum) androidx.compose.ui.text.font.FontFamily.Monospace else null,
                                                    textAlign = when {
                                                        isNum -> TextAlign.Center
                                                        metadata?.get("align")?.jsonPrimitive?.content == "center" -> TextAlign.Center
                                                        metadata?.get("align")?.jsonPrimitive?.content == "right" -> TextAlign.End
                                                        else -> TextAlign.Start
                                                    }
                                                )
                                            }
                                            "table" -> {
                                                val headers = el["headers"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
                                                val rows = el["rows"]?.jsonArray?.map { row -> 
                                                    row.jsonArray.map { it.jsonPrimitive.content } 
                                                } ?: emptyList()
                                                
                                                CanvasTable(headers, rows, colors)
                                            }
                                        }
                                    }
                                } else {
                                    val text = remember(metadata, block.contentJson) {
                                        try {
                                            metadata?.get("text")?.jsonPrimitive?.content ?: block.contentJson
                                        } catch (e: Exception) {
                                            block.contentJson
                                        }
                                    }
                                    Text(
                                        text = parseRichText(text),
                                        color = colors.textMain.copy(0.8f),
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = when(metadata?.get("align")?.jsonPrimitive?.content) { 
                                            "center" -> TextAlign.Center
                                            "right" -> TextAlign.End
                                            else -> TextAlign.Start 
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!isLocked) {
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .size(36.dp)
                    .pointerInput(key) {
                        detectDragGestures(
                            onDragStart = { 
                                isInteracting = true 
                                rawWidth = width.toFloat()
                                rawHeight = height.toFloat()
                            },
                            onDragEnd = { 
                                isInteracting = false
                                updatedOnResize(width, height)
                            },
                            onDragCancel = { isInteracting = false }
                        ) { change, drag ->
                            change.consume()
                            val s = updatedScale().coerceAtLeast(0.1f)
                            val snap = 10f
                            rawWidth += drag.x / (density.density * s)
                            rawHeight += drag.y / (density.density * s)
                            val snappedW = ((rawWidth / snap).roundToInt() * snap).roundToInt()
                            val snappedH = ((rawHeight / snap).roundToInt() * snap).roundToInt()
                            width = snappedW.coerceIn(120, 2000)
                            height = snappedH.coerceIn(80, 2000)
                        }
                    }
            ) { 
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 6.dp, end = 6.dp)
                        .size(14.dp)
                        .background(colors.accent, CircleShape)
                        .border(2.dp, colors.bgMain, CircleShape)
                )
            }
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
