package com.canvasstudio.data.repository

import com.canvasstudio.data.local.dao.ProjectDao
import com.canvasstudio.data.local.entity.ProjectEntity
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    fun getAllProjects(): Flow<List<ProjectEntity>>
    suspend fun insertProject(project: ProjectEntity): Long
    suspend fun updateProject(project: ProjectEntity)
    suspend fun deleteProject(project: ProjectEntity)
}

class ProjectRepositoryImpl(
    private val projectDao: ProjectDao
) : ProjectRepository {

    override fun getAllProjects(): Flow<List<ProjectEntity>> {
        return projectDao.getAllProjects()
    }

    override suspend fun insertProject(project: ProjectEntity): Long {
        return projectDao.insertProject(project)
    }

    override suspend fun updateProject(project: ProjectEntity) {
        projectDao.updateProject(project)
    }

    override suspend fun deleteProject(project: ProjectEntity) {
        projectDao.deleteProject(project)
    }
}