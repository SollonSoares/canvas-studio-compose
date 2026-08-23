package com.canvasstudio.ui.block.utils

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Base64
import androidx.core.content.FileProvider
import com.canvasstudio.data.local.entity.BlockEntity
import kotlinx.serialization.json.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Motor de Exportação em PDF Paginado em Padrão A4 (595 x 842 pt).
 * Diagrama automaticamente os blocos em páginas A4 com cabeçalho, rodapé e numeração.
 */
object PdfExporter {

    // Dimensões A4 Padrão (ISO 216 a 72 DPI)
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842

    private const val MARGIN_LEFT = 36f
    private const val MARGIN_RIGHT = 36f
    private const val CONTENT_START_Y = 52f
    private const val CONTENT_MAX_Y = 790f
    private const val CONTENT_WIDTH = PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT // 523f
    private const val COLUMN_GAP = 12f
    private const val COLUMN_WIDTH = (CONTENT_WIDTH - COLUMN_GAP) / 2f // 255.5f
    private const val BLOCK_GAP = 12f

    private data class BlockLayoutPosition(
        val block: BlockEntity,
        val pageIndex: Int,
        val rect: RectF
    )

    fun exportCanvasToPdf(
        context: Context,
        blocks: List<BlockEntity>,
        outputStream: OutputStream,
        documentTitle: String = "Canvas Studio",
        canvasWidth: Int = 2000,
        canvasHeight: Int = 2000
    ) {
        val document = PdfDocument()
        var currentPage: PdfDocument.Page? = null

        try {
            if (blocks.isEmpty()) {
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
                currentPage = document.startPage(pageInfo)
                val canvas = currentPage.canvas
                drawEmptyState(canvas, documentTitle)
                document.finishPage(currentPage)
                currentPage = null
                document.writeTo(outputStream)
                return
            }

            // 1. Calcular a diagramação e paginação dos blocos
            val layouts = calculateMultiPageLayout(blocks)
            val totalPages = maxOf(1, (layouts.maxOfOrNull { it.pageIndex } ?: 0) + 1)
            val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

            // 2. Renderizar cada página
            for (pageIdx in 0 until totalPages) {
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIdx + 1).create()
                currentPage = document.startPage(pageInfo)
                val canvas = currentPage.canvas

                // Fundo da folha
                canvas.drawColor(0xFFF8FAFC.toInt())

                // Cabeçalho e Rodapé da Página A4
                drawPageHeader(canvas, documentTitle)
                drawPageFooter(canvas, pageIdx + 1, totalPages, formattedDate)

                // Renderizar os blocos desta página
                val pageBlocks = layouts.filter { it.pageIndex == pageIdx }
                pageBlocks.forEach { layout ->
                    drawBlockCard(canvas, context, layout.block, layout.rect)
                }

                document.finishPage(currentPage)
                currentPage = null
            }

            document.writeTo(outputStream)
        } finally {
            currentPage?.let {
                try { document.finishPage(it) } catch (e: Exception) {}
            }
            try { document.close() } catch (e: Exception) {}
        }
    }

    // Sobrecarga para compatibilidade com chamadas existentes com canvasWidth/canvasHeight
    fun exportCanvasToPdf(
        context: Context,
        blocks: List<BlockEntity>,
        canvasWidth: Int,
        canvasHeight: Int,
        outputStream: OutputStream
    ) {
        exportCanvasToPdf(
            context = context,
            blocks = blocks,
            outputStream = outputStream,
            documentTitle = "Canvas Studio Pro",
            canvasWidth = canvasWidth,
            canvasHeight = canvasHeight
        )
    }

    private fun calculateMultiPageLayout(blocks: List<BlockEntity>): List<BlockLayoutPosition> {
        // Ordena os blocos no fluxo de leitura (de cima para baixo, da esquerda para a direita)
        val sortedBlocks = blocks.sortedWith(compareBy<BlockEntity>({ it.posY }, { it.posX }, { it.id }))

        val result = mutableListOf<BlockLayoutPosition>()

        var pageIndex = 0
        var col0Y = CONTENT_START_Y
        var col1Y = CONTENT_START_Y

        val col0X = MARGIN_LEFT
        val col1X = MARGIN_LEFT + COLUMN_WIDTH + COLUMN_GAP

        sortedBlocks.forEach { block ->
            val isFullWidth = block.width >= 360 || (block.type == "text" && block.width >= 320) || block.contentJson.length > 250
            val cardW = if (isFullWidth) CONTENT_WIDTH else COLUMN_WIDTH
            val cardH = estimateBlockHeight(block, isFullWidth)

            if (isFullWidth) {
                // Bloco de largura total
                var targetY = maxOf(col0Y, col1Y)
                if (targetY + cardH > CONTENT_MAX_Y) {
                    pageIndex++
                    col0Y = CONTENT_START_Y
                    col1Y = CONTENT_START_Y
                    targetY = CONTENT_START_Y
                }

                val rect = RectF(MARGIN_LEFT, targetY, MARGIN_LEFT + cardW, targetY + cardH)
                result.add(BlockLayoutPosition(block, pageIndex, rect))

                col0Y = targetY + cardH + BLOCK_GAP
                col1Y = targetY + cardH + BLOCK_GAP
            } else {
                // Bloco em 2 colunas: posiciona na coluna com menor Y
                var targetCol = if (col0Y <= col1Y) 0 else 1
                var targetY = if (targetCol == 0) col0Y else col1Y

                if (targetY + cardH > CONTENT_MAX_Y) {
                    val otherCol = if (targetCol == 0) 1 else 0
                    val otherY = if (otherCol == 0) col0Y else col1Y

                    if (otherY + cardH <= CONTENT_MAX_Y) {
                        targetCol = otherCol
                        targetY = otherY
                    } else {
                        pageIndex++
                        col0Y = CONTENT_START_Y
                        col1Y = CONTENT_START_Y
                        targetCol = 0
                        targetY = CONTENT_START_Y
                    }
                }

                val targetX = if (targetCol == 0) col0X else col1X
                val rect = RectF(targetX, targetY, targetX + cardW, targetY + cardH)
                result.add(BlockLayoutPosition(block, pageIndex, rect))

                if (targetCol == 0) {
                    col0Y = targetY + cardH + BLOCK_GAP
                } else {
                    col1Y = targetY + cardH + BLOCK_GAP
                }
            }
        }

        return result
    }

    private fun estimateBlockHeight(block: BlockEntity, isFullWidth: Boolean): Float {
        return when (block.type.lowercase()) {
            "image" -> if (isFullWidth) 240f else 190f
            "chart" -> if (isFullWidth) 220f else 190f
            "text" -> {
                val json = parseJson(block.contentJson)
                val isReceipt = json["valorFormatted"] != null || json["isPix"] != null || json["valor"] != null
                val textLength = json["text"]?.jsonPrimitive?.contentOrNull?.length ?: 0
                val base = if (isReceipt) 140f else 90f
                val textExtra = (textLength / (if (isFullWidth) 60 else 30)) * 14f
                (base + textExtra).coerceIn(100f, 320f)
            }
            else -> 120f
        }
    }

    private fun drawPageHeader(canvas: Canvas, title: String) {
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF0F172A.toInt()
            textSize = 12f
            isFakeBoldText = true
        }
        val tagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF64748B.toInt()
            textSize = 9f
            textAlign = Paint.Align.RIGHT
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFE2E8F0.toInt()
            strokeWidth = 1f
        }

        canvas.drawText("📋 $title", MARGIN_LEFT, 32f, titlePaint)
        canvas.drawText("CANVAS STUDIO EXPORT", PAGE_WIDTH - MARGIN_RIGHT, 32f, tagPaint)
        canvas.drawLine(MARGIN_LEFT, 40f, PAGE_WIDTH - MARGIN_RIGHT, 40f, linePaint)
    }

    private fun drawPageFooter(canvas: Canvas, currentPage: Int, totalPages: Int, dateStr: String) {
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFE2E8F0.toInt()
            strokeWidth = 1f
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF94A3B8.toInt()
            textSize = 8.5f
        }
        val pagePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF64748B.toInt()
            textSize = 8.5f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
        }

        val footerY = 808f
        canvas.drawLine(MARGIN_LEFT, footerY - 8f, PAGE_WIDTH - MARGIN_RIGHT, footerY - 8f, linePaint)
        canvas.drawText("Gerado em $dateStr", MARGIN_LEFT, footerY + 8f, textPaint)
        canvas.drawText("Página $currentPage de $totalPages", PAGE_WIDTH - MARGIN_RIGHT, footerY + 8f, pagePaint)
    }

    private fun drawEmptyState(canvas: Canvas, title: String) {
        drawPageHeader(canvas, title)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF64748B.toInt()
            textSize = 14f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("O Canvas não possui blocos para exportação.", PAGE_WIDTH / 2f, PAGE_HEIGHT / 2f, textPaint)
        drawPageFooter(canvas, 1, 1, SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()))
    }

    private fun drawBlockCard(canvas: Canvas, context: Context, block: BlockEntity, rect: RectF) {
        // Fundo do Card
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            style = Paint.Style.FILL
        }
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x0A000000
            style = Paint.Style.FILL
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFE2E8F0.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        val shadowRect = RectF(rect.left + 1f, rect.top + 1f, rect.right + 1f, rect.bottom + 2f)
        canvas.drawRoundRect(shadowRect, 8f, 8f, shadowPaint)
        canvas.drawRoundRect(rect, 8f, 8f, cardPaint)
        canvas.drawRoundRect(rect, 8f, 8f, borderPaint)

        // Cabeçalho do Card
        val headerBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFF8FAFC.toInt()
            style = Paint.Style.FILL
        }
        val headerRect = RectF(rect.left, rect.top, rect.right, rect.top + 26f)
        val headerPath = Path().apply {
            addRoundRect(headerRect, floatArrayOf(8f, 8f, 8f, 8f, 0f, 0f, 0f, 0f), Path.Direction.CW)
        }
        canvas.drawPath(headerPath, headerBgPaint)

        val headerDividerPaint = Paint().apply {
            color = 0xFFE2E8F0.toInt()
            strokeWidth = 1f
        }
        canvas.drawLine(rect.left, rect.top + 26f, rect.right, rect.top + 26f, headerDividerPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF1E293B.toInt()
            textSize = 10.5f
            isFakeBoldText = true
        }
        val icon = when (block.type.lowercase()) {
            "image" -> "🖼️"
            "chart" -> "📊"
            else -> "📄"
        }
        val titleText = "$icon ${block.title.ifBlank { "Bloco" }}"
        canvas.drawText(titleText, rect.left + 8f, rect.top + 17f, titlePaint)

        // Conteúdo do Card
        val contentRect = RectF(rect.left + 8f, rect.top + 32f, rect.right - 8f, rect.bottom - 8f)

        when (block.type.lowercase()) {
            "image" -> drawImageContent(canvas, context, block, contentRect)
            "chart" -> drawChartContent(canvas, block, contentRect)
            else -> drawTextContent(canvas, block, contentRect)
        }
    }

    private fun drawImageContent(canvas: Canvas, context: Context, block: BlockEntity, contentRect: RectF) {
        val json = parseJson(block.contentJson)
        val url = json["url"]?.jsonPrimitive?.contentOrNull ?: ""

        if (contentRect.width() <= 0 || contentRect.height() <= 0) return

        val bitmap = loadBitmap(context, url)
        if (bitmap != null) {
            val srcW = bitmap.width.toFloat()
            val srcH = bitmap.height.toFloat()
            val scale = minOf(contentRect.width() / srcW, contentRect.height() / srcH)
            val dstW = srcW * scale
            val dstH = srcH * scale
            val dstLeft = contentRect.left + (contentRect.width() - dstW) / 2f
            val dstTop = contentRect.top + (contentRect.height() - dstH) / 2f
            val dstRect = RectF(dstLeft, dstTop, dstLeft + dstW, dstTop + dstH)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            canvas.drawBitmap(bitmap, null, dstRect, paint)
        } else {
            val placeholderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFF1F5F9.toInt()
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(contentRect, 6f, 6f, placeholderPaint)

            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF64748B.toInt()
                textSize = 10f
                textAlign = Paint.Align.CENTER
            }
            val label = if (url.isBlank()) "Sem imagem configurada" else "🖼️ ${url.substringAfterLast('/').take(28)}"
            canvas.drawText(label, contentRect.centerX(), contentRect.centerY(), textPaint)
        }
    }

    private fun drawTextContent(canvas: Canvas, block: BlockEntity, contentRect: RectF) {
        val json = parseJson(block.contentJson)
        val valorFormatted = json["valorFormatted"]?.jsonPrimitive?.contentOrNull
        val realizadoEm = json["realizadoEm"]?.jsonPrimitive?.contentOrNull
        val pagador = json["pagador"]?.jsonPrimitive?.contentOrNull
        val destinatario = json["destinatario"]?.jsonPrimitive?.contentOrNull
        val instituicao = json["instituicao"]?.jsonPrimitive?.contentOrNull
        val isPix = json["isPix"]?.jsonPrimitive?.booleanOrNull ?: false
        val text = json["text"]?.jsonPrimitive?.contentOrNull ?: ""

        var currentY = contentRect.top + 4f

        if (valorFormatted != null || isPix) {
            val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (isPix) 0xFFCCFBF1.toInt() else 0xFFDCFCE7.toInt()
                style = Paint.Style.FILL
            }
            val badgeBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (isPix) 0xFF14B8A6.toInt() else 0xFF22C55E.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }
            val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (isPix) 0xFF0F766E.toInt() else 0xFF15803D.toInt()
                textSize = 10f
                isFakeBoldText = true
            }

            val badgeStr = if (isPix) "⚡ PIX: $valorFormatted" else "💰 $valorFormatted"
            val badgeWidth = badgeTextPaint.measureText(badgeStr) + 12f
            val badgeRect = RectF(contentRect.left, currentY, contentRect.left + badgeWidth, currentY + 18f)
            canvas.drawRoundRect(badgeRect, 4f, 4f, badgePaint)
            canvas.drawRoundRect(badgeRect, 4f, 4f, badgeBorder)
            canvas.drawText(badgeStr, contentRect.left + 6f, currentY + 13f, badgeTextPaint)

            currentY += 24f

            val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF475569.toInt()
                textSize = 9f
            }
            if (!pagador.isNullOrBlank()) {
                canvas.drawText("De: $pagador", contentRect.left, currentY, metaPaint)
                currentY += 12f
            }
            if (!destinatario.isNullOrBlank()) {
                canvas.drawText("Para: $destinatario", contentRect.left, currentY, metaPaint)
                currentY += 12f
            }
            if (!realizadoEm.isNullOrBlank()) {
                canvas.drawText("Data: $realizadoEm", contentRect.left, currentY, metaPaint)
                currentY += 12f
            }
            if (!instituicao.isNullOrBlank()) {
                canvas.drawText("Banco: $instituicao", contentRect.left, currentY, metaPaint)
                currentY += 12f
            }
        }

        val cleanText = text.replace("**", "").replace("#", "").trim()
        if (cleanText.isNotEmpty()) {
            val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF334155.toInt()
                textSize = 9.5f
            }
            val availableW = maxOf(10, contentRect.width().toInt())
            val availableH = maxOf(10f, contentRect.bottom - currentY)

            val textLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StaticLayout.Builder.obtain(cleanText, 0, cleanText.length, textPaint, availableW).build()
            } else {
                @Suppress("DEPRECATION")
                StaticLayout(cleanText, textPaint, availableW, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false)
            }

            canvas.save()
            canvas.translate(contentRect.left, currentY)
            canvas.clipRect(0f, 0f, availableW.toFloat(), availableH)
            textLayout.draw(canvas)
            canvas.restore()
        }
    }

    private fun drawChartContent(canvas: Canvas, block: BlockEntity, contentRect: RectF) {
        val json = parseJson(block.contentJson)
        val root = json["inputs"]?.jsonObject ?: json
        val stats = listOf(
            root["ninjutsu"]?.jsonPrimitive?.floatOrNull ?: root["nin"]?.jsonPrimitive?.floatOrNull ?: 5f,
            root["inteligencia"]?.jsonPrimitive?.floatOrNull ?: root["int"]?.jsonPrimitive?.floatOrNull ?: 5f,
            root["chakra"]?.jsonPrimitive?.floatOrNull ?: root["cha"]?.jsonPrimitive?.floatOrNull ?: root["chakraMax"]?.jsonPrimitive?.floatOrNull ?: 5f,
            root["taijutsu"]?.jsonPrimitive?.floatOrNull ?: root["tai"]?.jsonPrimitive?.floatOrNull ?: 5f,
            root["vigor"]?.jsonPrimitive?.floatOrNull ?: root["vig"]?.jsonPrimitive?.floatOrNull ?: 5f,
            root["genjutsu"]?.jsonPrimitive?.floatOrNull ?: root["gen"]?.jsonPrimitive?.floatOrNull ?: 5f
        )
        val labels = listOf("NIN", "INT", "CHK", "TAI", "VIG", "GEN")
        val maxVal = maxOf(10f, (stats.maxOrNull() ?: 5f))

        val cx = contentRect.centerX()
        val cy = contentRect.centerY()
        val radius = minOf(contentRect.width() / 2f - 24f, contentRect.height() / 2f - 12f).coerceAtLeast(20f)

        val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFCBD5E1.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
        }
        val polyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x440284C7
            style = Paint.Style.FILL
        }
        val polyBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF0284C7.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF64748B.toInt()
            textSize = 8f
            textAlign = Paint.Align.CENTER
        }

        // Anéis do radar
        for (step in 1..4) {
            val r = radius * (step / 4f)
            val ringPath = Path()
            for (i in 0 until 6) {
                val angle = (i * 60f - 90f) * (PI / 180f)
                val px = cx + (r * cos(angle)).toFloat()
                val py = cy + (r * sin(angle)).toFloat()
                if (i == 0) ringPath.moveTo(px, py) else ringPath.lineTo(px, py)
            }
            ringPath.close()
            canvas.drawPath(ringPath, gridPaint)
        }

        // Eixos radiais e rótulos
        for (i in 0 until 6) {
            val angle = (i * 60f - 90f) * (PI / 180f)
            val px = cx + (radius * cos(angle)).toFloat()
            val py = cy + (radius * sin(angle)).toFloat()
            canvas.drawLine(cx, cy, px, py, gridPaint)

            val lx = cx + ((radius + 10f) * cos(angle)).toFloat()
            val ly = cy + ((radius + 10f) * sin(angle)).toFloat() + 3f
            canvas.drawText(labels[i], lx, ly, labelPaint)
        }

        // Polígono preenchido
        val polyPath = Path()
        for (i in 0 until 6) {
            val v = (stats[i] / maxVal).coerceIn(0.1f, 1f)
            val r = radius * v
            val angle = (i * 60f - 90f) * (PI / 180f)
            val px = cx + (r * cos(angle)).toFloat()
            val py = cy + (r * sin(angle)).toFloat()
            if (i == 0) polyPath.moveTo(px, py) else polyPath.lineTo(px, py)
        }
        polyPath.close()
        canvas.drawPath(polyPath, polyPaint)
        canvas.drawPath(polyPath, polyBorder)
    }

    fun generatePdfFile(
        context: Context,
        blocks: List<BlockEntity>,
        documentTitle: String = "Canvas Studio",
        fileName: String = "canvas_export.pdf"
    ): File {
        val cacheFile = File(context.cacheDir, fileName)
        FileOutputStream(cacheFile).use { outputStream ->
            exportCanvasToPdf(context, blocks, outputStream, documentTitle)
        }
        return cacheFile
    }

    fun generatePdfFile(
        context: Context,
        blocks: List<BlockEntity>,
        canvasWidth: Int,
        canvasHeight: Int,
        fileName: String = "canvas_export.pdf"
    ): File {
        return generatePdfFile(context, blocks, "Canvas Studio Pro", fileName)
    }

    fun openShareIntent(context: Context, file: File) {
        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, "Canvas Export - PDF")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(shareIntent, "Compartilhar PDF").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    fun sharePdf(
        context: Context,
        blocks: List<BlockEntity>,
        documentTitle: String = "Canvas Studio",
        fileName: String = "canvas_export.pdf"
    ) {
        val file = generatePdfFile(context, blocks, documentTitle, fileName)
        openShareIntent(context, file)
    }

    private fun loadBitmap(context: Context, url: String): Bitmap? {
        if (url.isBlank()) return null
        return try {
            when {
                url.startsWith("file://") -> {
                    val rawPath = url.removePrefix("file://")
                    val cleanPath = if (rawPath.startsWith("/") && rawPath.length > 2 && rawPath[2] == ':') {
                        rawPath.removePrefix("/")
                    } else rawPath
                    BitmapFactory.decodeFile(cleanPath)
                }
                url.startsWith("/") -> {
                    BitmapFactory.decodeFile(url)
                }
                url.startsWith("content://") -> {
                    val uri = Uri.parse(url)
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        BitmapFactory.decodeStream(input)
                    }
                }
                url.startsWith("data:") -> {
                    val base64Part = url.substringAfter("base64,", "")
                    if (base64Part.isNotEmpty()) {
                        val bytes = Base64.decode(base64Part, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    } else null
                }
                url.startsWith("http://") || url.startsWith("https://") -> {
                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.connectTimeout = 8000
                    connection.readTimeout = 8000
                    connection.doInput = true
                    connection.connect()
                    connection.inputStream.use { input ->
                        BitmapFactory.decodeStream(input)
                    }
                }
                else -> {
                    val file = File(url)
                    if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun parseJson(jsonString: String): JsonObject {
        return try {
            Json.parseToJsonElement(jsonString).jsonObject
        } catch (e: Exception) {
            JsonObject(emptyMap())
        }
    }
}
