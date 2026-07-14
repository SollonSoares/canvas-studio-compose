package com.canvasstudio.ui.block

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import java.io.BufferedReader
import java.io.InputStreamReader
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import coil.compose.AsyncImage
import com.canvasstudio.data.local.entity.BlockEntity
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import java.util.Locale
import kotlin.math.roundToInt

data class CanvasColors(
    val bgMain: Color,
    val bgMenu: Color,
    val accent: Color,
    val canvasGrid: Color,
    val textMain: Color,
    val border: Color
)

@Composable
fun getDynamicColors() = if (MaterialTheme.colors.isLight) {
    CanvasColors(
        bgMain = Color(0xFFF2F2F7),
        bgMenu = Color(0xFFFFFFFF),
        accent = Color(0xFF007AFF),
        canvasGrid = Color(0xFFD1D1D6),
        textMain = Color(0xFF1C1C1E),
        border = Color(0xFFD1D1D6)
    )
} else {
    CanvasColors(
        bgMain = Color(0xFF1C1C1E),
        bgMenu = Color(0xF22C2C2E),
        accent = Color(0xFF0A84FF),
        canvasGrid = Color(0xFF2C2C2E),
        textMain = Color(0xFFF5F5F7),
        border = Color.White.copy(alpha = 0.15f)
    )
}

fun parseRichText(text: String, defaultSize: Int = 14): AnnotatedString {
    val builder = AnnotatedString.Builder()
    var currentIndex = 0
    val regex = Regex("(\\*\\*.*?\\*\\*)|(\\*.*?\\*)|(<u>.*?</u>)|(\\[size=\\d+\\].*?\\[/size\\])")
    val matches = regex.findAll(text)

    matches.forEach { match ->
        if (match.range.first > currentIndex) {
            builder.append(text.substring(currentIndex, match.range.first))
        }
        val tagText = match.value
        when {
            tagText.startsWith("**") -> builder.withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(tagText.removeSurrounding("**")) }
            tagText.startsWith("*") -> builder.withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(tagText.removeSurrounding("*")) }
            tagText.startsWith("<u>") -> builder.withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) { append(tagText.removePrefix("<u>").removeSuffix("</u>")) }
            tagText.startsWith("[size=") -> {
                val size = tagText.substringAfter("=").substringBefore("]").toIntOrNull() ?: defaultSize
                builder.withStyle(SpanStyle(fontSize = size.sp)) { append(tagText.substringAfter("]").removeSuffix("[/size]")) }
            }
        }
        currentIndex = match.range.last + 1
    }
    if (currentIndex < text.length) builder.append(text.substring(currentIndex))
    return builder.toAnnotatedString()
}

@Composable
fun BlockScreen(uiState: BlockUiState, viewModel: BlockViewModel, onBack: () -> Unit) {
    val colors = getDynamicColors()
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var editingBlock by remember { mutableStateOf<BlockEntity?>(null) }
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val appliedQuery by viewModel.appliedQuery.collectAsStateWithLifecycle()

    val brandTitle by viewModel.brandTitle.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    var showSettingsModal by remember { mutableStateOf(false) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val content = reader.readText()
                    viewModel.importFromJson(content)
                }
            } catch (e: Exception) {
                scope.launch {
                    scaffoldState.snackbarHostState.showSnackbar("Erro ao ler arquivo: ${e.message}")
                }
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            try {
                val jsonString = viewModel.exportToJson()
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(jsonString.toByteArray())
                }
                scope.launch {
                    scaffoldState.snackbarHostState.showSnackbar("Canvas exportado com sucesso!")
                }
            } catch (e: Exception) {
                scope.launch {
                    scaffoldState.snackbarHostState.showSnackbar("Erro ao exportar arquivo: ${e.message}")
                }
            }
        }
    }

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.15f, 3f)
        offset += offsetChange
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text(brandTitle, color = colors.textMain, fontWeight = FontWeight.Bold) },
                backgroundColor = colors.bgMenu,
                actions = {
                    IconButton(onClick = { showSettingsModal = true }) {
                        Icon(Icons.Default.Settings, "Configurações", tint = colors.accent)
                    }
                    IconButton(onClick = { scale = 1f; offset = Offset.Zero }) {
                        Icon(Icons.Default.FilterCenterFocus, "Resetar Visão", tint = colors.accent)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { scaffoldState.drawerState.open() } }) {
                        Icon(Icons.Default.Menu, "Menu", tint = colors.accent)
                    }
                }
            )
        },
        floatingActionButton = {
            val density = LocalDensity.current
            FloatingActionButton(
                onClick = {
                    val spawnX = ((-offset.x / density.density) + 200f) / scale
                    val spawnY = ((-offset.y / density.density) + 200f) / scale
                    viewModel.insertBlock(BlockEntity(0, 0, "Novo Bloco", "text", spawnX, spawnY, 220, 180, 
                        "{\"text\":\"**Título**\\nEscreva aqui...\", \"titleSize\": 13, \"titleBold\": true, \"align\": \"left\"}"))
                },
                backgroundColor = colors.accent
            ) { Icon(Icons.Default.Add, "Adicionar", tint = Color.White) }
        },
        drawerContent = { 
            val modules by viewModel.modulesState.collectAsState()
            SidebarContent(
                q = searchQuery, 
                onQ = { viewModel.setSearchQuery(it) }, 
                onSearch = { 
                    viewModel.applySearch()
                    scope.launch { scaffoldState.drawerState.close() }
                },
                onImp = { importLauncher.launch("*/*") }, 
                onExp = { exportLauncher.launch("canvas_export.json") },
                onClr = { viewModel.clearCanvas() },
                onGen = { viewModel.generateTestBlocks(50) },
                onOrg = { viewModel.autoOrganizeBlocks() },
                modules = modules,
                onToggleModule = { type, enabled -> viewModel.toggleModule(type, enabled) },
                isDarkMode = isDarkMode,
                onToggleTheme = { viewModel.toggleDarkMode() },
                colors = colors
            ) 
        }
    ) { padding ->
        Box(
            Modifier.padding(padding).fillMaxSize().background(colors.bgMain)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offset += dragAmount
                    }
                }
                .transformable(state = transformState)
        ) {
            CanvasBackground({ scale }, { offset }, colors.canvasGrid)

            Box(Modifier.fillMaxSize().graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y
            }) {
                if (uiState is BlockUiState.Success) {
                    val blocks = (uiState as BlockUiState.Success).blocks
                    blocks.forEach { block ->
                        key(block.id) {
                            DraggableBlock(block.id, block, 
                                { x, y -> viewModel.updateBlockLive(block.copy(posX = x, posY = y)) },
                                { w, h -> viewModel.updateBlockLive(block.copy(width = w, height = h)) },
                                { viewModel.deleteBlock(block) },
                                { editingBlock = block },
                                currentScale = { scale },
                                colors = colors
                            )
                        }
                    }
                }
            }

            if (uiState is BlockUiState.Success) {
                val blocks = (uiState as BlockUiState.Success).blocks
                if (appliedQuery.isNotEmpty()) {
                    Column(
                        Modifier.align(Alignment.TopCenter).padding(top = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = colors.accent.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(20.dp),
                            elevation = 4.dp
                        ) {
                            Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FilterList, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Filtrando por: \"$appliedQuery\"", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { viewModel.setSearchQuery(""); viewModel.applySearch() }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                        
                        if (blocks.isEmpty()) {
                            Spacer(Modifier.height(100.dp))
                            Text("Nenhum bloco corresponde à sua busca.", color = colors.textMain.copy(0.4f), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            if (uiState is BlockUiState.Loading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center), color = colors.accent)
            }
            
            editingBlock?.let { block ->
                EditBlockDialog(block, { editingBlock = null }, { viewModel.updateBlock(it); editingBlock = null }, { viewModel.updateBlockLive(it) }, colors)
            }

            if (showSettingsModal) {
                val modules by viewModel.modulesState.collectAsState()
                SettingsModal(
                    title = brandTitle,
                    onTitleChange = { viewModel.setBrandTitle(it) },
                    modules = modules,
                    onToggleModule = { type, enabled -> viewModel.toggleModule(type, enabled) },
                    onDismiss = { showSettingsModal = false },
                    colors = colors
                )
            }
        }
    }
}

@Composable
fun SettingsModal(title: String, onTitleChange: (String) -> Unit, modules: Map<String, Boolean>, onToggleModule: (String, Boolean) -> Unit, onDismiss: () -> Unit, colors: CanvasColors) {
    AlertDialog(
        onDismissRequest = onDismiss,
        backgroundColor = colors.bgMenu,
        shape = RoundedCornerShape(16.dp),
        title = { Text("Configurações", color = colors.textMain, fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.fillMaxWidth()) {
                Text("Título do Projeto", color = colors.textMain.copy(0.6f), fontSize = 12.sp)
                TextField(
                    value = title,
                    onValueChange = onTitleChange,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.textFieldColors(textColor = colors.textMain, cursorColor = colors.accent, focusedIndicatorColor = colors.accent, backgroundColor = Color.Transparent)
                )
                
                Spacer(Modifier.height(24.dp))
                
                Text("Módulos Habilitados", color = colors.textMain.copy(0.6f), fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                modules.forEach { (type, enabled) ->
                    ModuleToggle(type.replaceFirstChar { it.uppercase() }, enabled, { onToggleModule(type, it) }, colors)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(backgroundColor = colors.accent)) {
                Text("Fechar", color = Color.White)
            }
        }
    )
}

@Composable
fun CanvasBackground(scale: () -> Float, offset: () -> Offset, gridColor: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val s = scale()
        val o = offset()
        val gridSize = 25.dp.toPx() * s
        val dotRadius = (1.2.dp.toPx() * s).coerceIn(1f, 2.5f)
        val startX = (o.x % gridSize) - gridSize
        val startY = (o.y % gridSize) - gridSize
        
        var x = startX
        while (x < size.width + gridSize) {
            var y = startY
            while (y < size.height + gridSize) {
                drawCircle(gridColor, dotRadius, Offset(x, y))
                y += gridSize
            }
            x += gridSize
        }
    }
}

@Composable
fun DraggableBlock(
    key: Long, block: BlockEntity, 
    onMove: (Float, Float) -> Unit, onResize: (Int, Int) -> Unit, 
    onDelete: () -> Unit, onEdit: () -> Unit, currentScale: () -> Float,
    colors: CanvasColors
) {
    val updatedOnMove by rememberUpdatedState(onMove)
    val updatedOnResize by rememberUpdatedState(onResize)
    val updatedScale by rememberUpdatedState(currentScale)

    var offsetX by remember(key) { mutableFloatStateOf(block.posX) }
    var offsetY by remember(key) { mutableFloatStateOf(block.posY) }
    var width by remember(key) { mutableIntStateOf(block.width) }
    var height by remember(key) { mutableIntStateOf(block.height) }

    val density = LocalDensity.current
    
    LaunchedEffect(block.posX, block.posY, block.width, block.height) {
        offsetX = block.posX
        offsetY = block.posY
        width = block.width
        height = block.height
    }

    val metadata = remember(block.contentJson) {
        try { Json.parseToJsonElement(block.contentJson).jsonObject } catch (e: Exception) { null }
    }

    Box(
        modifier = Modifier.offset { 
            IntOffset((offsetX * density.density).roundToInt(), (offsetY * density.density).roundToInt()) 
        }.size(width.dp, height.dp)
            .background(colors.bgMenu.copy(alpha = 0.95f), RoundedCornerShape(12.dp))
            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
    ) {
        Column {
            Row(Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)).padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f).pointerInput(key) {
                    detectDragGestures(onDragEnd = { updatedOnMove(offsetX, offsetY) }) { change, drag ->
                        change.consume()
                        val s = updatedScale()
                        offsetX += drag.x / (s * density.density)
                        offsetY += drag.y / (s * density.density)
                    }
                }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).background(colors.accent, CircleShape))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = block.title, 
                        color = colors.textMain, 
                        fontSize = (metadata?.get("titleSize")?.jsonPrimitive?.intOrNull ?: 13).sp, 
                        fontWeight = FontWeight.Bold, 
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Edit, null, tint = colors.textMain.copy(0.4f), modifier = Modifier.size(14.dp)) }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Close, null, tint = colors.textMain.copy(0.4f), modifier = Modifier.size(14.dp)) }
            }
            Box(Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 8.dp).clickable { onEdit() }) {
                when (block.type.lowercase()) {
                    "chart" -> com.canvasstudio.ui.block.modules.ChartBlock(block, colors = colors)
                    "image" -> ImageBlock(block)
                    else -> {
                        val text = remember(metadata, block.contentJson) {
                            metadata?.get("text")?.jsonPrimitive?.content ?: block.contentJson
                        }
                        val richText = remember(text) { parseRichText(text) }
                        
                        Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                            Text(
                                text = richText, 
                                color = colors.textMain.copy(0.8f), 
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = when(metadata?.get("align")?.jsonPrimitive?.content) { 
                                    "center" -> TextAlign.Center; 
                                    "right" -> TextAlign.End; 
                                    else -> TextAlign.Start 
                                }
                            )
                        }
                    }
                }
            }
        }
        Box(Modifier.align(Alignment.BottomEnd).size(20.dp).pointerInput(key) {
            detectDragGestures(onDragEnd = { updatedOnResize(width, height) }) { change, drag ->
                change.consume()
                val s = updatedScale()
                width = (width + (drag.x / (density.density * s)).toInt()).coerceAtLeast(100)
                height = (height + (drag.y / (density.density * s)).toInt()).coerceAtLeast(80)
            }
        }) { Canvas(Modifier.fillMaxSize()) { drawLine(colors.accent.copy(0.5f), Offset(size.width, 0f), Offset(0f, size.height), 2.dp.toPx()) } }
    }
}

@Composable
fun EditBlockDialog(block: BlockEntity, onDismiss: () -> Unit, onConfirm: (BlockEntity) -> Unit, onLiveUpdate: (BlockEntity) -> Unit, colors: CanvasColors) {
    var title by remember { mutableStateOf(block.title) }
    var type by remember { mutableStateOf(block.type) }
    val initialContent = remember(block.contentJson) {
        try { Json.parseToJsonElement(block.contentJson).jsonObject } catch (e: Exception) { buildJsonObject {} }
    }
    var textFieldValue by remember { 
        val text = initialContent["text"]?.jsonPrimitive?.content ?: ""
        mutableStateOf(TextFieldValue(text)) 
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        backgroundColor = colors.bgMenu,
        shape = RoundedCornerShape(16.dp),
        title = { Text("Editar Bloco", color = colors.textMain, fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Text("Título", color = colors.textMain.copy(0.6f), fontSize = 12.sp)
                TextField(value = title, onValueChange = { title = it }, modifier = Modifier.fillMaxWidth(), colors = TextFieldDefaults.textFieldColors(textColor = colors.textMain, cursorColor = colors.accent, focusedIndicatorColor = colors.accent, backgroundColor = Color.Transparent))
                Spacer(Modifier.height(16.dp))
                Text("Tipo", color = colors.textMain.copy(0.6f), fontSize = 12.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("text", "image", "chart").forEach { t ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { type = t }) {
                            RadioButton(selected = type == t, onClick = { type = t }, colors = RadioButtonDefaults.colors(selectedColor = colors.accent))
                            Text(t.replaceFirstChar { it.uppercase() }, color = colors.textMain, fontSize = 14.sp)
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onConfirm(block.copy(title = title, type = type)) }, colors = ButtonDefaults.buttonColors(backgroundColor = colors.accent)) { Text("Salvar", color = Color.White) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = colors.textMain.copy(0.6f)) } }
    )
}

fun insertTag(textFieldValue: TextFieldValue, startTag: String, endTag: String): TextFieldValue {
    val text = textFieldValue.text
    val selection = textFieldValue.selection
    val newText = text.substring(0, selection.start) + startTag + text.substring(selection.start, selection.end) + endTag + text.substring(selection.end)
    return textFieldValue.copy(text = newText, selection = TextRange(selection.start + startTag.length, selection.end + startTag.length))
}

@Composable
fun FormatAction(icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean = false, onClick: () -> Unit) {
    IconButton(onClick = onClick) { Icon(icon, null, tint = if (isSelected) Color.Cyan else Color.White) }
}

@Composable
fun ImageBlock(block: BlockEntity) {
    val metadata = try { Json.parseToJsonElement(block.contentJson).jsonObject } catch (e: Exception) { null }
    val url = metadata?.get("url")?.jsonPrimitive?.content ?: ""
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (url.isNotEmpty()) {
            AsyncImage(model = url, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            Icon(Icons.Default.Image, null, tint = Color.Gray, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun SidebarContent(
    q: String, onQ: (String) -> Unit, onSearch: () -> Unit, onImp: () -> Unit, onExp: () -> Unit, 
    onClr: () -> Unit, onGen: () -> Unit, onOrg: () -> Unit, modules: Map<String, Boolean>, 
    onToggleModule: (String, Boolean) -> Unit, isDarkMode: Boolean, onToggleTheme: () -> Unit, colors: CanvasColors
) {
    Column(Modifier.fillMaxSize().background(colors.bgMenu).padding(24.dp).verticalScroll(rememberScrollState())) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Aparência", color = colors.textMain.copy(0.6f), fontSize = 12.sp)
            IconButton(onClick = onToggleTheme) {
                Icon(if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode, "Tema", tint = colors.accent)
            }
        }
        Spacer(Modifier.height(16.dp))
        TextField(value = q, onValueChange = onQ, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Buscar...", color = colors.textMain.copy(0.3f)) }, leadingIcon = { Icon(Icons.Default.Search, null, tint = colors.accent) }, colors = TextFieldDefaults.textFieldColors(textColor = colors.textMain, cursorColor = colors.accent, focusedIndicatorColor = colors.accent, backgroundColor = Color.White.copy(0.05f)))
        Spacer(Modifier.height(24.dp))
        SidebarButton("📥 Importar JSON", colors.accent, onImp)
        Spacer(Modifier.height(8.dp))
        SidebarButton("📤 Exportar JSON", colors.accent, onExp)
        Spacer(Modifier.height(8.dp))
        SidebarButton("🧩 Auto Organizar", colors.accent, onOrg)
        Spacer(Modifier.height(8.dp))
        SidebarButton("🧪 Gerar Blocos", Color.Cyan, onGen)
        Spacer(Modifier.height(8.dp))
        SidebarButton("💥 Limpar Canvas", Color.Red, onClr)
    }
}

@Composable
fun ModuleToggle(label: String, isEnabled: Boolean, onToggle: (Boolean) -> Unit, colors: CanvasColors) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = colors.textMain, fontSize = 14.sp)
        Switch(checked = isEnabled, onCheckedChange = onToggle, colors = SwitchDefaults.colors(checkedThumbColor = colors.accent))
    }
}

@Composable
fun SidebarButton(label: String, color: Color, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(backgroundColor = color.copy(0.1f)), elevation = ButtonDefaults.elevation(0.dp)) {
        Text(label, color = color, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
