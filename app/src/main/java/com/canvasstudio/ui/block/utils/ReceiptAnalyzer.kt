package com.canvasstudio.ui.block.utils

import android.graphics.Bitmap
import android.util.Log
import com.canvasstudio.domain.model.AnalyzedReceipt
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

/**
 * Fachada principal de OCR e extração heurística de comprovantes.
 */
object ReceiptAnalyzer {

    suspend fun extractTextFromBitmap(bitmap: Bitmap): String {
        return try {
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val image = InputImage.fromBitmap(bitmap, 0)
            val result = recognizer.process(image).await()
            val text = result.text
            Log.d("ReceiptAnalyzer", "OCR extracted text (${text.length} chars)")
            text
        } catch (e: Exception) {
            Log.e("ReceiptAnalyzer", "OCR extraction failed: ${e.message}", e)
            ""
        }
    }

    fun analyze(rawText: String, fileName: String = ""): AnalyzedReceipt {
        val cleanText = rawText.replace("\r", "\n")
        val lines = cleanText.split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val isPix = ReceiptFinancialExtractors.isPixPayment(cleanText, fileName)
        val extractedValue = ReceiptFinancialExtractors.extractMonetaryValue(cleanText)
            ?: ReceiptFinancialExtractors.extractValueFromFileName(fileName)
        val formattedValue = extractedValue?.let { formatCurrency(it) }
        val extractedDate = ReceiptFinancialExtractors.extractRealizadoEm(cleanText)
        val (pagador, destinatario) = ReceiptPartiesExtractor.extractParties(cleanText, lines)
        val instituicao = ReceiptInstitutionsCatalog.extractInstituicao(cleanText, lines)

        val extractedTitle = if (isPix) {
            when {
                destinatario != null -> "Pix - $destinatario"
                pagador != null -> "Pix - $pagador"
                instituicao != null -> "Pix - $instituicao"
                else -> "Comprovante Pix"
            }
        } else {
            ReceiptPartiesExtractor.extractTitle(cleanText, lines, fileName, destinatario ?: pagador)
        }

        return AnalyzedReceipt(
            title = extractedTitle,
            value = extractedValue,
            valueFormatted = formattedValue,
            realizadoEm = extractedDate,
            isPix = isPix,
            pagador = pagador,
            destinatario = destinatario,
            instituicao = instituicao,
            rawText = cleanText
        )
    }

    fun formatCurrency(value: Float): String {
        return ReceiptFinancialExtractors.formatCurrency(value)
    }
}
