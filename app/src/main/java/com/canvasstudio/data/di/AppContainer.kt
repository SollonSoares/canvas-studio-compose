package com.canvasstudio.data.di

import android.content.Context
import androidx.room.Room
import com.canvasstudio.data.local.AppDatabase
import com.canvasstudio.data.repository.*

interface AppContainer {
    val projectRepository: ProjectRepository
    val blockRepository: BlockRepository
}

class AppContainerImpl(private val context: Context) : AppContainer {
    private val database: AppDatabase by lazy {
        Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "project_database")
            .fallbackToDestructiveMigration() // Garante que o Room recrie as tabelas se o esquema mudar
            .build()
    }
    override val projectRepository: ProjectRepository by lazy { ProjectRepositoryImpl(database.projectDao()) }
    override val blockRepository: BlockRepository by lazy { BlockRepositoryImpl(database.blockDao()) }
}