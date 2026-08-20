package com.canvasstudio.data.di

import android.content.Context
import androidx.room.Room
import com.canvasstudio.data.local.AppDatabase
import com.canvasstudio.data.local.preferences.UserPreferencesManager
import com.canvasstudio.data.repository.BlockRepository
import com.canvasstudio.data.repository.OfflineBlockRepository
import com.canvasstudio.data.repository.ProjectRepository
import com.canvasstudio.data.repository.OfflineProjectRepository

interface AppContainer {
    val blockRepository: BlockRepository
    val projectRepository: ProjectRepository
    val userPreferencesManager: UserPreferencesManager
}

class AppContainerImpl(context: Context) : AppContainer {
    private val appContext = context.applicationContext

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "canvas_db"
        ).fallbackToDestructiveMigration().build()
    }

    override val blockRepository: BlockRepository by lazy { 
        OfflineBlockRepository(database.blockDao(), database.cachedImageDao())
    }
    
    override val projectRepository: ProjectRepository by lazy { 
        OfflineProjectRepository(database.projectDao()) 
    }
    
    override val userPreferencesManager: UserPreferencesManager by lazy {
        UserPreferencesManager(appContext)
    }
}
