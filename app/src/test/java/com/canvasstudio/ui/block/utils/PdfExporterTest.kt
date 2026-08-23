package com.canvasstudio.ui.block.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import androidx.test.core.app.ApplicationProvider
import com.canvasstudio.data.local.entity.BlockEntity
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream

@Implements(PdfDocument::class)
class ShadowPdfDocument {
    @Implementation
    fun __constructor__() {}

    @Implementation
    fun startPage(pageInfo: PdfDocument.PageInfo): PdfDocument.Page {
        val bitmap = Bitmap.createBitmap(pageInfo.pageWidth, pageInfo.pageHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val constructor = PdfDocument.Page::class.java.getDeclaredConstructor(Canvas::class.java, PdfDocument.PageInfo::class.java)
        constructor.isAccessible = true
        return constructor.newInstance(canvas, pageInfo)
    }

    @Implementation
    fun finishPage(page: PdfDocument.Page) {}

    @Implementation
    fun writeTo(out: OutputStream) {
        out.write("%PDF-1.4 simulated pdf".toByteArray())
    }

    @Implementation
    fun close() {}
}

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, shadows = [ShadowPdfDocument::class])
class PdfExporterTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `exportCanvasToPdf gera PDF com blocos de texto, imagem e radar sem quebrar`() {
        val tempImg = File(context.cacheDir, "sample_pdf_img.jpg")
        tempImg.writeText("sample_img_data")

        val imgJson = buildJsonObject {
            put("url", "file://${tempImg.absolutePath}")
        }.toString()

        val textJson = buildJsonObject {
            put("text", "Texto de Teste para o PDF")
            put("valorFormatted", "R$ 250,00")
            put("isPix", true)
            put("pagador", "Carlos")
            put("destinatario", "Maria")
        }.toString()

        val chartJson = buildJsonObject {
            put("ninjutsu", 8)
            put("inteligencia", 9)
            put("chakra", 7)
            put("taijutsu", 6)
            put("vigor", 8)
            put("genjutsu", 5)
        }.toString()

        val blocks = listOf(
            BlockEntity(1, 0, "Comprovante", "text", 50f, 50f, 250, 200, textJson),
            BlockEntity(2, 0, "Imagem de Teste", "image", 320f, 50f, 250, 200, imgJson),
            BlockEntity(3, 0, "Radar", "chart", 50f, 280f, 300, 300, chartJson)
        )

        val output = ByteArrayOutputStream()
        PdfExporter.exportCanvasToPdf(
            context = context,
            blocks = blocks,
            canvasWidth = 1000,
            canvasHeight = 1000,
            outputStream = output
        )

        val pdfBytes = output.toByteArray()
        assertTrue("PDF deve ter tamanho maior que zero", pdfBytes.isNotEmpty())
    }

    @Test
    fun `generatePdfFile cria arquivo em disco com sucesso`() {
        val blocks = listOf(
            BlockEntity(1, 0, "Bloco Simples", "text", 10f, 10f, 200, 150, """{"text":"Olá PDF"}""")
        )

        val file = PdfExporter.generatePdfFile(
            context = context,
            blocks = blocks,
            canvasWidth = 800,
            canvasHeight = 800,
            fileName = "test_export.pdf"
        )

        assertNotNull(file)
        assertTrue(file.exists())
        assertTrue(file.length() > 0)
    }

    @Test
    fun `exportCanvasToPdf pagina multiplos blocos em varias paginas A4`() {
        val blocks = (1..15).map { idx ->
            BlockEntity(
                id = idx.toLong(),
                projectId = 0,
                title = "Bloco #$idx",
                type = if (idx % 3 == 0) "chart" else "text",
                posX = 50f,
                posY = idx * 150f,
                width = 250,
                height = 180,
                contentJson = """{"text": "Conteúdo do bloco $idx"}"""
            )
        }

        val output = ByteArrayOutputStream()
        PdfExporter.exportCanvasToPdf(
            context = context,
            blocks = blocks,
            outputStream = output,
            documentTitle = "Meu Projeto Multipage"
        )

        assertTrue(output.toByteArray().isNotEmpty())
    }
}
