package com.canvasstudio.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.canvasstudio.data.local.AppDatabase
import com.canvasstudio.data.local.preferences.UserPreferencesManager
import com.canvasstudio.data.repository.OfflineBlockRepository
import com.canvasstudio.ui.block.BlockUiState
import com.canvasstudio.ui.block.BlockViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class MediaSharingIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: OfflineBlockRepository
    private lateinit var viewModel: BlockViewModel
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = OfflineBlockRepository(database.blockDao(), database.cachedImageDao())
        val preferencesManager = UserPreferencesManager(context)
        viewModel = BlockViewModel(repository, preferencesManager)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun `importTextShared de comprovante PIX processa e insere bloco estruturado no repositorio`() = runTest {
        val pixSharedText = """
            COMPROVANTE DE TRANSFERÊNCIA
            De: JOÃO PEDRO SANTOS
            Para: ANA CLARA ALMEIDA
            Valor: R$ 450,00
            Data: 21/08/2026
            Banco: Itaú
        """.trimIndent()

        viewModel.importTextShared(pixSharedText, subject = "Comprovante Pix")
        advanceUntilIdle()

        val blocks = repository.getBlocksStream(0).first()
        assertEquals(1, blocks.size)

        val block = blocks[0]
        assertEquals("text", block.type)
        assertTrue(block.title.contains("ANA CLARA ALMEIDA") || block.title.contains("Pix"))

        val content = Json.parseToJsonElement(block.contentJson).jsonObject
        assertEquals(450.00f, content["valor"]?.jsonPrimitive?.floatOrNull)
        assertEquals("JOÃO PEDRO SANTOS", content["pagador"]?.jsonPrimitive?.content)
        assertEquals("ANA CLARA ALMEIDA", content["destinatario"]?.jsonPrimitive?.content)
    }

    @Test
    fun `importTextShared de texto comum cria bloco de texto com tamanho default`() = runTest {
        viewModel.importTextShared("Lista de compras da semana:\n- Kunai\n- Pergaminho")
        advanceUntilIdle()

        val blocks = repository.getBlocksStream(0).first()
        assertEquals(1, blocks.size)

        val block = blocks[0]
        assertEquals("text", block.type)
        val content = Json.parseToJsonElement(block.contentJson).jsonObject
        assertTrue(content["text"]?.jsonPrimitive?.content?.contains("Kunai") == true)
    }
}
