package com.canvasstudio.ui.block.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.canvasstudio.designsystem.components.CanvasButton
import com.canvasstudio.designsystem.components.CanvasButtonVariant
import com.canvasstudio.ui.theme.CanvasColors

@Composable
fun SidebarActionsSection(
    onAddTextBlock: () -> Unit,
    onAddChartBlock: () -> Unit,
    onPickGalleryImage: () -> Unit,
    onAddImageUrlBlock: () -> Unit,
    onAutoOrganize: () -> Unit,
    onClearCanvas: () -> Unit,
    onImportJson: () -> Unit,
    onExportJson: () -> Unit,
    onExportPdf: () -> Unit,
    onSharePdf: () -> Unit,
    onExportToGallery: () -> Unit,
    colors: CanvasColors
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        TextSectionHeader("ADICIONAR BLOCOS", colors)
        CanvasButton("Novo Bloco de Texto", onAddTextBlock, variant = CanvasButtonVariant.Secondary, leadingIcon = Icons.Rounded.TextFields, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp))
        CanvasButton("Novo Gráfico Radar", onAddChartBlock, variant = CanvasButtonVariant.Secondary, leadingIcon = Icons.Rounded.PieChart, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp))
        CanvasButton("Inserir Imagem (Galeria)", onPickGalleryImage, variant = CanvasButtonVariant.Secondary, leadingIcon = Icons.Rounded.AddPhotoAlternate, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp))
        CanvasButton("Inserir Imagem (URL)", onAddImageUrlBlock, variant = CanvasButtonVariant.Secondary, leadingIcon = Icons.Rounded.Link, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp))

        Spacer(Modifier.height(12.dp))
        TextSectionHeader("ORGANIZAÇÃO & DADOS", colors)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CanvasButton("Organizar em Grade", onAutoOrganize, variant = CanvasButtonVariant.Secondary, leadingIcon = Icons.Rounded.GridOn, modifier = Modifier.weight(1f))
            CanvasButton("Limpar Tudo", onClearCanvas, variant = CanvasButtonVariant.Danger, leadingIcon = Icons.Rounded.DeleteSweep, modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CanvasButton("JSON Import", onImportJson, variant = CanvasButtonVariant.Outlined, leadingIcon = Icons.Rounded.FileDownload, modifier = Modifier.weight(1f))
            CanvasButton("JSON Export", onExportJson, variant = CanvasButtonVariant.Outlined, leadingIcon = Icons.Rounded.FileUpload, modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(12.dp))
        TextSectionHeader("EXPORTAÇÃO & PORTABILIDADE", colors)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CanvasButton("Exportar PDF", onExportPdf, variant = CanvasButtonVariant.Secondary, leadingIcon = Icons.Rounded.PictureAsPdf, modifier = Modifier.weight(1f))
            CanvasButton("Compartilhar PDF", onSharePdf, variant = CanvasButtonVariant.Secondary, leadingIcon = Icons.Rounded.Share, modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(8.dp))
        CanvasButton("Exportar para Galeria Web", onExportToGallery, variant = CanvasButtonVariant.Primary, leadingIcon = Icons.Rounded.CloudUpload, modifier = Modifier.fillMaxWidth())
    }
}
