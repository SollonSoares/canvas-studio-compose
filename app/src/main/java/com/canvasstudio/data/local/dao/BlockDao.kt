package com.canvasstudio.data.local.dao

import androidx.room.*
import com.canvasstudio.data.local.entity.BlockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockDao {
    @Query("SELECT * FROM canvas_blocks WHERE projectId = :projectId")
    fun getBlocksByProject(projectId: Long): Flow<List<BlockEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(block: BlockEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(blocks: List<BlockEntity>)

    @Update
    suspend fun update(block: BlockEntity)

    @Delete
    suspend fun delete(block: BlockEntity)

    @Query("DELETE FROM canvas_blocks WHERE projectId = :projectId")
    suspend fun clearBlocksByProject(projectId: Long)
}