package com.canvasstudio.data.repository

import com.canvasstudio.data.local.entity.BlockEntity
import kotlinx.coroutines.flow.Flow

interface BlockRepository {
    fun getBlocksStream(projectId: Long): Flow<List<BlockEntity>>
    suspend fun insertBlock(block: BlockEntity)
    suspend fun insertBlocks(blocks: List<BlockEntity>)
    suspend fun updateBlock(block: BlockEntity)
    suspend fun deleteBlock(block: BlockEntity)
    suspend fun clearCanvas(projectId: Long)
    
    // Image Caching
    suspend fun getCachedImage(imgId: String): String?
    suspend fun saveImageCache(imgId: String, data: String)
    suspend fun deleteImageCache(imgId: String)
}
