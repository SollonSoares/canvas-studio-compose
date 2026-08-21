package com.canvasstudio.ui.block.delegates

import android.content.Context
import android.net.Uri
import com.canvasstudio.data.local.entity.BlockEntity
import com.canvasstudio.domain.service.SharedMediaImporter
import com.canvasstudio.ui.block.utils.ReceiptAnalyzer
import kotlinx.serialization.json.*
import java.io.File
import java.io.FileOutputStream
import java.net.URL

object CanvasMediaCoordinator {

    suspend fun importMedia(
        context: Context,
        uri: Uri,
        autoOcrEnabled: Boolean,
        spawnX: Float,
        spawnY: Float,
        projectId: Long
    ): Pair<BlockEntity, String> {
        val importer = SharedMediaImporter(context)
        val result = importer.importMedia(uri, autoOcrEnabled = autoOcrEnabled)

        val block = if (result.savedFileUri != null) {
            val content = buildJsonObject {
                put("url", result.savedFileUri)
                if (result.analysis.value != null) put("valor", result.analysis.value)
                if (result.analysis.valueFormatted != null) put("valorFormatted", result.analysis.valueFormatted)
                put("realizadoEm", result.analysis.realizadoEm)
                if (result.analysis.isPix) put("isPix", true)
                if (result.analysis.pagador != null) put("pagador", result.analysis.pagador)
                if (result.analysis.destinatario != null) put("destinatario", result.analysis.destinatario)
                if (result.analysis.instituicao != null) put("instituicao", result.analysis.instituicao)
                if (result.analysis.rawText.isNotBlank()) put("rawText", result.analysis.rawText)
            }.toString()
            BlockEntity(
                projectId = projectId,
                title = result.analysis.title,
                type = "image",
                posX = spawnX,
                posY = spawnY,
                width = result.blockWidth,
                height = result.blockHeight,
                contentJson = content
            )
        } else {
            val deStr = if (result.analysis.pagador != null) "\nDe (Pagador): ${result.analysis.pagador}" else ""
            val paraStr = if (result.analysis.destinatario != null) "\nPara (Destinatário): ${result.analysis.destinatario}" else ""
            val instStr = if (result.analysis.instituicao != null) "\nInstituição: ${result.analysis.instituicao}" else ""
            val content = buildJsonObject {
                if (result.analysis.value != null) put("valor", result.analysis.value)
                if (result.analysis.valueFormatted != null) put("valorFormatted", result.analysis.valueFormatted)
                put("realizadoEm", result.analysis.realizadoEm)
                if (result.analysis.isPix) put("isPix", true)
                if (result.analysis.pagador != null) put("pagador", result.analysis.pagador)
                if (result.analysis.destinatario != null) put("destinatario", result.analysis.destinatario)
                if (result.analysis.instituicao != null) put("instituicao", result.analysis.instituicao)
                put("text", "📄 **${result.analysis.title}**\n\nValor: ${result.analysis.valueFormatted ?: "N/D"}\nRealizado em: ${result.analysis.realizadoEm}$deStr$paraStr$instStr\n\n${result.analysis.rawText}")
                put("fontSize", 13)
                put("align", "left")
            }.toString()
            BlockEntity(
                projectId = projectId,
                title = result.analysis.title,
                type = "text",
                posX = spawnX,
                posY = spawnY,
                width = 260,
                height = 180,
                contentJson = content
            )
        }

        val msg = if (result.analysis.valueFormatted != null) {
            "Comprovante '${result.analysis.title}' (${result.analysis.valueFormatted}) adicionado!"
        } else {
            "Item '${result.analysis.title}' adicionado ao Canvas!"
        }
        return Pair(block, msg)
    }

    fun importText(
        text: String,
        subject: String?,
        spawnX: Float,
        spawnY: Float,
        projectId: Long
    ): Pair<BlockEntity, String> {
        val analysis = ReceiptAnalyzer.analyze(text, subject ?: "")
        val content = buildJsonObject {
            if (analysis.value != null) put("valor", analysis.value)
            if (analysis.valueFormatted != null) put("valorFormatted", analysis.valueFormatted)
            put("realizadoEm", analysis.realizadoEm)
            if (analysis.isPix) put("isPix", true)
            if (analysis.pagador != null) put("pagador", analysis.pagador)
            if (analysis.destinatario != null) put("destinatario", analysis.destinatario)
            if (analysis.instituicao != null) put("instituicao", analysis.instituicao)
            put("text", text)
            put("fontSize", 12)
            put("align", "left")
        }.toString()

        val block = BlockEntity(
            projectId = projectId,
            title = analysis.title,
            type = "text",
            posX = spawnX,
            posY = spawnY,
            width = 280,
            height = 200,
            contentJson = content
        )

        val msg = if (analysis.valueFormatted != null) {
            "Comprovante '${analysis.title}' (${analysis.valueFormatted}) adicionado!"
        } else {
            "Texto compartilhado adicionado ao Canvas!"
        }
        return Pair(block, msg)
    }

    fun updateImageFromUri(context: Context, block: BlockEntity, uri: Uri): BlockEntity? {
        val imagesDir = File(context.filesDir, "canvas_images").apply { if (!exists()) mkdirs() }
        val destFile = File(imagesDir, "img_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output -> input.copyTo(output) }
        }
        if (destFile.exists() && destFile.length() > 0) {
            return BlockPropertyUpdater.updateImageUrl(block, "file://${destFile.absolutePath}")
        }
        return null
    }

    fun downloadImageBase64(url: String): String? {
        if (url.startsWith("data:") || url.startsWith("blob:")) return null
        return try {
            val connection = URL(url).openConnection()
            connection.connect()
            val bytes = connection.getInputStream().use { it.readBytes() }
            "data:image/jpeg;base64," + android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }
}
