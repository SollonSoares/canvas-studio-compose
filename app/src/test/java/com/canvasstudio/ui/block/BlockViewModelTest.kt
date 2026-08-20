package com.canvasstudio.ui.block

import com.canvasstudio.data.local.entity.BlockEntity
import com.canvasstudio.data.local.preferences.UserPreferencesManager
import com.canvasstudio.data.repository.BlockRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Testes Unitários de Lógica de Negócio e Portabilidade (ViewModel).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BlockViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    // Fake Repository
    private class FakeBlockRepository : BlockRepository {
        val blocks = mutableListOf<BlockEntity>()
        private val blocksFlow = MutableStateFlow<List<BlockEntity>>(emptyList())

        override fun getBlocksStream(projectId: Long): Flow<List<BlockEntity>> = blocksFlow

        override suspend fun insertBlock(block: BlockEntity) {
            blocks.add(block)
            blocksFlow.value = blocks.toList()
        }

        override suspend fun insertBlocks(newBlocks: List<BlockEntity>) {
            blocks.addAll(newBlocks)
            blocksFlow.value = blocks.toList()
        }

        override suspend fun updateBlock(block: BlockEntity) {
            val idx = blocks.indexOfFirst { it.id == block.id }
            if (idx != -1) {
                blocks[idx] = block
                blocksFlow.value = blocks.toList()
            }
        }

        override suspend fun deleteBlock(block: BlockEntity) {
            blocks.removeAll { it.id == block.id }
            blocksFlow.value = blocks.toList()
        }

        override suspend fun clearCanvas(projectId: Long) {
            blocks.clear()
            blocksFlow.value = emptyList()
        }

        override suspend fun getCachedImage(imgId: String): String? = null
        override suspend fun saveImageCache(imgId: String, data: String) {}
        override suspend fun deleteImageCache(imgId: String) {}
    }

    private class FakePreferences : UserPreferencesManager() {
        override val brandTitleFlow: Flow<String> = flowOf("Canvas Studio Pro")
        override val canvasDimensionsFlow: Flow<Pair<Int, Int>> = flowOf(2000 to 2000)
        override val darkModeFlow: Flow<Boolean> = flowOf(true)
        override val modulesStateFlow: Flow<Map<String, Boolean>> = flowOf(mapOf("text" to true, "image" to true, "chart" to true))
    }

    private lateinit var repository: FakeBlockRepository
    private lateinit var preferences: FakePreferences
    private lateinit var viewModel: BlockViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeBlockRepository()
        preferences = FakePreferences()
        viewModel = BlockViewModel(repository, preferences)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `importFromJson deve importar blocos Web corretamente`() = runTest {
        val webJson = """
        {
            "metadata": { "brand": "Naruto RPG Sheet" },
            "blocks": {
                "block_1": {
                    "type": "chart",
                    "title": "Status Ninja",
                    "top": "100px",
                    "left": "150px",
                    "width": "300px",
                    "height": "300px",
                    "inputs": {
                        "ninjutsu": 15,
                        "inteligencia": 20,
                        "chakraMax": 50,
                        "taijutsu": 10,
                        "vigor": 25,
                        "genjutsu": 5
                    }
                }
            }
        }
        """.trimIndent()

        viewModel.importFromJson(webJson)
        advanceUntilIdle()

        assertEquals(1, repository.blocks.size)
        val imported = repository.blocks.first()
        assertEquals("Status Ninja", imported.title)
        assertEquals("chart", imported.type)
        assertEquals(150f, imported.posX)
        assertEquals(100f, imported.posY)
    }

    @Test
    fun `exportToJson deve gerar estrutura compativel com Web`() = runTest {
        val block = BlockEntity(
            id = 1,
            projectId = 0,
            title = "Anotação",
            type = "text",
            posX = 50f,
            posY = 50f,
            width = 200,
            height = 150,
            contentJson = """{"text": "**Importante**"}"""
        )
        repository.insertBlock(block)
        advanceUntilIdle()

        val jsonExported = viewModel.exportToJson()
        assertTrue(jsonExported.contains("app_brand_title"))
        assertTrue(jsonExported.contains("blocks"))
    }
}
