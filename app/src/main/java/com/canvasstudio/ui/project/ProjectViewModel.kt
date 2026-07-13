package com.canvasstudio.ui.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.canvasstudio.CanvasApplication
import com.canvasstudio.data.local.entity.ProjectEntity
import com.canvasstudio.data.repository.ProjectRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ProjectUiState {
    object Loading : ProjectUiState
    data class Success(val projects: List<ProjectEntity>) : ProjectUiState
    data class Error(val message: String) : ProjectUiState
}

class ProjectViewModel(
    private val projectRepository: ProjectRepository
) : ViewModel() {

    val uiState: StateFlow<ProjectUiState> = projectRepository.getAllProjects()
        .map { projects -> ProjectUiState.Success(projects) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProjectUiState.Loading
        )

    fun insertProject(name: String) {
        viewModelScope.launch {
            try {
                val currentTime = System.currentTimeMillis()
                val newProject = ProjectEntity(
                    name = name,
                    createdAt = currentTime,
                    updatedAt = currentTime
                )
                projectRepository.insertProject(newProject)
            } catch (e: Exception) {
                // Tratamento de exceção de persistência
            }
        }
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch {
            projectRepository.deleteProject(project)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as CanvasApplication)
                ProjectViewModel(application.container.projectRepository)
            }
        }
    }
}