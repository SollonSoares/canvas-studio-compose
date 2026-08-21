package com.canvasstudio.ui.block.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.canvasstudio.data.local.entity.BlockEntity
import java.io.OutputStream

object PdfExporter {
    fun exportCanvasToPdf(
        context: Context,
        blocks: List<BlockEntity>,
        canvasWidth: Int,
        canvasHeight: Int,
        outputStream: OutputStream
    ) {
        val document = PdfDocument()
        
        // Android PdfDocument uses 72 DPI points. 
        // We might need to scale dp to points if needed, but for simplicity we'll treat 1dp approx 1pt here
        // or just use the pixel/dp values as is for the page size.
        val pageInfo = PdfDocument.PageInfo.Builder(canvasWidth, canvasHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        
        val paint = Paint()
        val textPaint = Paint().apply {
            textSize = 12f
            isAntiAlias = true
        }

        blocks.forEach { block ->
            // Draw Block Background
            paint.color = 0xFFF0F0F0.toInt() // Light gray for blocks in PDF
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(
                block.posX, 
                block.posY, 
                block.posX + block.width, 
                block.posY + block.height, 
                10f, 10f, paint
            )
            
            // Draw Border
            paint.color = 0xFFCCCCCC.toInt()
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRoundRect(
                block.posX, 
                block.posY, 
                block.posX + block.width, 
                block.posY + block.height, 
                10f, 10f, paint
            )

            // Draw Title
            textPaint.color = 0xFF333333.toInt()
            textPaint.isFakeBoldText = true
            canvas.drawText(block.title, block.posX + 10f, block.posY + 25f, textPaint)
            
            // Draw Content (Summary)
            textPaint.isFakeBoldText = false
            textPaint.textSize = 10f
            val contentSnippet = if (block.type == "text") {
                // Simplified text extraction for PDF
                block.contentJson.take(50) + "..."
            } else {
                "[Type: ${block.type}]"
            }
            canvas.drawText(contentSnippet, block.posX + 10f, block.posY + 45f, textPaint)
        }

        document.finishPage(page)
        document.writeTo(outputStream)
        document.close()
    }

    fun sharePdf(
        context: Context,
        blocks: List<BlockEntity>,
        canvasWidth: Int,
        canvasHeight: Int,
        fileName: String = "canvas_export.pdf"
    ) {
        try {
            val cacheFile = java.io.File(context.cacheDir, fileName)
            java.io.FileOutputStream(cacheFile).use { outputStream ->
                exportCanvasToPdf(context, blocks, canvasWidth, canvasHeight, outputStream)
            }
            
            val contentUri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                cacheFile
            )
            
            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(android.content.Intent.EXTRA_STREAM, contentUri)
                putExtra(android.content.Intent.EXTRA_SUBJECT, "Canvas Export - PDF")
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            val chooser = android.content.Intent.createChooser(shareIntent, "Compartilhar PDF").apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "Erro ao compartilhar PDF: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}
