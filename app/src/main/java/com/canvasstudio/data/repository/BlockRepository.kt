package com.canvasstudio.data.repository

import com.canvasstudio.data.local.dao.BlockDao
import com.canvasstudio.data.local.entity.BlockEntity
import kotlinx.coroutines.flow.Flow

interface BlockRepository {
    fun getBlocksForProject(projectId: Long): Flow<List<BlockEntity>>
    suspend fun insertBlock(block: BlockEntity): Long
    suspend fun updateBlock(block: BlockEntity)
    suspend fun deleteBlock(block: BlockEntity)
    suspend fun clearCanvas(projectId: Long)
}

class BlockRepositoryImpl(
    private val blockDao: BlockDao
) : BlockRepository {

    override fun getBlocksForProject(projectId: Long): Flow<List<BlockEntity>> {
        return blockDao.getBlocksForProject(projectId)
    }

    override suspend fun insertBlock(block: BlockEntity): Long {
        return blockDao.insertBlock(block)
    }

    override suspend fun updateBlock(block: BlockEntity) {
        blockDao.updateBlock(block)
    }

    override suspend fun deleteBlock(block: BlockEntity) {
        blockDao.deleteBlock(block)
    }

    override suspend fun clearCanvas(projectId: Long) {
        blockDao.clearCanvas(projectId)
    }
}