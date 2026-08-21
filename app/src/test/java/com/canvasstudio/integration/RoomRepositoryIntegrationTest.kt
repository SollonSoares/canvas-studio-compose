package com.canvasstudio.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.canvasstudio.data.local.AppDatabase
import com.canvasstudio.data.local.entity.BlockEntity
import com.canvasstudio.data.repository.OfflineBlockRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class RoomRepositoryIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: OfflineBlockRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = OfflineBlockRepository(database.blockDao(), database.cachedImageDao())
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun `insertBlock e getBlocksStream persistem e emitem blocos em tempo real`() = runTest {
        val block = BlockEntity(0, 1, "Bloco Teste", "text", 50f, 60f, 200, 150, "{\"text\":\"Ola\"}")
        repository.insertBlock(block)

        val stream = repository.getBlocksStream(1).first()
        assertEquals(1, stream.size)
        assertEquals("Bloco Teste", stream[0].title)
        assertEquals(1L, stream[0].projectId)
    }

    @Test
    fun `insertBlocks em lote insere multiplos blocos atomicamente`() = runTest {
        val blocks = listOf(
            BlockEntity(0, 1, "Bloco 1", "text", 10f, 10f, 100, 100, "{}"),
            BlockEntity(0, 1, "Bloco 2", "image", 20f, 20f, 200, 200, "{}"),
            BlockEntity(0, 1, "Bloco 3", "chart", 30f, 30f, 300, 300, "{}")
        )
        repository.insertBlocks(blocks)

        val result = repository.getBlocksStream(1).first()
        assertEquals(3, result.size)
    }

    @Test
    fun `clearCanvas remove todos os blocos do projeto especificado`() = runTest {
        repository.insertBlock(BlockEntity(0, 1, "Projeto 1 Bloco", "text", 0f, 0f, 100, 100, "{}"))
        repository.insertBlock(BlockEntity(0, 2, "Projeto 2 Bloco", "text", 0f, 0f, 100, 100, "{}"))

        repository.clearCanvas(1)

        val proj1Blocks = repository.getBlocksStream(1).first()
        val proj2Blocks = repository.getBlocksStream(2).first()

        assertTrue(proj1Blocks.isEmpty())
        assertEquals(1, proj2Blocks.size)
        assertEquals("Projeto 2 Bloco", proj2Blocks[0].title)
    }

    @Test
    fun `saveImageCache e getCachedImage persistem base64 no banco Room`() = runTest {
        repository.saveImageCache("img_123", "data:image/jpeg;base64,/9j/4AAQSkZJRg==")
        val cached = repository.getCachedImage("img_123")

        assertEquals("data:image/jpeg;base64,/9j/4AAQSkZJRg==", cached)
    }
}
