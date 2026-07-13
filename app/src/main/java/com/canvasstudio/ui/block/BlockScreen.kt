package com.canvasstudio.ui.block

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.* import androidx.compose.material.* import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.* import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.canvasstudio.data.local.entity.BlockEntity
import kotlin.math.roundToInt

@Composable
fun BlockScreen(
    uiState: BlockUiState,
    onUpdateBlock: (BlockEntity) -> Unit,
    onDeleteBlock: (BlockEntity) -> Unit,
    onAddBlock: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Canvas de Blocos", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                backgroundColor = Color(0xFF2C2C2E),
                elevation = 4.dp
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddBlock,
                backgroundColor = Color(0xFF6200EE)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Adicionar Bloco", tint = Color.White)
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize().background(Color(0xFF1C1C1E))) {
            // Dot Grid Background
            Canvas(modifier = Modifier.fillMaxSize()) {
                val spacing = 40f
                for (x in 0..size.width.toInt() step spacing.toInt()) {
                    for (y in 0..size.height.toInt() step spacing.toInt()) {
                        drawCircle(color = Color.Gray.copy(alpha = 0.2f), radius = 2f, center = Offset(x.toFloat(), y.toFloat()))
                    }
                }
            }

            when (uiState) {
                is BlockUiState.Loading -> { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                is BlockUiState.Error -> { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(text = "Erro: ${uiState.message}", color = MaterialTheme.colors.error) } }
                is BlockUiState.Success -> {
                    uiState.blocks.forEach { bloco ->
                        DraggableBlock(block = bloco, onUpdateBlock = onUpdateBlock, onDeleteBlock = onDeleteBlock)
                    }
                }
            }
        }
    }
}

@Composable
fun DraggableBlock(block: BlockEntity, onUpdateBlock: (BlockEntity) -> Unit, onDeleteBlock: (BlockEntity) -> Unit) {
    val gridSize = 20
    var offsetX by remember { mutableStateOf(block.posX) }
    var offsetY by remember { mutableStateOf(block.posY) }
    Card(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .width(block.width.dp)
            .height(block.height.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        val snapX = (offsetX / gridSize).roundToInt() * gridSize
                        val snapY = (offsetY / gridSize).roundToInt() * gridSize
                        offsetX = snapX.toFloat()
                        offsetY = snapY.toFloat()
                        onUpdateBlock(block.copy(posX = offsetX, posY = offsetY))
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                )
            },
        backgroundColor = Color(0xFF1E1E21),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        elevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3E3E42))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = block.title, color = Color(0xFFE1E1E6), style = MaterialTheme.typography.subtitle2, fontWeight = FontWeight.Bold)
                IconButton(onClick = { onDeleteBlock(block) }, modifier = Modifier.size(24.dp)) { Icon(imageVector = Icons.Default.Delete, contentDescription = "Deletar", tint = Color(0xFFFF6666)) }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = block.type, color = Color(0xFFA0A0A5), style = MaterialTheme.typography.caption)
        }
    }
}