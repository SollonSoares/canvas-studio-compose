package com.canvasstudio.ui.block

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.canvasstudio.CanvasApplication
import com.canvasstudio.data.local.entity.BlockEntity
import com.canvasstudio.data.repository.BlockRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface BlockUiState {
    object Loading : BlockUiState
    data class Success(val blocks: List<BlockEntity>) : BlockUiState
    data class Error(val message: String) : BlockUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class BlockViewModel(private val blockRepository: BlockRepository) : ViewModel() {

    private val _currentProjectId = MutableStateFlow<Long?>(null)

    // O fluxo é instanciado uma única vez e reage às mudanças do ID do projeto
    val uiState: StateFlow<BlockUiState> = _currentProjectId
        .flatMapLatest { projectId ->
            if (projectId == null) {
                kotlinx.coroutines.flow.flowOf(BlockUiState.Success(emptyList()))
            } else {
                blockRepository.getBlocksForProject(projectId).map { BlockUiState.Success(it) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BlockUiState.Loading
        )

    fun setProject(projectId: Long) {
        _currentProjectId.value = projectId
    }

    fun insertBlock(block: BlockEntity) { viewModelScope.launch { try { blockRepository.insertBlock(block) } catch (e: Exception) {} } }
    fun updateBlock(block: BlockEntity) { viewModelScope.launch { try { blockRepository.updateBlock(block) } catch (e: Exception) {} } }
    fun deleteBlock(block: BlockEntity) { viewModelScope.launch { try { blockRepository.deleteBlock(block) } catch (e: Exception) {} } }
    fun clearCanvas(projectId: Long) { viewModelScope.launch { try { blockRepository.clearCanvas(projectId) } catch (e: Exception) {} } }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as CanvasApplication)
                BlockViewModel(application.container.blockRepository)
            }
        }
    }
}