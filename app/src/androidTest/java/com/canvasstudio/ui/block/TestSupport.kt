package com.canvasstudio.ui.block

import com.canvasstudio.data.local.entity.BlockEntity
import com.canvasstudio.data.local.preferences.UserPreferencesManager
import com.canvasstudio.data.repository.BlockRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf

/**
 * Repositório em memória para suporte aos testes de fluxo negocial ponta a ponta (E2E).
 */
class InMemoryBlockRepository : BlockRepository {
    private val blocksList = mutableListOf<BlockEntity>()
    private val _blocksFlow = MutableStateFlow<List<BlockEntity>>(emptyList())
    private var nextId = 1L

    override fun getBlocksStream(projectId: Long): Flow<List<BlockEntity>> = _blocksFlow.asStateFlow()

    override suspend fun insertBlock(block: BlockEntity) {
        val assignedBlock = if (block.id == 0L) block.copy(id = nextId++) else block
        blocksList.add(assignedBlock)
        _blocksFlow.value = blocksList.toList()
    }

    override suspend fun insertBlocks(blocks: List<BlockEntity>) {
        val assigned = blocks.map { if (it.id == 0L) it.copy(id = nextId++) else it }
        blocksList.addAll(assigned)
        _blocksFlow.value = blocksList.toList()
    }

    override suspend fun updateBlock(block: BlockEntity) {
        val index = blocksList.indexOfFirst { it.id == block.id }
        if (index != -1) {
            blocksList[index] = block
            _blocksFlow.value = blocksList.toList()
        }
    }

    override suspend fun deleteBlock(block: BlockEntity) {
        blocksList.removeAll { it.id == block.id }
        _blocksFlow.value = blocksList.toList()
    }

    override suspend fun clearCanvas(projectId: Long) {
        blocksList.clear()
        _blocksFlow.value = emptyList()
    }

    override suspend fun getCachedImage(imgId: String): String? = null
    override suspend fun saveImageCache(imgId: String, data: String) {}
    override suspend fun deleteImageCache(imgId: String) {}
}

/**
 * Gerenciador de preferências em memória para testes.
 */
class InMemoryPreferencesManager : UserPreferencesManager() {
    private val _darkMode = MutableStateFlow(false)
    private val _gridEnabled = MutableStateFlow(true)
    private val _isLocked = MutableStateFlow(false)
    private val _brandTitle = MutableStateFlow("Canvas Studio")
    private val _modules = MutableStateFlow(mapOf("text" to true, "image" to true, "chart" to true))

    override val darkModeFlow: Flow<Boolean> = _darkMode.asStateFlow()
    override val gridEnabledFlow: Flow<Boolean> = _gridEnabled.asStateFlow()
    override val isLockedFlow: Flow<Boolean> = _isLocked.asStateFlow()
    override val brandTitleFlow: Flow<String> = _brandTitle.asStateFlow()
    override val canvasDimensionsFlow: Flow<Pair<Int, Int>> = flowOf(2000 to 2000)
    override val modulesStateFlow: Flow<Map<String, Boolean>> = _modules.asStateFlow()

    override suspend fun setDarkMode(enabled: Boolean) { _darkMode.value = enabled }
    override suspend fun setGridEnabled(enabled: Boolean) { _gridEnabled.value = enabled }
    override suspend fun setLocked(locked: Boolean) { _isLocked.value = locked }
    override suspend fun setBrandTitle(title: String) { _brandTitle.value = title }
    override suspend fun setModuleEnabled(module: String, enabled: Boolean) {
        _modules.value = _modules.value.toMutableMap().apply { put(module, enabled) }
    }
}
