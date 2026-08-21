package com.canvasstudio.ui.block

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.canvasstudio.designsystem.CanvasTheme
import com.canvasstudio.ui.block.components.*
import com.canvasstudio.ui.block.utils.PdfExporter
import kotlinx.coroutines.launch

@Composable
fun BlockScreen(
    viewModel: BlockViewModel,
    uiState: BlockUiState = viewModel.uiState.collectAsStateWithLifecycle().value,
    onBack: () -> Unit = {}
) {
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val isLocked by viewModel.isLocked.collectAsStateWithLifecycle()
    val isGridEnabled by viewModel.isGridEnabled.collectAsStateWithLifecycle()
    val selectedBlockId by viewModel.selectedBlockId.collectAsStateWithLifecycle()
    val brandTitle by viewModel.brandTitle.collectAsStateWithLifecycle()
    val canvasDimensions by viewModel.canvasDimensions.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val appliedQuery by viewModel.appliedQuery.collectAsStateWithLifecycle()
    val themeStyle by viewModel.themeStyle.collectAsStateWithLifecycle()

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var showSettingsModal by remember { mutableStateOf(false) }

    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val density = LocalDensity.current
    val colors = CanvasTheme.colors

    val jsonImportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                val json = context.contentResolver.openInputStream(it)?.bufferedReader()?.use { reader -> reader.readText() }
                if (json != null) viewModel.importFromJson(json)
            } catch (e: Exception) {}
        }
    }
    val jsonExportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
        uri?.let {
            try {
                val json = viewModel.exportToJson()
                context.contentResolver.openOutputStream(it)?.use { out -> out.write(json.toByteArray()) }
            } catch (e: Exception) {}
        }
    }
    val pdfExportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri: Uri? ->
        uri?.let { destUri ->
            if (uiState is BlockUiState.Success) {
                try {
                    context.contentResolver.openOutputStream(destUri)?.use { out ->
                        val (w, h) = canvasDimensions
                        PdfExporter.exportCanvasToPdf(context, uiState.blocks, w, h, out)
                    }
                } catch (e: Exception) {}
            }
        }
    }
    val galleryPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.importSharedUri(context, it) }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { msg ->
            scaffoldState.snackbarHostState.showSnackbar(msg)
        }
    }

    val selectedBlock = remember(uiState, selectedBlockId) {
        (uiState as? BlockUiState.Success)?.blocks?.find { it.id == selectedBlockId }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            BlockScreenTopBar(
                brandTitle = brandTitle,
                onOpenDrawer = { scope.launch { scaffoldState.drawerState.open() } },
                onOpenSettings = { showSettingsModal = true },
                onResetVision = { scale = 1f; offset = Offset.Zero },
                colors = colors
            )
        },
        drawerContent = {
            BlockDrawerWrapper(
                viewModel = viewModel,
                uiState = uiState,
                searchQuery = searchQuery,
                brandTitle = brandTitle,
                isDarkMode = isDarkMode,
                isGridEnabled = isGridEnabled,
                isLocked = isLocked,
                canvasDimensions = canvasDimensions,
                selectedBlock = selectedBlock,
                scale = scale,
                offset = offset,
                density = density,
                context = context,
                colors = colors,
                scope = scope,
                onCloseDrawer = { scope.launch { scaffoldState.drawerState.close() } },
                onShowSettings = {
                    showSettingsModal = true
                    scope.launch { scaffoldState.drawerState.close() }
                },
                onLaunchImport = { jsonImportLauncher.launch("application/json"); scope.launch { scaffoldState.drawerState.close() } },
                onLaunchExport = { jsonExportLauncher.launch("${brandTitle.replace(" ", "_")}_backup.json"); scope.launch { scaffoldState.drawerState.close() } },
                onLaunchPdfExport = { pdfExportLauncher.launch("${brandTitle.replace(" ", "_")}_export.pdf"); scope.launch { scaffoldState.drawerState.close() } },
                onLaunchGalleryPicker = { galleryPickerLauncher.launch("image/*"); scope.launch { scaffoldState.drawerState.close() } }
            )
        }
    ) { padding ->
        Box(
            Modifier.padding(padding).fillMaxSize().background(colors.bgMain)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan: Offset, zoom: Float, _ ->
                        scale = (scale * zoom).coerceIn(0.15f, 3f)
                        offset += pan
                    }
                }
        ) {
            if (isGridEnabled) CanvasBackground(scale = { scale }, offset = { offset }, gridColor = colors.canvasGrid)

            Box(Modifier.fillMaxSize().graphicsLayer { scaleX = scale; scaleY = scale; translationX = offset.x; translationY = offset.y }) {
                if (uiState is BlockUiState.Success) {
                    uiState.blocks.forEach { block ->
                        key(block.id) {
                            DraggableBlock(
                                key = block.id, block = block,
                                onMove = { x, y -> if (!isLocked) viewModel.updateBlockLive(block.copy(posX = x.coerceAtLeast(0f), posY = y.coerceAtLeast(0f))) },
                                onResize = { w, h -> if (!isLocked) viewModel.updateBlockLive(block.copy(width = w.coerceIn(100, 3000), height = h.coerceIn(80, 3000))) },
                                onDelete = { if (!isLocked) viewModel.deleteBlock(block) },
                                onSelect = { viewModel.selectBlock(block) },
                                isSelected = block.id == selectedBlockId,
                                colors = colors, viewModel = viewModel
                            )
                        }
                    }
                }
            }

            if (uiState is BlockUiState.Success) BlockFilterPill(appliedQuery, uiState.blocks.isEmpty(), { viewModel.setSearchQuery(""); viewModel.applySearch() }, colors)
            if (uiState is BlockUiState.Loading) CircularProgressIndicator(Modifier.align(Alignment.Center), color = colors.accent)

            if (showSettingsModal) {
                val modules by viewModel.modulesState.collectAsState()
                val canvasConfig by viewModel.canvasConfig.collectAsState()
                val galleryBaseUrl by viewModel.galleryBaseUrl.collectAsStateWithLifecycle()
                val githubToken by viewModel.githubToken.collectAsStateWithLifecycle()
                SettingsModal(
                    title = brandTitle, onTitleChange = { viewModel.setBrandTitle(it) },
                    themeStyle = themeStyle, onThemeStyleChange = { viewModel.setThemeStyle(it) },
                    galleryBaseUrl = galleryBaseUrl, onGalleryBaseUrlChange = { viewModel.setGalleryBaseUrl(it) },
                    githubToken = githubToken, onGithubTokenChange = { viewModel.setGithubToken(it) },
                    modules = modules, onToggleModule = { type, enabled -> viewModel.toggleModule(type, enabled) },
                    config = canvasConfig, onToggleAutoOcr = { viewModel.toggleAutoOcr(it) },
                    onToggleFinancialBadges = { viewModel.toggleFinancialBadges(it) },
                    onTogglePartyDetails = { viewModel.togglePartyDetails(it) },
                    onToggleFitAspectRatio = { viewModel.toggleFitAspectRatio(it) },
                    onDismiss = { showSettingsModal = false }, colors = colors
                )
            }

            BlockFloatingZoomBar(scale = scale, onScaleChange = { scale = it }, onResetVision = { scale = 1f; offset = Offset.Zero }, colors = colors)
        }
    }
}
