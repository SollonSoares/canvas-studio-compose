package com.canvasstudio.ui.block.delegates

import com.canvasstudio.data.local.entity.BlockEntity
import kotlin.math.roundToInt

object CanvasAutoOrganizer {

    fun organize(blocks: List<BlockEntity>, canvasWidth: Int): List<BlockEntity> {
        if (blocks.isEmpty()) return emptyList()

        val sortedBlocks = blocks.sortedWith(compareBy<BlockEntity>({ it.title.lowercase() }, { it.id }))

        val padding = 20f
        val startX = 40f
        val startY = 40f
        val maxCanvasWidth = canvasWidth.toFloat() - 40f

        var currentX = startX
        var currentY = startY
        var currentRowMaxHeight = 0f

        val updatedBlocks = mutableListOf<BlockEntity>()

        sortedBlocks.forEach { block ->
            if (currentX + block.width > maxCanvasWidth && currentX > startX) {
                currentX = startX
                currentY += currentRowMaxHeight + padding
                currentRowMaxHeight = 0f
            }

            val posX = (currentX / 20f).roundToInt() * 20f
            val posY = (currentY / 20f).roundToInt() * 20f

            updatedBlocks.add(block.copy(
                posX = posX,
                posY = posY
            ))

            currentX += block.width.toFloat() + padding

            if (block.height.toFloat() > currentRowMaxHeight) {
                currentRowMaxHeight = block.height.toFloat()
            }
        }

        return updatedBlocks
    }
}
