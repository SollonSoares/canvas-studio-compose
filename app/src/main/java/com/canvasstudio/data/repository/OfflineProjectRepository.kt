package com.canvasstudio.data.repository

import com.canvasstudio.data.local.dao.ProjectDao
import com.canvasstudio.data.local.entity.ProjectEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class OfflineProjectRepository(private val projectDao: ProjectDao) : ProjectRepository {
    override fun getAllProjectsStream(): Flow<List<ProjectEntity>> = projectDao.getAllProjects()

    override suspend fun insertProject(project: ProjectEntity): Long = withContext(Dispatchers.IO) {
        projectDao.insertProject(project)
    }

    override suspend fun updateProject(project: ProjectEntity) = withContext(Dispatchers.IO) {
        projectDao.updateProject(project)
    }

    override suspend fun deleteProject(project: ProjectEntity) = withContext(Dispatchers.IO) {
        projectDao.deleteProject(project)
    }
}
