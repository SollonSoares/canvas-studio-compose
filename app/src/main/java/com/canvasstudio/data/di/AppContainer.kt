package com.canvasstudio.data.di
import android.content.Context
import androidx.room.Room
import com.canvasstudio.data.local.AppDatabase
import com.canvasstudio.data.repository.ProjectRepository
import com.canvasstudio.data.repository.ProjectRepositoryImpl
interface AppContainer {
    val projectRepository: ProjectRepository
}
class AppContainerImpl(private val context: Context) : AppContainer {
    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "project_database"
        ).build()
    }
    override val projectRepository: ProjectRepository by lazy {
        ProjectRepositoryImpl(database.projectDao())
    }
}
