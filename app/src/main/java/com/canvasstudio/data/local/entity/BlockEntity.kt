package com.canvasstudio.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "canvas_blocks")
data class BlockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val title: String,
    val type: String,
    val posX: Float,
    val posY: Float,
    val width: Int,
    val height: Int,
    val contentJson: String
)