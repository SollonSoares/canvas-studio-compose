package com.canvasstudio.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.canvasstudio.data.local.dao.ProjectDao
import com.canvasstudio.data.local.dao.BlockDao
import com.canvasstudio.data.local.dao.CachedImageDao
import com.canvasstudio.data.local.entity.ProjectEntity
import com.canvasstudio.data.local.entity.BlockEntity
import com.canvasstudio.data.local.entity.CachedImageEntity

@Database(entities = [ProjectEntity::class, BlockEntity::class, CachedImageEntity::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun blockDao(): BlockDao
    abstract fun cachedImageDao(): CachedImageDao
}
