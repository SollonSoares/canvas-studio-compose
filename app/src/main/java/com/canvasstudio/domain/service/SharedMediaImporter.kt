package com.canvasstudio.domain.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import com.canvasstudio.domain.model.AnalyzedReceipt
import com.canvasstudio.ui.block.utils.ReceiptAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class ImportedMediaResult(
    val savedFileUri: String?,
    val blockWidth: Int,
    val blockHeight: Int,
    val analysis: AnalyzedReceipt,
    val isImage: Boolean
)

class SharedMediaImporter(private val context: Context) {

    private val imagesDir: File by lazy {
        File(context.filesDir, "canvas_images").apply {
            if (!exists()) mkdirs()
        }
    }

    suspend fun importMedia(uri: Uri, autoOcrEnabled: Boolean = true): ImportedMediaResult = withContext(Dispatchers.IO) {
        val fileName = resolveFileName(uri)
        val mimeType = context.contentResolver.getType(uri) ?: ""
        var savedFileUri: String? = null
        var extractedOcrText = ""
        var blockWidth = 320
        var blockHeight = 420
        var isImage = false

        // 1. Tentar processar como PDF via PdfRenderer
        val isPdf = mimeType.contains("pdf", ignoreCase = true) || fileName.endsWith(".pdf", ignoreCase = true)
        if (isPdf) {
            val tempPdf = File(context.cacheDir, "temp_${System.currentTimeMillis()}.pdf")
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(tempPdf).use { output ->
                        input.copyTo(output)
                    }
                }
                if (tempPdf.exists() && tempPdf.length() > 0) {
                    val pfd = ParcelFileDescriptor.open(tempPdf, ParcelFileDescriptor.MODE_READ_ONLY)
                    val renderer = PdfRenderer(pfd)
                    if (renderer.pageCount > 0) {
                        val page = renderer.openPage(0)
                        val bW = page.width * 2
                        val bH = page.height * 2
                        val bitmap = Bitmap.createBitmap(bW, bH, Bitmap.Config.ARGB_8888)
                        bitmap.eraseColor(android.graphics.Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        page.close()
                        renderer.close()
                        pfd.close()

                        if (autoOcrEnabled) {
                            extractedOcrText = ReceiptAnalyzer.extractTextFromBitmap(bitmap)
                        }

                        val destFile = File(imagesDir, "pdf_page_${System.currentTimeMillis()}.jpg")
                        FileOutputStream(destFile).use { out ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
                        }
                        savedFileUri = "file://${destFile.absolutePath}"
                        val ratio = bH.toFloat() / bW.toFloat()
                        blockWidth = 320
                        blockHeight = (320 * ratio).toInt() + 65
                        isImage = true
                    } else {
                        renderer.close()
                        pfd.close()
                    }
                }
            } catch (e: Exception) {
                Log.w("SharedMediaImporter", "PdfRenderer attempt failed: ${e.message}")
            } finally {
                tempPdf.delete()
            }
        }

        // 2. Se não era PDF ou falhou, tentar como Imagem
        if (savedFileUri == null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    val bitmap = BitmapFactory.decodeStream(input)
                    if (bitmap != null) {
                        if (autoOcrEnabled) {
                            extractedOcrText = ReceiptAnalyzer.extractTextFromBitmap(bitmap)
                        }

                        val maxDim = 1920
                        val w = bitmap.width
                        val h = bitmap.height
                        val scaled = if (w > maxDim || h > maxDim) {
                            val r = w.toFloat() / h.toFloat()
                            val (nw, nh) = if (w > h) Pair(maxDim, (maxDim / r).toInt()) else Pair((maxDim * r).toInt(), maxDim)
                            Bitmap.createScaledBitmap(bitmap, nw, nh, true)
                        } else bitmap

                        val destFile = File(imagesDir, "img_${System.currentTimeMillis()}.jpg")
                        FileOutputStream(destFile).use { out ->
                            scaled.compress(Bitmap.CompressFormat.JPEG, 90, out)
                        }
                        savedFileUri = "file://${destFile.absolutePath}"
                        val ratio = h.toFloat() / w.toFloat().coerceAtLeast(0.4f)
                        blockWidth = 320
                        blockHeight = (320 * ratio).toInt() + 65
                        isImage = true
                    }
                }
            } catch (e: Exception) {
                Log.w("SharedMediaImporter", "Bitmap decode attempt failed: ${e.message}")
            }
        }

        // 3. Análise Inteligente de Metadados
        val analysis = if (autoOcrEnabled && extractedOcrText.isNotBlank()) {
            ReceiptAnalyzer.analyze(extractedOcrText, fileName)
        } else {
            ReceiptAnalyzer.analyze("", fileName)
        }

        ImportedMediaResult(
            savedFileUri = savedFileUri,
            blockWidth = blockWidth,
            blockHeight = blockHeight,
            analysis = analysis,
            isImage = isImage
        )
    }

    private fun resolveFileName(uri: Uri): String {
        var name = ""
        try {
            context.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    name = cursor.getString(0) ?: ""
                }
            }
        } catch (e: Exception) {
            name = uri.lastPathSegment ?: "Item"
        }
        return name
    }
}
