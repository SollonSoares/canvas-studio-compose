package com.canvasstudio.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.canvasstudio.data.local.dao.ProjectDao
import com.canvasstudio.data.local.dao.BlockDao
import com.canvasstudio.data.local.entity.ProjectEntity
import com.canvasstudio.data.local.entity.BlockEntity

@Database(entities = [ProjectEntity::class, BlockEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun blockDao(): BlockDao
}
