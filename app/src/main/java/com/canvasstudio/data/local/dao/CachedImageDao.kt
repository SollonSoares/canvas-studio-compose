package com.canvasstudio.data.local.dao

import androidx.room.*
import com.canvasstudio.data.local.entity.CachedImageEntity

@Dao
interface CachedImageDao {
    @Query("SELECT * FROM cached_images WHERE imgId = :imgId")
    suspend fun getCachedImage(imgId: String): CachedImageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cachedImage: CachedImageEntity)

    @Query("DELETE FROM cached_images WHERE imgId = :imgId")
    suspend fun delete(imgId: String)
}
