package com.canvasstudio.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_images")
data class CachedImageEntity(
    @PrimaryKey val imgId: String,
    val data: String // Base64 encoded image data (simulating DataURL)
)
