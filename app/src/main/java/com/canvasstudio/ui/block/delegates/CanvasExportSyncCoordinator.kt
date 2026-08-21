package com.canvasstudio.ui.block.delegates

import android.content.Context
import com.canvasstudio.data.local.entity.BlockEntity
import com.canvasstudio.data.repository.BlockRepository
import com.canvasstudio.features.export_portability.GallerySyncService
import com.canvasstudio.features.export_portability.JsonPortabilityService
import java.io.File

object CanvasExportSyncCoordinator {

    suspend fun syncToGallery(
        context: Context,
        blocks: List<BlockEntity>,
        galleryBaseUrl: String,
        githubToken: String,
        blockRepository: BlockRepository,
        onShareZip: (File) -> Unit,
        onEmitEvent: suspend (String) -> Unit
    ) {
        if (blocks.none { it.type.equals("image", ignoreCase = true) }) {
            onEmitEvent("Nenhuma imagem encontrada no Canvas para sincronizar.")
            return
        }
        val syncService = GallerySyncService(context)
        val result = syncService.syncBlocksToGallery(blocks, galleryBaseUrl, githubToken)
        if (result.syncedCount > 0) {
            result.updatedBlocks.forEach { blockRepository.updateBlock(it) }
            val msg = if (result.githubUploadedCount > 0) "🚀 ${result.githubUploadedCount} imagem(ns) enviadas para o GitHub!" else "${result.syncedCount} imagem(ns) preparadas!"
            onEmitEvent(msg)
            result.zipFile?.let { onShareZip(it) }
        } else {
            onEmitEvent("Todas as imagens do Canvas já possuem links públicos da Galeria!")
        }
    }

    suspend fun importFromJson(
        jsonString: String,
        projectId: Long,
        clearFirst: Boolean,
        blockRepository: BlockRepository,
        onSetBrand: (String) -> Unit,
        onEmitEvent: suspend (String) -> Unit
    ) {
        if (jsonString.isBlank()) return
        try {
            val (brand, newBlocks) = JsonPortabilityService.parseBlocksFromJson(jsonString, projectId)
            brand?.let { onSetBrand(it) }
            if (newBlocks.isNotEmpty()) {
                if (clearFirst) blockRepository.clearCanvas(projectId)
                blockRepository.insertBlocks(newBlocks)
                onEmitEvent("${newBlocks.size} blocos carregados com sucesso!")
            }
        } catch (e: Exception) {
            onEmitEvent("Erro na importação: Estrutura JSON inválida.")
        }
    }
}
