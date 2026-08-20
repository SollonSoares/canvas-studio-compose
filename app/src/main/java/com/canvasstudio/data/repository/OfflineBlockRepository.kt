package com.canvasstudio.data.repository

import com.canvasstudio.data.local.dao.BlockDao
import com.canvasstudio.data.local.dao.CachedImageDao
import com.canvasstudio.data.local.entity.BlockEntity
import com.canvasstudio.data.local.entity.CachedImageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class OfflineBlockRepository(
    private val blockDao: BlockDao,
    private val cachedImageDao: CachedImageDao
) : BlockRepository {
    override fun getBlocksStream(projectId: Long): Flow<List<BlockEntity>> = 
        blockDao.getBlocksByProject(projectId)

    override suspend fun insertBlock(block: BlockEntity) = withContext(Dispatchers.IO) {
        blockDao.insert(block)
    }

    override suspend fun insertBlocks(blocks: List<BlockEntity>) = withContext(Dispatchers.IO) {
        blockDao.insertAll(blocks)
    }

    override suspend fun updateBlock(block: BlockEntity) = withContext(Dispatchers.IO) {
        blockDao.update(block)
    }

    override suspend fun deleteBlock(block: BlockEntity) = withContext(Dispatchers.IO) {
        blockDao.delete(block)
    }

    override suspend fun clearCanvas(projectId: Long) = withContext(Dispatchers.IO) {
        blockDao.clearBlocksByProject(projectId)
    }

    override suspend fun getCachedImage(imgId: String): String? = withContext(Dispatchers.IO) {
        cachedImageDao.getCachedImage(imgId)?.data
    }

    override suspend fun saveImageCache(imgId: String, data: String) = withContext(Dispatchers.IO) {
        cachedImageDao.insert(CachedImageEntity(imgId, data))
    }

    override suspend fun deleteImageCache(imgId: String) = withContext(Dispatchers.IO) {
        cachedImageDao.delete(imgId)
    }
}
