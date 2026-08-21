package com.canvasstudio.features.export_portability

import android.content.Context
import android.net.Uri
import android.util.Log
import com.canvasstudio.data.local.entity.BlockEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

data class GallerySyncResult(
    val updatedBlocks: List<BlockEntity>,
    val syncedCount: Int,
    val githubUploadedCount: Int,
    val zipFile: File?,
    val imageFiles: List<File>,
    val publicUrls: List<String>,
    val errorMessages: List<String>
)

class GallerySyncService(private val context: Context) {

    private val gitHubApiService = GitHubApiService()

    suspend fun syncBlocksToGallery(
        blocks: List<BlockEntity>,
        galleryBaseUrl: String,
        githubToken: String = "",
        repoOwner: String = "SollonSoares",
        repoName: String = "galeria",
        repoPath: String = "imagens",
        repoBranch: String = "main"
    ): GallerySyncResult = withContext(Dispatchers.IO) {
        val stagingDir = File(context.cacheDir, "gallery_staging_${System.currentTimeMillis()}").apply {
            if (!exists()) mkdirs()
        }

        val cleanBaseUrl = galleryBaseUrl.trim().trimEnd('/')
        var syncedCount = 0
        var githubUploadedCount = 0
        val exportedFiles = mutableListOf<File>()
        val publicUrls = mutableListOf<String>()
        val errorMessages = mutableListOf<String>()

        val updatedBlocks = blocks.map { block ->
            if (block.type.equals("image", ignoreCase = true)) {
                val currentObj = try {
                    Json.parseToJsonElement(block.contentJson).jsonObject.toMutableMap()
                } catch (e: Exception) {
                    mutableMapOf()
                }

                val currentUrl = currentObj["url"]?.jsonPrimitive?.contentOrNull ?: ""
                val isLocal = currentUrl.startsWith("file://") || 
                              currentUrl.startsWith("content://") || 
                              currentUrl.startsWith("/") ||
                              currentUrl.startsWith("data:")

                if (isLocal && currentUrl.isNotBlank()) {
                    val sanitizedTitle = block.title
                        .lowercase()
                        .replace(Regex("[^a-z0-9_]"), "_")
                        .trim('_')
                        .take(30)
                        .ifBlank { "imagem" }

                    val fileName = if (sanitizedTitle.isNotEmpty()) {
                        "${sanitizedTitle}_${block.id}.jpg"
                    } else {
                        "img_${block.id}.jpg"
                    }

                    val destFile = File(stagingDir, fileName)

                    var copySuccess = false
                    try {
                        if (currentUrl.startsWith("file://")) {
                            val srcFile = File(currentUrl.removePrefix("file://"))
                            if (srcFile.exists()) {
                                srcFile.copyTo(destFile, overwrite = true)
                                copySuccess = true
                            }
                        } else if (currentUrl.startsWith("content://")) {
                            val uri = Uri.parse(currentUrl)
                            context.contentResolver.openInputStream(uri)?.use { input ->
                                FileOutputStream(destFile).use { output ->
                                    input.copyTo(output)
                                }
                            }
                            copySuccess = destFile.exists() && destFile.length() > 0
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    if (copySuccess) {
                        exportedFiles.add(destFile)

                        // Se o token do GitHub estiver configurado, faz o commit direto no repositório!
                        if (githubToken.isNotBlank()) {
                            val uploadResult = gitHubApiService.uploadFile(
                                owner = repoOwner,
                                repo = repoName,
                                path = repoPath,
                                branch = repoBranch,
                                token = githubToken.trim(),
                                file = destFile,
                                customFileName = fileName
                            )

                            if (uploadResult.success) {
                                githubUploadedCount++
                                Log.d("GallerySyncService", "Upload direto no GitHub concluído: ${uploadResult.publicUrl}")
                            } else {
                                uploadResult.errorMessage?.let { errorMessages.add(it) }
                            }
                        }
                    }

                    // Construir a URL pública definitiva da Galeria Web
                    val publicUrl = "$cleanBaseUrl/$fileName"
                    currentObj["url"] = JsonPrimitive(publicUrl)
                    publicUrls.add(publicUrl)
                    syncedCount++

                    block.copy(contentJson = JsonObject(currentObj).toString())
                } else {
                    block
                }
            } else {
                block
            }
        }

        // Criar o arquivo ZIP contendo todas as imagens físicas como backup
        var zipFile: File? = null
        if (exportedFiles.isNotEmpty()) {
            try {
                val zipDest = File(context.cacheDir, "galeria_imagens_export_${System.currentTimeMillis()}.zip")
                ZipOutputStream(FileOutputStream(zipDest)).use { zipOut ->
                    for (file in exportedFiles) {
                        val entry = ZipEntry(file.name)
                        zipOut.putNextEntry(entry)
                        FileInputStream(file).use { input ->
                            input.copyTo(zipOut)
                        }
                        zipOut.closeEntry()
                    }
                }
                if (zipDest.exists() && zipDest.length() > 0) {
                    zipFile = zipDest
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        GallerySyncResult(
            updatedBlocks = updatedBlocks,
            syncedCount = syncedCount,
            githubUploadedCount = githubUploadedCount,
            zipFile = zipFile,
            imageFiles = exportedFiles,
            publicUrls = publicUrls,
            errorMessages = errorMessages
        )
    }
}
