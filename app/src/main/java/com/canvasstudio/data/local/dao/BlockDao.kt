package com.canvasstudio.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.canvasstudio.data.local.entity.BlockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockDao {
    @Query("SELECT * FROM canvas_blocks WHERE projectId = :projectId")
    fun getBlocksForProject(projectId: Long): Flow<List<BlockEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlock(block: BlockEntity): Long

    @Update
    suspend fun updateBlock(block: BlockEntity)

    @Delete
    suspend fun deleteBlock(block: BlockEntity)

    @Query("DELETE FROM canvas_blocks WHERE projectId = :projectId")
    suspend fun clearCanvas(projectId: Long)
}