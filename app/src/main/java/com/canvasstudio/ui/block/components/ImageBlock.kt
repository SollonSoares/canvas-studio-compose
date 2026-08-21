package com.canvasstudio.ui.block.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Link
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.canvasstudio.data.local.entity.BlockEntity
import com.canvasstudio.ui.block.BlockViewModel
import com.canvasstudio.ui.theme.CanvasColors
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.io.File

@Composable
fun ImageBlock(
    block: BlockEntity,
    viewModel: BlockViewModel,
    colors: CanvasColors
) {
    val metadata = remember(block.contentJson) {
        try { Json.parseToJsonElement(block.contentJson).jsonObject } catch (e: Exception) { null }
    }
    val url = metadata?.get("url")?.jsonPrimitive?.content ?: ""
    val imgId = metadata?.get("imgId")?.jsonPrimitive?.content

    var isEditingUrl by remember(url) { mutableStateOf(url.isEmpty()) }
    var inputUrl by remember(url) { mutableStateOf(url) }
    var cachedDataUrl by remember(imgId) { mutableStateOf<String?>(null) }

    LaunchedEffect(imgId) {
        if (imgId != null) {
            viewModel.getCachedImage(imgId) { cached ->
                if (cached != null) {
                    cachedDataUrl = cached
                } else if (url.isNotEmpty()) {
                    viewModel.cacheImage(imgId, url)
                }
            }
        }
    }

    val effectiveUrl = cachedDataUrl ?: url

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (effectiveUrl.isNotEmpty() && !isEditingUrl) {
            val imageModel = remember(effectiveUrl) {
                if (effectiveUrl.startsWith("file://")) {
                    File(effectiveUrl.removePrefix("file://"))
                } else {
                    effectiveUrl
                }
            }
            AsyncImage(
                model = imageModel,
                contentDescription = "Imagem do Bloco",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
            ) {
                IconButton(
                    onClick = { isEditingUrl = true },
                    modifier = Modifier
                        .size(26.dp)
                        .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(13.dp))
                ) {
                    Icon(
                        Icons.Rounded.Edit,
                        contentDescription = "Editar URL",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Rounded.Link, contentDescription = null, tint = colors.accent, modifier = Modifier.size(30.dp))
                Spacer(Modifier.height(3.dp))
                Text("URL DA IMAGEM", color = colors.textMain, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                Spacer(Modifier.height(5.dp))
                BasicTextField(
                    value = inputUrl,
                    onValueChange = { inputUrl = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.bgInput, RoundedCornerShape(6.dp))
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    textStyle = TextStyle(color = colors.textMain, fontSize = 11.sp),
                    cursorBrush = SolidColor(colors.accent),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (inputUrl.isNotBlank()) {
                            isEditingUrl = false
                            viewModel.updateBlock(block.copy(contentJson = buildJsonObject { put("url", inputUrl.trim()) }.toString()))
                        }
                    }),
                    decorationBox = { inner ->
                        if (inputUrl.isEmpty()) Text("https://exemplo.com/foto.jpg", color = colors.textMuted, fontSize = 11.sp)
                        inner()
                    }
                )
                Spacer(Modifier.height(6.dp))
                Button(
                    onClick = {
                        if (inputUrl.isNotBlank()) {
                            isEditingUrl = false
                            viewModel.updateBlock(block.copy(contentJson = buildJsonObject { put("url", inputUrl.trim()) }.toString()))
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = colors.accent),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth().height(28.dp)
                ) {
                    Text("Carregar Imagem", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
