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
            viewModel.insertBlock(
                BlockEntity(
                    id = 0,
                    projectId = 0,
                    title = "STATUS SHINOBI",
                    type = "chart",
                    posX = sX,
                    posY = sY,
                    width = 280,
                    height = 360,
                    contentJson = """{"ninjutsu":0,"inteligencia":0,"chakraMax":6,"taijutsu":0,"vigor":0,"genjutsu":0}"""
                )
            )
            onCloseDrawer()
        },
        onExportPdf = onLaunchPdfExport,
        onSharePdf = {
            viewModel.sharePdf(context, "${brandTitle.replace(" ", "_")}_export.pdf")
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
        onUpdateTitle = { title ->
            if (selectedBlock != null) viewModel.updateBlockLive(selectedBlock.copy(title = title))
        },
        onUpdateContentText = { if (selectedBlock != null) viewModel.updateBlockContentText(selectedBlock, it) },
        onUpdateTextFormatting = { sz, b, it, al, col ->
            if (selectedBlock != null) viewModel.updateBlock(com.canvasstudio.ui.block.delegates.BlockPropertyUpdater.updateFormatting(selectedBlock, sz, b, it, al, col))
        },
        onInsertTable = { if (selectedBlock != null) viewModel.insertTable(selectedBlock) },
        onInsertCallout = { if (selectedBlock != null) viewModel.insertCallout(selectedBlock) },
        onInsertCollapsible = { if (selectedBlock != null) viewModel.insertCollapsible(selectedBlock) },
        onInsertList = { if (selectedBlock != null) viewModel.insertList(selectedBlock) },
        onUpdateChartAttribute = { attr, value ->
            if (selectedBlock != null) viewModel.updateBlockLive(com.canvasstudio.ui.block.delegates.BlockPropertyUpdater.updateChartAttribute(selectedBlock, attr, value))
        },
        onUpdateImageUrl = { url ->
            if (selectedBlock != null) viewModel.updateBlock(com.canvasstudio.ui.block.delegates.BlockPropertyUpdater.updateImageUrl(selectedBlock, url))
        },
        onUpdateValue = { valNum ->
            if (selectedBlock != null) viewModel.updateBlock(com.canvasstudio.ui.block.delegates.BlockPropertyUpdater.updateValue(selectedBlock, valNum))
        },
        onUpdateRealizadoEm = { dateStr ->
            if (selectedBlock != null) viewModel.updateBlock(com.canvasstudio.ui.block.delegates.BlockPropertyUpdater.updateRealizadoEm(selectedBlock, dateStr))
        },
        onUpdatePagador = { pag ->
            if (selectedBlock != null) viewModel.updateBlock(com.canvasstudio.ui.block.delegates.BlockPropertyUpdater.updatePartyField(selectedBlock, "pagador", pag))
        },
        onUpdateDestinatario = { dest ->
            if (selectedBlock != null) viewModel.updateBlock(com.canvasstudio.ui.block.delegates.BlockPropertyUpdater.updatePartyField(selectedBlock, "destinatario", dest))
        },
        onUpdateInstituicao = { inst ->
            if (selectedBlock != null) viewModel.updateBlock(com.canvasstudio.ui.block.delegates.BlockPropertyUpdater.updatePartyField(selectedBlock, "instituicao", inst))
        },
        onDuplicateBlock = { viewModel.duplicateBlock(it) },
        onDeleteBlock = { viewModel.deleteBlock(it) },
        colors = colors
    )
}
