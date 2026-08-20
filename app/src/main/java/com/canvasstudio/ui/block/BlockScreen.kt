package com.canvasstudio.ui.block

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.canvasstudio.data.local.entity.BlockEntity
import com.canvasstudio.ui.block.components.*
import com.canvasstudio.ui.block.dialogs.EditBlockDialog
import com.canvasstudio.ui.block.utils.PdfExporter
import com.canvasstudio.ui.theme.CanvasTheme
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun BlockScreen(uiState: BlockUiState, viewModel: BlockViewModel, onBack: () -> Unit) {
    val colors = CanvasTheme.colors
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var editingBlock by remember { mutableStateOf<BlockEntity?>(null) }
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val appliedQuery by viewModel.appliedQuery.collectAsStateWithLifecycle()

    val brandTitle by viewModel.brandTitle.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val isGridEnabled by viewModel.isGridEnabled.collectAsStateWithLifecycle()
    val isLocked by viewModel.isLocked.collectAsStateWithLifecycle()
    val canvasDimensions by viewModel.canvasDimensions.collectAsStateWithLifecycle()
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

    val pdfExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        uri?.let {
            try {
                if (uiState is BlockUiState.Success) {
                    val blocks = uiState.blocks
                    val (w, h) = canvasDimensions
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        PdfExporter.exportCanvasToPdf(context, blocks, w, h, outputStream)
                    }
                    scope.launch {
                        scaffoldState.snackbarHostState.showSnackbar("PDF exportado com sucesso!")
                    }
                }
            } catch (e: Exception) {
                scope.launch {
                    scaffoldState.snackbarHostState.showSnackbar("Erro ao exportar PDF: ${e.message}")
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
            val density = LocalDensity.current
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
                isGridEnabled = isGridEnabled,
                onToggleGrid = { viewModel.toggleGrid() },
                isLocked = isLocked,
                onToggleLock = { viewModel.toggleLock() },
                onShowSettings = { 
                    showSettingsModal = true
                    scope.launch { scaffoldState.drawerState.close() }
                },
                onAddTextBlock = {
                    val spawnX = ((-offset.x / density.density) + 100f) / scale
                    val spawnY = ((-offset.y / density.density) + 100f) / scale
                    viewModel.insertBlock(BlockEntity(0, 0, "Novo Bloco", "text", spawnX, spawnY, 220, 180, 
                        "{\"text\":\"**Título**\\nEscreva aqui...\", \"titleSize\": 13, \"align\": \"left\"}"))
                    scope.launch { scaffoldState.drawerState.close() }
                },
                onAddChartBlock = {
                    val spawnX = ((-offset.x / density.density) + 100f) / scale
                    val spawnY = ((-offset.y / density.density) + 100f) / scale
                    viewModel.insertBlock(BlockEntity(0, 0, "Radar Chart", "chart", spawnX, spawnY, 300, 300, 
                        "{\"ninjutsu\":5, \"inteligencia\":5, \"chakra\":5, \"taijutsu\":5, \"vigor\":5, \"genjutsu\":5}"))
                    scope.launch { scaffoldState.drawerState.close() }
                },
                onAddImageBlock = {
                    val spawnX = ((-offset.x / density.density) + 100f) / scale
                    val spawnY = ((-offset.y / density.density) + 100f) / scale
                    viewModel.insertBlock(BlockEntity(0, 0, "Imagem", "image", spawnX, spawnY, 250, 250, 
                        "{\"url\":\"\"}"))
                    scope.launch { scaffoldState.drawerState.close() }
                },
                onExportPdf = {
                    pdfExportLauncher.launch("${brandTitle.replace(" ", "_")}_export.pdf")
                    scope.launch { scaffoldState.drawerState.close() }
                },
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
            val (canvasWidth, canvasHeight) = canvasDimensions
            
            Box(Modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y
            }) {
                Surface(
                    modifier = Modifier.size(canvasWidth.dp, canvasHeight.dp),
                    color = colors.bgMain,
                    elevation = 2.dp,
                    border = BorderStroke(1.dp, colors.canvasGrid)
                ) {
                    if (isGridEnabled) {
                        CanvasBackground({ 1f }, { Offset.Zero }, colors.canvasGrid)
                    }
                }
            }

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
                                { x, y -> 
                                    if (!isLocked) {
                                        val boundedX = x.coerceIn(0f, canvasWidth.toFloat() - block.width)
                                        val boundedY = y.coerceIn(0f, canvasHeight.toFloat() - block.height)
                                        viewModel.updateBlockLive(block.copy(posX = boundedX, posY = boundedY))
                                    }
                                },
                                { w, h -> 
                                    if (!isLocked) {
                                        val boundedW = w.coerceIn(100, canvasWidth - block.posX.toInt())
                                        val boundedH = h.coerceIn(80, canvasHeight - block.posY.toInt())
                                        viewModel.updateBlockLive(block.copy(width = boundedW, height = boundedH))
                                    }
                                },
                                { if (!isLocked) viewModel.deleteBlock(block) },
                                { editingBlock = block },
                                currentScale = { scale },
                                colors = colors,
                                viewModel = viewModel
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
                    canvasDimensions = canvasDimensions,
                    onDimensionsChange = { w, h -> viewModel.setCanvasDimensions(w, h) },
                    modules = modules,
                    onToggleModule = { type, enabled -> viewModel.toggleModule(type, enabled) },
                    onDismiss = { showSettingsModal = false },
                    colors = colors
                )
            }
        }
    }
}
