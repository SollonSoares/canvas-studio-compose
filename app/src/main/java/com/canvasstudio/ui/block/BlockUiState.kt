package com.canvasstudio.ui.block

import com.canvasstudio.data.local.entity.BlockEntity

sealed interface BlockUiState {
    object Loading : BlockUiState
    data class Success(val blocks: List<BlockEntity>) : BlockUiState
    data class Error(val message: String) : BlockUiState
}