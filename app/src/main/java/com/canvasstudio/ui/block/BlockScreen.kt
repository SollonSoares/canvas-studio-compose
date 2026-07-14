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

// Design System
val BgMain = Color(0xFF1C1C1E)
val BgMenu = Color(0xF22C2C2E)
val Accent = Color(0xFF0A84FF)
val CanvasGrid = Color(0xFF2C2C2E)
val TextMain = Color(0xFFF5F5F7)

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
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var editingBlock by remember { mutableStateOf<BlockEntity?>(null) }
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val appliedQuery by viewModel.appliedQuery.collectAsStateWithLifecycle()

    val brandTitle by viewModel.brandTitle.collectAsStateWithLifecycle()
    var showSettingsModal by remember { mutableStateOf(false) }

    // Launcher para selecionar arquivo JSON
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

    // Launcher para exportar arquivo JSON
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

    // Estado da Câmera Infinita
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
                title = { Text(brandTitle, color = TextMain, fontWeight = FontWeight.Bold) },
                backgroundColor = BgMenu,
                actions = {
                    IconButton(onClick = { showSettingsModal = true }) {
                        Icon(Icons.Default.Settings, "Configurações", tint = Accent)
                    }
                    IconButton(onClick = { scale = 1f; offset = Offset.Zero }) {
                        Icon(Icons.Default.FilterCenterFocus, "Resetar Visão", tint = Accent)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { scaffoldState.drawerState.open() } }) {
                        Icon(Icons.Default.Menu, "Menu", tint = Accent)
                    }
                }
            )
        },
        floatingActionButton = {
            val density = LocalDensity.current
            FloatingActionButton(
                onClick = {
                    // Adiciona o bloco na posição relativa ao centro da visão atual
                    // Convertendo pixels da câmera para DP para consistência no canvas
                    val spawnX = ((-offset.x / density.density) + 200f) / scale
                    val spawnY = ((-offset.y / density.density) + 200f) / scale
                    viewModel.insertBlock(BlockEntity(0, 0, "Novo Bloco", "text", spawnX, spawnY, 220, 180, 
                        "{\"text\":\"**Título**\\nEscreva aqui...\", \"titleSize\": 13, \"titleBold\": true, \"align\": \"left\"}"))
                },
                backgroundColor = Accent
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
                onToggleModule = { type, enabled -> viewModel.toggleModule(type, enabled) }
            ) 
        }
    ) { padding ->
        Box(
            Modifier.padding(padding).fillMaxSize().background(BgMain)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offset += dragAmount
                    }
                }
                .transformable(state = transformState)
        ) {
            CanvasBackground({ scale }, { offset })

            // MESA INFINITA (Apenas os blocos)
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
                                currentScale = { scale }
                            )
                        }
                    }
                }
            }

            // CAMADA DE INTERFACE SOBRE O CANVAS (Sempre visível/estática)
            if (uiState is BlockUiState.Success) {
                val blocks = (uiState as BlockUiState.Success).blocks
                
                // Feedback de Busca Ativa
                if (appliedQuery.isNotEmpty()) {
                    Column(
                        Modifier.align(Alignment.TopCenter).padding(top = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = Accent.copy(alpha = 0.9f),
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
                            Text("Nenhum bloco corresponde à sua busca.", color = TextMain.copy(0.4f), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            if (uiState is BlockUiState.Loading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center), color = Accent)
            }
            
            editingBlock?.let { block ->
                EditBlockDialog(block, { editingBlock = null }, { viewModel.updateBlock(it); editingBlock = null }, { viewModel.updateBlockLive(it) })
            }

            if (showSettingsModal) {
                val modules by viewModel.modulesState.collectAsState()
                SettingsModal(
                    title = brandTitle,
                    onTitleChange = { viewModel.setBrandTitle(it) },
                    modules = modules,
                    onToggleModule = { type, enabled -> viewModel.toggleModule(type, enabled) },
                    onDismiss = { showSettingsModal = false }
                )
            }
        }
    }
}

@Composable
fun SettingsModal(
    title: String,
    onTitleChange: (String) -> Unit,
    modules: Map<String, Boolean>,
    onToggleModule: (String, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        backgroundColor = BgMenu,
        shape = RoundedCornerShape(16.dp),
        title = { Text("Configurações do Projeto", color = TextMain, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text("NOME DO PROJETO", color = TextMain.copy(0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    TextField(
                        value = title,
                        onValueChange = onTitleChange,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = TextFieldDefaults.textFieldColors(
                            textColor = TextMain,
                            backgroundColor = Color.White.copy(0.05f),
                            cursorColor = Accent,
                            focusedIndicatorColor = Accent
                        )
                    )
                }

                Divider(color = Color.White.copy(0.05f))

                Column {
                    Text("MÓDULOS ATIVOS", color = TextMain.copy(0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    ModuleToggle("📝 Texto", modules["text"] ?: true) { onToggleModule("text", it) }
                    ModuleToggle("🖼️ Imagem", modules["image"] ?: true) { onToggleModule("image", it) }
                    ModuleToggle("📊 Gráfico", modules["chart"] ?: true) { onToggleModule("chart", it) }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(backgroundColor = Accent)) {
                Text("Concluído", color = Color.White)
            }
        }
    )
}

@Composable
fun CanvasBackground(scale: () -> Float, offset: () -> Offset) {
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
                drawCircle(CanvasGrid, dotRadius, Offset(x, y))
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
    onDelete: () -> Unit, onEdit: () -> Unit, currentScale: () -> Float
) {
    // Usar rememberUpdatedState para garantir que as lambdas e valores atuais sejam usados dentro do pointerInput
    val updatedOnMove by rememberUpdatedState(onMove)
    val updatedOnResize by rememberUpdatedState(onResize)
    val updatedScale by rememberUpdatedState(currentScale)

    var offsetX by remember(key) { mutableFloatStateOf(block.posX) }
    var offsetY by remember(key) { mutableFloatStateOf(block.posY) }
    var width by remember(key) { mutableIntStateOf(block.width) }
    var height by remember(key) { mutableIntStateOf(block.height) }

    val density = LocalDensity.current
    
    // Sincroniza o estado local caso o bloco mude externamente (ex: via diálogo de edição ou auto-organização)
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
            IntOffset(
                (offsetX * density.density).roundToInt(), 
                (offsetY * density.density).roundToInt()
            ) 
        }.size(width.dp, height.dp)
            .background(BgMenu.copy(alpha = 0.95f), RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
    ) {
        Column {
            Row(Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)).padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f).pointerInput(key) {
                    detectDragGestures(onDragEnd = { updatedOnMove(offsetX, offsetY) }) { change, drag ->
                        change.consume()
                        val s = updatedScale()
                        // Conversão de delta pixels para DP
                        offsetX += drag.x / (s * density.density)
                        offsetY += drag.y / (s * density.density)
                    }
                }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).background(Accent, CircleShape))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = block.title, 
                        color = TextMain, 
                        fontSize = (metadata?.get("titleSize")?.jsonPrimitive?.intOrNull ?: 13).sp, 
                        fontWeight = FontWeight.Bold, 
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Edit, null, tint = TextMain.copy(0.4f), modifier = Modifier.size(14.dp)) }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Close, null, tint = TextMain.copy(0.4f), modifier = Modifier.size(14.dp)) }
            }
            Box(Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 8.dp).clickable { onEdit() }) {
                when (block.type.lowercase()) {
                    "chart" -> com.canvasstudio.ui.block.modules.ChartBlock(block)
                    "image" -> ImageBlock(block)
                    else -> {
                        val text = remember(metadata, block.contentJson) {
                            metadata?.get("text")?.jsonPrimitive?.content ?: block.contentJson
                        }
                        val richText = remember(text) { parseRichText(text) }
                        
                        Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                            Text(
                                text = richText, 
                                color = TextMain.copy(0.8f), 
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
        }) { Canvas(Modifier.fillMaxSize()) { drawLine(Accent.copy(0.5f), Offset(size.width, 0f), Offset(0f, size.height), 2.dp.toPx()) } }
    }
}

@Composable
fun EditBlockDialog(block: BlockEntity, onDismiss: () -> Unit, onConfirm: (BlockEntity) -> Unit, onLiveUpdate: (BlockEntity) -> Unit) {
    var title by remember { mutableStateOf(block.title) }
    var currentType by remember { mutableStateOf(block.type) }
    val initialText = remember(block.id) {
        try {
            val json = Json.parseToJsonElement(block.contentJson).jsonObject
            json["text"]?.jsonPrimitive?.content ?: json["url"]?.jsonPrimitive?.content ?: block.contentJson
        } catch (e: Exception) { block.contentJson }
    }
    var textFieldValue by remember { mutableStateOf(TextFieldValue(initialText)) }
    var titleSize by remember { mutableIntStateOf(try { Json.parseToJsonElement(block.contentJson).jsonObject["titleSize"]?.jsonPrimitive?.intOrNull ?: 13 } catch(e: Exception) { 13 }) }
    var align by remember { mutableStateOf(try { Json.parseToJsonElement(block.contentJson).jsonObject["align"]?.jsonPrimitive?.content ?: "left" } catch(e: Exception) { "left" }) }
    var stats by remember { mutableStateOf(try {
        val json = Json.parseToJsonElement(block.contentJson).jsonObject
        listOf(
            json["ninjutsu"]?.jsonPrimitive?.floatOrNull ?: 4f,
            json["inteligencia"]?.jsonPrimitive?.floatOrNull ?: 4f,
            json["chakra"]?.jsonPrimitive?.floatOrNull ?: 4f,
            json["taijutsu"]?.jsonPrimitive?.floatOrNull ?: 4f,
            json["vigor"]?.jsonPrimitive?.floatOrNull ?: 4f,
            json["genjutsu"]?.jsonPrimitive?.floatOrNull ?: 4f
        )
    } catch (e: Exception) { listOf(4f, 4f, 4f, 4f, 4f, 4f) }) }

    LaunchedEffect(title, textFieldValue.text, align, titleSize, currentType, stats) {
        val json = when (currentType) {
            "text" -> buildJsonObject { put("text", textFieldValue.text); put("titleSize", titleSize); put("titleBold", true); put("align", align) }.toString()
            "image" -> buildJsonObject { put("url", textFieldValue.text) }.toString()
            "chart" -> buildJsonObject {
                put("ninjutsu", stats[0]); put("inteligencia", stats[1]); put("chakra", stats[2])
                put("taijutsu", stats[3]); put("vigor", stats[4]); put("genjutsu", stats[5])
            }.toString()
            else -> textFieldValue.text
        }
        onLiveUpdate(block.copy(title = title, type = currentType, contentJson = json))
    }

    AlertDialog(
        onDismissRequest = onDismiss, backgroundColor = BgMenu, shape = RoundedCornerShape(16.dp),
        title = { Text("Formatar Bloco", color = TextMain, fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // PRÉ-VISUALIZAÇÃO
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("PRÉ-VISUALIZAÇÃO", color = TextMain.copy(0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Box(Modifier.fillMaxWidth().heightIn(min = 80.dp, max = 180.dp).background(Color.Black.copy(0.3f), RoundedCornerShape(8.dp)).border(1.dp, Accent.copy(0.3f), RoundedCornerShape(8.dp)).padding(12.dp)) {
                        Column(Modifier.verticalScroll(rememberScrollState())) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(6.dp).background(Accent, CircleShape))
                                Spacer(Modifier.width(8.dp))
                                Text(text = title, color = TextMain, fontSize = titleSize.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                            Spacer(Modifier.height(8.dp))
                            if (currentType == "image") {
                                Box(Modifier.fillMaxWidth().height(60.dp).background(Color.White.copy(0.05f), RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
                                    if (textFieldValue.text.isNotEmpty()) AsyncImage(textFieldValue.text, null, Modifier.fillMaxSize().clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop)
                                    else Text("🖼️", fontSize = 20.sp)
                                }
                            } else if (currentType == "chart") {
                                Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                    com.canvasstudio.ui.block.modules.ChartBlock(block.copy(type = "chart", contentJson = buildJsonObject {
                                        put("ninjutsu", stats[0]); put("inteligencia", stats[1]); put("chakra", stats[2])
                                        put("taijutsu", stats[3]); put("vigor", stats[4]); put("genjutsu", stats[5])
                                    }.toString()))
                                }
                            } else {
                                Text(text = parseRichText(textFieldValue.text), color = TextMain.copy(0.8f), fontSize = 13.sp, modifier = Modifier.fillMaxWidth(),
                                     textAlign = when(align) { "center" -> TextAlign.Center; "right" -> TextAlign.End; else -> TextAlign.Start })
                            }
                        }
                    }
                }
                Divider(color = Color.White.copy(0.05f))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("text", "image", "chart").forEach { type ->
                        val sel = currentType == type
                        Box(Modifier.weight(1f).height(32.dp).clip(RoundedCornerShape(6.dp)).background(if (sel) Accent else Color.White.copy(0.05f)).clickable { currentType = type }, contentAlignment = Alignment.Center) {
                            Text(type.uppercase(), color = if (sel) Color.White else TextMain.copy(0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Título do Bloco", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    TextField(value = title, onValueChange = { title = it }, modifier = Modifier.fillMaxWidth(), colors = TextFieldDefaults.textFieldColors(textColor = TextMain, backgroundColor = Color.Black.copy(0.2f)))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Tam: $titleSize", color = TextMain, fontSize = 11.sp, modifier = Modifier.weight(1f))
                        IconButton(onClick = { if (titleSize > 8) titleSize-- }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Remove, null, tint = Accent) }
                        IconButton(onClick = { if (titleSize < 30) titleSize++ }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Add, null, tint = Accent) }
                    }
                }
                if (currentType == "text") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Editor Rich Text", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FormatAction(Icons.Default.FormatBold) { textFieldValue = insertTag(textFieldValue, "**", "**") }
                            FormatAction(Icons.Default.FormatItalic) { textFieldValue = insertTag(textFieldValue, "*", "*") }
                            FormatAction(Icons.Default.TextFields) { textFieldValue = insertTag(textFieldValue, "[size=20]", "[/size]") }
                            Spacer(Modifier.width(8.dp))
                            FormatAction(Icons.AutoMirrored.Filled.FormatAlignLeft, align == "left") { align = "left" }
                            FormatAction(Icons.Default.FormatAlignCenter, align == "center") { align = "center" }
                            FormatAction(Icons.AutoMirrored.Filled.FormatAlignRight, align == "right") { align = "right" }
                        }
                        TextField(value = textFieldValue, onValueChange = { textFieldValue = it }, modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp), colors = TextFieldDefaults.textFieldColors(textColor = TextMain, backgroundColor = Color.Black.copy(0.2f)))
                    }
                } else if (currentType == "image") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("URL da Imagem", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        TextField(value = textFieldValue, onValueChange = { textFieldValue = it }, modifier = Modifier.fillMaxWidth(), colors = TextFieldDefaults.textFieldColors(textColor = TextMain, backgroundColor = Color.Black.copy(0.2f)))
                    }
                } else if (currentType == "chart") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Atributos do Gráfico", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        val labels = listOf("Ninjutsu", "Inteligência", "Chakra", "Taijutsu", "Vigor", "Genjutsu")
                        labels.forEachIndexed { index, label ->
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(label, color = TextMain, fontSize = 11.sp, modifier = Modifier.weight(1f))
                                    Text(stats[index].toString(), color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = stats[index],
                                    onValueChange = { v -> stats = stats.toMutableList().apply { this[index] = (v * 10).roundToInt() / 10f } },
                                    valueRange = 0f..8f,
                                    colors = SliderDefaults.colors(thumbColor = Accent, activeTrackColor = Accent)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val json = when (currentType) {
                    "text" -> buildJsonObject { put("text", textFieldValue.text); put("titleSize", titleSize); put("titleBold", true); put("align", align) }.toString()
                    "image" -> buildJsonObject { put("url", textFieldValue.text) }.toString()
                    "chart" -> buildJsonObject {
                        put("ninjutsu", stats[0]); put("inteligencia", stats[1]); put("chakra", stats[2])
                        put("taijutsu", stats[3]); put("vigor", stats[4]); put("genjutsu", stats[5])
                    }.toString()
                    else -> textFieldValue.text
                }
                onConfirm(block.copy(title = title, type = currentType, contentJson = json))
            }, colors = ButtonDefaults.buttonColors(backgroundColor = Accent)) { Text("Salvar", color = Color.White) }
        }
    )
}

fun insertTag(value: TextFieldValue, prefix: String, suffix: String): TextFieldValue {
    val sel = value.selection
    val newText = value.text.replaceRange(sel.start, sel.end, "$prefix${value.text.substring(sel.start, sel.end)}$suffix")
    return TextFieldValue(newText, TextRange(sel.start + prefix.length + (sel.end - sel.start) + suffix.length))
}

@Composable
fun FormatAction(icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean = false, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(32.dp).clip(RoundedCornerShape(4.dp)).background(if (isSelected) Accent.copy(0.2f) else Color.White.copy(0.05f))) {
        Icon(icon, null, tint = if (isSelected) Accent else TextMain, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun ImageBlock(block: BlockEntity) {
    val url = remember(block.contentJson) {
        try { Json.parseToJsonElement(block.contentJson).jsonObject["url"]?.jsonPrimitive?.content ?: "" } catch(e: Exception) { "" }
    }
    Box(Modifier.fillMaxSize().background(Color.Black.copy(0.1f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
        if (url.isNotEmpty()) {
            AsyncImage(
                model = url, 
                contentDescription = null, 
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)), 
                contentScale = ContentScale.Fit // Usamos Fit para garantir que a imagem seja vista por inteiro se preferir, ou Crop para manter o design preenchido. 
                // Mudando para Fit para facilitar a validação de redimensionamento sem "esconder" partes da imagem.
            )
        } else {
            Icon(Icons.Default.Image, null, tint = TextMain.copy(0.2f), modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun SidebarContent(
    q: String, 
    onQ: (String) -> Unit, 
    onSearch: () -> Unit, 
    onImp: () -> Unit, 
    onExp: () -> Unit, 
    onClr: () -> Unit, 
    onGen: () -> Unit,
    onOrg: () -> Unit,
    modules: Map<String, Boolean>,
    onToggleModule: (String, Boolean) -> Unit
) {
    Column(Modifier.fillMaxSize().background(BgMenu).padding(24.dp).verticalScroll(rememberScrollState())) {
        TextField(
            value = q,
            onValueChange = onQ,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar no canvas...", color = TextMain.copy(0.3f), fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = Accent, modifier = Modifier.size(20.dp)) },
            trailingIcon = {
                if (q.isNotEmpty()) {
                    IconButton(onClick = { onQ("") }) {
                        Icon(Icons.Default.Clear, null, tint = TextMain.copy(0.4f), modifier = Modifier.size(18.dp))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            colors = TextFieldDefaults.textFieldColors(
                textColor = TextMain,
                backgroundColor = Color.White.copy(0.05f),
                cursorColor = Accent,
                focusedIndicatorColor = Accent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )

        Spacer(Modifier.height(24.dp))

        SidebarButton("📥 Importar JSON", Accent, onImp)
        Spacer(Modifier.height(8.dp))
        SidebarButton("📤 Exportar JSON", Accent, onExp)
        Spacer(Modifier.height(8.dp))
        SidebarButton("🧩 Auto Organizar", Accent, onOrg)
        Spacer(Modifier.height(8.dp))
        SidebarButton("🧪 Gerar 50 Blocos (Teste)", Color.Cyan, onGen)
        Spacer(Modifier.height(8.dp))
        SidebarButton("💥 Limpar Canvas", Color.Red, onClr)
    }
}

@Composable
fun ModuleToggle(label: String, isEnabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clickable { onToggle(!isEnabled) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isEnabled,
            onCheckedChange = onToggle,
            colors = CheckboxDefaults.colors(
                checkedColor = Accent,
                uncheckedColor = TextMain.copy(0.3f),
                checkmarkColor = Color.White
            )
        )
        Text(label, color = if (isEnabled) TextMain else TextMain.copy(0.5f), fontSize = 14.sp)
    }
}

@Composable
fun SidebarButton(t: String, c: Color, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(8.dp)).background(c.copy(0.1f)).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Text(t, color = c, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}
