package com.canvasstudio.ui.block.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.canvasstudio.data.local.entity.BlockEntity
import com.canvasstudio.ui.theme.CanvasColors

@Composable
fun SidebarContent(
    q: String,
    onQ: (String) -> Unit,
    onSearch: () -> Unit,
    onImp: () -> Unit,
    onExp: () -> Unit,
    onClr: () -> Unit,
    onOrg: () -> Unit,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    isGridEnabled: Boolean,
    onToggleGrid: () -> Unit,
    isLocked: Boolean,
    onToggleLock: () -> Unit,
    onShowSettings: () -> Unit,
    onAddTextBlock: () -> Unit,
    onAddChartBlock: () -> Unit,
    onPickGalleryImage: () -> Unit,
    onAddImageUrlBlock: () -> Unit,
    onExportPdf: () -> Unit,
    onSharePdf: () -> Unit,
    onExportToGallery: () -> Unit,
    selectedBlock: BlockEntity?,
    onDeselectBlock: () -> Unit,
    onUpdateTitle: (String) -> Unit,
    onUpdateContentText: (String) -> Unit,
    onUpdateTextFormatting: (Int?, Boolean?, Boolean?, String?, String?) -> Unit,
    onInsertTable: () -> Unit = {},
    onInsertCallout: () -> Unit = {},
    onInsertCollapsible: () -> Unit = {},
    onInsertList: () -> Unit = {},
    onUpdateChartAttribute: (String, Float) -> Unit,
    onUpdateImageUrl: (String) -> Unit,
    onUpdateValue: (Float?) -> Unit,
    onUpdateRealizadoEm: (String) -> Unit,
    onUpdatePagador: (String) -> Unit,
    onUpdateDestinatario: (String) -> Unit,
    onUpdateInstituicao: (String) -> Unit,
    onDuplicateBlock: (BlockEntity) -> Unit,
    onDeleteBlock: (BlockEntity) -> Unit,
    colors: CanvasColors
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(320.dp)
            .background(colors.bgMenu)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp)
    ) {
        SidebarSearchHeader(
            query = q,
            onQueryChange = onQ,
            onSearch = onSearch,
            colors = colors
        )

        Divider(color = colors.borderSubtle, modifier = Modifier.padding(vertical = 4.dp))

        if (selectedBlock != null) {
            SidebarBlockInspector(
                block = selectedBlock,
                onDeselect = onDeselectBlock,
                onUpdateTitle = onUpdateTitle,
                onUpdateContentText = onUpdateContentText,
                onUpdateTextFormatting = onUpdateTextFormatting,
                onInsertTable = onInsertTable,
                onInsertCallout = onInsertCallout,
                onInsertCollapsible = onInsertCollapsible,
                onInsertList = onInsertList,
                onUpdateChartAttribute = onUpdateChartAttribute,
                onUpdateImageUrl = onUpdateImageUrl,
                onUpdateValue = onUpdateValue,
                onUpdateRealizadoEm = onUpdateRealizadoEm,
                onUpdatePagador = onUpdatePagador,
                onUpdateDestinatario = onUpdateDestinatario,
                onUpdateInstituicao = onUpdateInstituicao,
                onDuplicateBlock = onDuplicateBlock,
                onDeleteBlock = onDeleteBlock,
                colors = colors
            )
            Divider(color = colors.borderSubtle, modifier = Modifier.padding(vertical = 4.dp))
        }

        SidebarActionsSection(
            onAddTextBlock = onAddTextBlock,
            onAddChartBlock = onAddChartBlock,
            onPickGalleryImage = onPickGalleryImage,
            onAddImageUrlBlock = onAddImageUrlBlock,
            onAutoOrganize = onOrg,
            onClearCanvas = onClr,
            onImportJson = onImp,
            onExportJson = onExp,
            onExportPdf = onExportPdf,
            onSharePdf = onSharePdf,
            onExportToGallery = onExportToGallery,
            colors = colors
        )

        Divider(color = colors.borderSubtle, modifier = Modifier.padding(vertical = 4.dp))

        SidebarModulesSection(
            isDarkMode = isDarkMode,
            onToggleTheme = onToggleTheme,
            isGridEnabled = isGridEnabled,
            onToggleGrid = onToggleGrid,
            isLocked = isLocked,
            onToggleLock = onToggleLock,
            onShowSettings = onShowSettings,
            colors = colors
        )
    }
}
