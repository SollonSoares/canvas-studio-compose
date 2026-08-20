package com.canvasstudio.ui.block.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.canvasstudio.data.local.entity.BlockEntity
import com.canvasstudio.ui.block.BlockViewModel
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun ImageBlock(block: BlockEntity, viewModel: BlockViewModel) {
    val metadata = remember(block.contentJson) { 
        try { Json.parseToJsonElement(block.contentJson).jsonObject } catch (e: Exception) { null }
    }
    val url = metadata?.get("url")?.jsonPrimitive?.content ?: ""
    val imgId = metadata?.get("imgId")?.jsonPrimitive?.content
    
    var displayUrl by remember(url, imgId) { mutableStateOf(url) }
    
    LaunchedEffect(url, imgId) {
        if (imgId != null) {
            viewModel.getCachedImage(imgId) { cachedData ->
                if (cachedData != null) {
                    displayUrl = cachedData
                } else if (url.isNotEmpty()) {
                    viewModel.cacheImage(imgId, url)
                }
            }
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (displayUrl.isNotEmpty()) {
            AsyncImage(
                model = displayUrl, 
                contentDescription = null, 
                modifier = Modifier.fillMaxSize(), 
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(Icons.Default.Image, null, tint = Color.Gray, modifier = Modifier.size(32.dp))
        }
    }
}
