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
 
class ProjectViewModel(private val projectRepository: ProjectRepository) : ViewModel() { 
    val uiState: StateFlow<ProjectUiState> = projectRepository.getAllProjects().map { ProjectUiState.Success(it) }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = ProjectUiState.Loading) 
    fun insertProject(name: String) { viewModelScope.launch { try { projectRepository.insertProject(ProjectEntity(name = name, createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis())) } catch (e: Exception) {} } } 
    fun deleteProject(project: ProjectEntity) { viewModelScope.launch { try { projectRepository.deleteProject(project) } catch (e: Exception) {} } } 
    companion object { val Factory: ViewModelProvider.Factory = viewModelFactory { initializer { val application = (this[APPLICATION_KEY] as CanvasApplication); ProjectViewModel(application.container.projectRepository) } } } 
} 
