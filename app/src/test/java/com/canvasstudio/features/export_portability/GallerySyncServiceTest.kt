package com.canvasstudio.features.export_portability

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.canvasstudio.data.local.entity.BlockEntity
import com.canvasstudio.data.repository.BlockRepository
import com.canvasstudio.ui.block.delegates.CanvasExportSyncCoordinator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class GallerySyncServiceTest {

    private lateinit var context: Context
    private lateinit var syncService: GallerySyncService

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        syncService = GallerySyncService(context)
    }

    @Test
    fun `syncBlocksToGallery exporta imagem local e altera link para Galeria Web`() = runTest {
        val tempImg = File(context.cacheDir, "test_source.jpg")
        tempImg.writeText("fake_image_content")

        val jsonContent = buildJsonObject {
            put("url", "file://${tempImg.absolutePath}")
        }.toString()

        val block = BlockEntity(
            id = 10,
            projectId = 0,
            title = "Foto de Teste",
            type = "image",
            posX = 0f,
            posY = 0f,
            width = 200,
            height = 200,
            contentJson = jsonContent
        )

        val result = syncService.syncBlocksToGallery(
            blocks = listOf(block),
            galleryBaseUrl = "https://sollonsoares.github.io/galeria/imagens"
        )

        assertEquals(1, result.syncedCount)
        assertEquals(1, result.imageFiles.size)
        assertNotNull(result.zipFile)

        val updatedJson = Json.parseToJsonElement(result.updatedBlocks.first().contentJson).jsonObject
        val newUrl = updatedJson["url"]?.jsonPrimitive?.content ?: ""
        assertTrue(newUrl.startsWith("https://sollonsoares.github.io/galeria/imagens/foto_de_teste_10.jpg"))
    }

    @Test
    fun `syncBlocksToGallery mantem url publica inalterada`() = runTest {
        val block = BlockEntity(
            id = 11,
            projectId = 0,
            title = "Imagem Remota",
            type = "image",
            posX = 0f,
            posY = 0f,
            width = 200,
            height = 200,
            contentJson = """{"url":"https://sollonsoares.github.io/galeria/imagens/remota.jpg"}"""
        )

        val result = syncService.syncBlocksToGallery(
            blocks = listOf(block),
            galleryBaseUrl = "https://sollonsoares.github.io/galeria/imagens"
        )

        assertEquals(0, result.syncedCount)
        assertEquals(0, result.imageFiles.size)
    }

    @Test
    fun `CanvasExportSyncCoordinator emite mensagem informando sucesso de exportacao e alteracao de links`() = runTest {
        val tempImg = File(context.cacheDir, "test_photo.jpg")
        tempImg.writeText("image_bytes")

        val jsonContent = buildJsonObject {
            put("url", "file://${tempImg.absolutePath}")
        }.toString()

        val block = BlockEntity(
            id = 20,
            projectId = 0,
            title = "Avatar",
            type = "image",
            posX = 0f,
            posY = 0f,
            width = 200,
            height = 200,
            contentJson = jsonContent
        )

        var emittedMsg = ""
        val updatedInRepo = mutableListOf<BlockEntity>()

        val fakeRepo = object : BlockRepository {
            override fun getBlocksStream(projectId: Long): Flow<List<BlockEntity>> = flowOf(emptyList())
            override suspend fun insertBlock(block: BlockEntity) {}
            override suspend fun insertBlocks(blocks: List<BlockEntity>) {}
            override suspend fun updateBlock(block: BlockEntity) { updatedInRepo.add(block) }
            override suspend fun deleteBlock(block: BlockEntity) {}
            override suspend fun clearCanvas(projectId: Long) {}
            override suspend fun getCachedImage(imgId: String): String? = null
            override suspend fun saveImageCache(imgId: String, data: String) {}
            override suspend fun deleteImageCache(imgId: String) {}
        }

        CanvasExportSyncCoordinator.syncToGallery(
            context = context,
            blocks = listOf(block),
            galleryBaseUrl = "https://sollonsoares.github.io/galeria/imagens",
            githubToken = "",
            blockRepository = fakeRepo,
            onShareZip = {},
            onEmitEvent = { emittedMsg = it }
        )

        assertTrue(emittedMsg.contains("Sucesso"))
        assertTrue(emittedMsg.contains("links alterados"))
        assertEquals(1, updatedInRepo.size)
    }
}
