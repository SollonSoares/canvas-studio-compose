package com.canvasstudio.ui.block.components

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
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
    key: Long, 
    block: BlockEntity, 
    onMove: (Float, Float) -> Unit, 
    onResize: (Int, Int) -> Unit, 
    onDelete: () -> Unit, 
    onSelect: () -> Unit,
    isSelected: Boolean,
    currentScale: () -> Float,
    colors: CanvasColors,
    viewModel: BlockViewModel
) {
    val updatedOnMove by rememberUpdatedState(onMove)
    val updatedOnResize by rememberUpdatedState(onResize)
    val updatedScale by rememberUpdatedState(currentScale)
    val updatedOnSelect by rememberUpdatedState(onSelect)

    var offsetX by remember(key) { mutableFloatStateOf(block.posX) }
    var offsetY by remember(key) { mutableFloatStateOf(block.posY) }
    var width by remember(key) { mutableIntStateOf(block.width) }
    var height by remember(key) { mutableIntStateOf(block.height) }
    var rawOffsetX by remember(key) { mutableFloatStateOf(block.posX) }
    var rawOffsetY by remember(key) { mutableFloatStateOf(block.posY) }
    var rawWidth by remember(key) { mutableFloatStateOf(block.width.toFloat()) }
    var rawHeight by remember(key) { mutableFloatStateOf(block.height.toFloat()) }
    var isInteracting by remember { mutableStateOf(false) }

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

    val parsedTextColor = remember(metadata) {
        val hex = metadata?.get("textColor")?.jsonPrimitive?.content
        if (!hex.isNullOrBlank()) {
            try {
                Color(android.graphics.Color.parseColor(hex))
            } catch (e: Exception) {
                null
            }
        } else null
    }

    val fontSize = (metadata?.get("fontSize")?.jsonPrimitive?.intOrNull 
        ?: metadata?.get("titleSize")?.jsonPrimitive?.intOrNull 
        ?: 13).sp
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
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Box(
        modifier = Modifier
            .offset { 
                IntOffset((offsetX * density.density).roundToInt(), (offsetY * density.density).roundToInt()) 
            }
            .size(width.dp, height.dp)
            .background(colors.bgCard, RoundedCornerShape(10.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(10.dp))
            .pointerInput(key) {
                detectTapGestures(
                    onDoubleTap = { updatedOnSelect() }
                )
            }
    ) {
        Column {
            // Header do Bloco
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isSelected) colors.accent.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.05f))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Alça de Arraste (Drag Handle)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .pointerInput(key, isLocked) {
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
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.DragIndicator, 
                        "Mover bloco", 
                        tint = if (isSelected) colors.accent else colors.textMain.copy(alpha = 0.4f), 
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                // Campo Título Direto e Editável (Salva ao perder o foco para performance máxima)
                var currentBlockTitle by remember(block.title) { mutableStateOf(block.title) }
                BasicTextField(
                    value = currentBlockTitle,
                    onValueChange = {
                        currentBlockTitle = it
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 2.dp)
                        .onFocusChanged {
                            if (!it.isFocused && currentBlockTitle != block.title) {
                                viewModel.updateBlock(block.copy(title = currentBlockTitle))
                            }
                        },
                    enabled = !isLocked,
                    textStyle = TextStyle(
                        color = if (isSelected) colors.accent else colors.textMain,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    cursorBrush = SolidColor(colors.accent),
                    singleLine = true,
                    decorationBox = { inner ->
                        if (currentBlockTitle.isEmpty()) {
                            Text("Título...", color = colors.textMuted, fontSize = 12.sp)
                        }
                        inner()
                    }
                )

                // Badge de PIX (Ciano / Teal)
                val isPix = remember(metadata) {
                    metadata?.get("isPix")?.jsonPrimitive?.booleanOrNull == true || block.title.contains("PIX", ignoreCase = true)
                }
                if (isPix) {
                    Surface(
                        color = Color(0xFF32BCAD).copy(alpha = 0.18f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) {
                        Text(
                            text = "PIX",
                            color = Color(0xFF32BCAD),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                // Badge de Valor Monetário Extraído (Ex: R$ 150,00)
                val valorFormatted = remember(metadata) {
                    metadata?.get("valorFormatted")?.jsonPrimitive?.content
                        ?: metadata?.get("valor")?.jsonPrimitive?.floatOrNull?.let { 
                            "R$ " + String.format(java.util.Locale("pt", "BR"), "%,.2f", it) 
                        }
                }

                if (valorFormatted != null) {
                    Surface(
                        color = Color(0xFF34C759).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = valorFormatted,
                            color = Color(0xFF34C759),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                if (!isLocked) {
                    IconButton(
                        onClick = { viewModel.duplicateBlock(block) },
                        modifier = Modifier.size(24.dp)
                    ) { 
                        Icon(
                            Icons.Rounded.ContentCopy, 
                            "Duplicar", 
                            tint = colors.textMain.copy(0.35f), 
                            modifier = Modifier.size(13.dp)
                        ) 
                    }
                    Spacer(Modifier.width(2.dp))
                    IconButton(
                        onClick = onDelete, 
                        modifier = Modifier.size(24.dp)
                    ) { 
                        Icon(
                            Icons.Default.Close, 
                            "Excluir Bloco", 
                            tint = colors.textMuted, 
                            modifier = Modifier.size(14.dp)
                        ) 
                    }
                }
            }

            // Metadados do Comprovante (Pagador "De", Destinatário "Para", Instituição e Data)
            val pagador = remember(metadata) { metadata?.get("pagador")?.jsonPrimitive?.contentOrNull }
            val destinatario = remember(metadata) { metadata?.get("destinatario")?.jsonPrimitive?.contentOrNull }
            val instituicao = remember(metadata) { metadata?.get("instituicao")?.jsonPrimitive?.contentOrNull }
            val realizadoEm = remember(metadata) { metadata?.get("realizadoEm")?.jsonPrimitive?.contentOrNull }

            if (pagador != null || destinatario != null || instituicao != null || (realizadoEm != null && realizadoEm.isNotEmpty())) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
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

            // Corpo do Bloco
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                when (block.type.lowercase()) {
                    "chart" -> com.canvasstudio.ui.block.modules.ChartBlock(
                        block.copy(width = width, height = height), 
                        colors = colors
                    )
                    "image" -> ImageBlock(
                        block.copy(width = width, height = height),
                        viewModel = viewModel,
                        colors = colors
                    )
                    else -> {
                        val elements = remember(metadata, block.contentJson) {
                            metadata?.get("elements")?.jsonArray 
                                ?: metadata?.get("campos")?.jsonArray 
                                ?: JsonArray(emptyList())
                        }
                        
                        if (elements.isNotEmpty()) {
                            // Suporte para blocos com tabelas ou elementos estruturados legados
                            Box(Modifier.fillMaxSize()) {
                                Column(Modifier.fillMaxWidth()) {
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
                                                    color = parsedTextColor ?: colors.textMain.copy(if (isTitle) 1f else 0.8f),
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
                                                        else -> fontSize
                                                    },
                                                    fontWeight = if (isTitle || isNum || isBold) FontWeight.Bold else FontWeight.Normal,
                                                    fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                                                    fontFamily = if (isNum) androidx.compose.ui.text.font.FontFamily.Monospace else null,
                                                    textAlign = when {
                                                        isNum -> TextAlign.Center
                                                        else -> blockTextAlign
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
                                }
                            }
                        } else {
                            // Edição Direta Inline no Corpo do Bloco (WYSIWYG)
                            val rawText = remember(metadata, block.contentJson) {
                                try {
                                    metadata?.get("text")?.jsonPrimitive?.content ?: block.contentJson
                                } catch (e: Exception) {
                                    block.contentJson
                                }
                            }
                            var inlineText by remember(rawText) { mutableStateOf(rawText) }

                            Box(Modifier.fillMaxSize()) {
                                BasicTextField(
                                    value = inlineText,
                                    onValueChange = {
                                        inlineText = it
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp)
                                        .onFocusChanged {
                                            if (!it.isFocused && inlineText != rawText) {
                                                viewModel.updateBlockContentText(block, inlineText)
                                            }
                                        },
                                    enabled = !isLocked,
                                    textStyle = TextStyle(
                                        color = parsedTextColor ?: colors.textMain.copy(0.9f),
                                        fontSize = fontSize,
                                        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                                        fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                                        textAlign = blockTextAlign,
                                        lineHeight = (fontSize.value * 1.35f).sp
                                    ),
                                    cursorBrush = SolidColor(colors.accent),
                                    decorationBox = { innerTextField ->
                                        if (inlineText.isEmpty()) {
                                            Text(
                                                "Escreva aqui...",
                                                color = colors.textMuted,
                                                fontSize = fontSize,
                                                textAlign = blockTextAlign,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Alça de Redimensionamento no canto inferior direito
        if (!isLocked) {
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .size(32.dp)
                    .pointerInput(key) {
                        detectDragGestures(
                            onDragStart = { 
                                isInteracting = true 
                                updatedOnSelect()
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
                        .padding(bottom = 5.dp, end = 5.dp)
                        .size(12.dp)
                        .background(if (isSelected) colors.accent else colors.textMuted, CircleShape)
                        .border(1.5.dp, colors.bgMain, CircleShape)
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

