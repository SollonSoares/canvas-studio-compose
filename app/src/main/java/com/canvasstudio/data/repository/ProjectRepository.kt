package com.canvasstudio.data.repository

import com.canvasstudio.data.local.entity.ProjectEntity
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    fun getAllProjectsStream(): Flow<List<ProjectEntity>>
    suspend fun insertProject(project: ProjectEntity): Long
    suspend fun updateProject(project: ProjectEntity)
    suspend fun deleteProject(project: ProjectEntity)
}
