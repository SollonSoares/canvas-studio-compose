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
            onEmitEvent("ℹ️ Nenhuma imagem encontrada no Canvas para exportar ou alterar links.")
            return
        }
        val syncService = GallerySyncService(context)
        val result = syncService.syncBlocksToGallery(blocks, galleryBaseUrl, githubToken)
        if (result.syncedCount > 0) {
            result.updatedBlocks.forEach { blockRepository.updateBlock(it) }
            val msg = buildString {
                if (githubToken.isNotBlank()) {
                    if (result.githubUploadedCount == result.syncedCount) {
                        append("✅ Sucesso: ${result.syncedCount} imagem(ns) exportadas, enviadas ao GitHub e links alterados no Canvas!")
                    } else if (result.githubUploadedCount > 0) {
                        append("⚠️ ${result.syncedCount} imagem(ns) exportadas e links alterados, com ${result.githubUploadedCount}/${result.syncedCount} enviadas ao GitHub.")
                    } else {
                        append("⚠️ ${result.syncedCount} imagem(ns) exportadas e links alterados no Canvas, mas falhou o upload no GitHub.")
                    }
                } else {
                    append("✅ Sucesso: ${result.syncedCount} imagem(ns) exportadas e links alterados com sucesso para a Galeria Web!")
                }
            }
            onEmitEvent(msg)
            result.zipFile?.let { onShareZip(it) }
        } else {
            if (result.errorMessages.isNotEmpty()) {
                onEmitEvent("❌ Falha ao exportar imagens: ${result.errorMessages.first()}")
            } else {
                onEmitEvent("ℹ️ Todas as imagens do Canvas já possuem links públicos da Galeria!")
            }
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
