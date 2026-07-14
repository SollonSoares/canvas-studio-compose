package com.canvasstudio.ui.project

import com.canvasstudio.data.local.entity.ProjectEntity

sealed interface ProjectUiState {
    object Loading : ProjectUiState
    data class Success(val projects: List<ProjectEntity>) : ProjectUiState
    data class Error(val message: String) : ProjectUiState
}