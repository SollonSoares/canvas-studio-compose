package com.canvasstudio.ui.block.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import com.canvasstudio.data.local.entity.BlockEntity
import com.canvasstudio.ui.block.BlockUiState
import com.canvasstudio.ui.block.BlockViewModel
import com.canvasstudio.ui.block.utils.PdfExporter
import com.canvasstudio.ui.theme.CanvasColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun BlockDrawerWrapper(
    viewModel: BlockViewModel,
    uiState: BlockUiState,
    searchQuery: String,
    brandTitle: String,
    isDarkMode: Boolean,
    isGridEnabled: Boolean,
    isLocked: Boolean,
    canvasDimensions: Pair<Int, Int>,
    selectedBlock: BlockEntity?,
    offset: Offset,
    scale: Float,
    density: Density,
    context: Context,
    colors: CanvasColors,
    scope: CoroutineScope,
    onCloseDrawer: () -> Unit,
    onShowSettings: () -> Unit,
    onLaunchImport: () -> Unit,
    onLaunchExport: () -> Unit,
    onLaunchPdfExport: () -> Unit,
    onLaunchGalleryPicker: () -> Unit
) {
    SidebarContent(
        q = searchQuery,
        onQ = { viewModel.setSearchQuery(it) },
        onSearch = { viewModel.applySearch(); onCloseDrawer() },
        onImp = onLaunchImport,
        onExp = onLaunchExport,
        onClr = { viewModel.clearCanvas(); onCloseDrawer() },
        onOrg = { viewModel.autoOrganizeBlocks(); onCloseDrawer() },
        isDarkMode = isDarkMode,
        onToggleTheme = { viewModel.toggleDarkMode() },
        isGridEnabled = isGridEnabled,
        onToggleGrid = { viewModel.toggleGrid() },
        isLocked = isLocked,
        onToggleLock = { viewModel.toggleLock() },
        onShowSettings = onShowSettings,
        onAddTextBlock = {
            val sX = ((-offset.x / density.density) + 100f) / scale
            val sY = ((-offset.y / density.density) + 100f) / scale
            viewModel.insertBlock(BlockEntity(0, 0, "Novo Bloco", "text", sX, sY, 220, 180, "{\"text\":\"Novo texto aqui...\", \"fontSize\": 13, \"align\": \"left\"}"))
            onCloseDrawer()
        },
        onAddChartBlock = {
            val sX = ((-offset.x / density.density) + 100f) / scale
            val sY = ((-offset.y / density.density) + 100f) / scale
            viewModel.insertBlock(BlockEntity(0, 0, "Radar Chart", "chart", sX, sY, 300, 300, "{\"ninjutsu\":5, \"inteligencia\":5, \"chakra\":5, \"taijutsu\":5, \"vigor\":5, \"genjutsu\":5}"))
            onCloseDrawer()
        },
        onExportPdf = onLaunchPdfExport,
        onSharePdf = {
            if (uiState is BlockUiState.Success) {
                val (w, h) = canvasDimensions
                PdfExporter.sharePdf(context, uiState.blocks, w, h, "${brandTitle.replace(" ", "_")}_export.pdf")
            }
            onCloseDrawer()
        },
        onPickGalleryImage = onLaunchGalleryPicker,
        onAddImageUrlBlock = {
            val sX = ((-offset.x / density.density) + 100f) / scale
            val sY = ((-offset.y / density.density) + 100f) / scale
            viewModel.insertBlock(BlockEntity(0, 0, "Imagem", "image", sX, sY, 250, 250, "{\"url\":\"\"}"))
            onCloseDrawer()
        },
        onExportToGallery = {
            viewModel.syncToGallery(context)
            onCloseDrawer()
        },
        selectedBlock = selectedBlock,
        onDeselectBlock = { viewModel.selectBlock(null) },
        onUpdateTitle = { viewModel.updateSelectedTitle(it) },
        onUpdateContentText = { if (selectedBlock != null) viewModel.updateBlockContentText(selectedBlock, it) },
        onUpdateTextFormatting = { sz, b, it, al, col -> viewModel.updateSelectedFormatting(sz, b, it, al, col) },
        onUpdateChartAttribute = { attr, value -> viewModel.updateSelectedChartAttribute(attr, value) },
        onUpdateImageUrl = { url -> viewModel.updateSelectedImageUrl(url) },
        onUpdateValue = { valNum -> viewModel.updateSelectedValue(valNum) },
        onUpdateRealizadoEm = { dateStr -> viewModel.updateSelectedRealizadoEm(dateStr) },
        onUpdatePagador = { pag -> viewModel.updateSelectedPagador(pag) },
        onUpdateDestinatario = { dest -> viewModel.updateSelectedDestinatario(dest) },
        onUpdateInstituicao = { inst -> viewModel.updateSelectedInstituicao(inst) },
        onDuplicateBlock = { viewModel.duplicateBlock(it) },
        onDeleteBlock = { viewModel.deleteBlock(it) },
        colors = colors
    )
}
