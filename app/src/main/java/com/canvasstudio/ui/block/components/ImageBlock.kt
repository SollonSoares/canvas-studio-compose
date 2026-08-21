package com.canvasstudio.ui.block.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Edit
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
    
    var displayUrl by remember(url, imgId) { mutableStateOf(url) }
    var isEditingUrl by remember { mutableStateOf(url.isEmpty()) }
    var inputUrl by remember(url) { mutableStateOf(url) }
    
    LaunchedEffect(url, imgId) {
        if (imgId != null) {
            viewModel.getCachedImage(imgId) { cachedData ->
                if (cachedData != null) {
                    displayUrl = cachedData
                } else if (url.isNotEmpty()) {
                    viewModel.cacheImage(imgId, url)
                }
            }
        } else {
            displayUrl = url
        }
        if (url.isNotEmpty()) {
            isEditingUrl = false
            inputUrl = url
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val galleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { selectedUri: android.net.Uri? ->
        selectedUri?.let { uri ->
            isEditingUrl = false
            viewModel.updateBlockImageFromUri(block, uri, context)
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (displayUrl.isNotEmpty() && !isEditingUrl) {
            AsyncImage(
                model = displayUrl, 
                contentDescription = null, 
                modifier = Modifier.fillMaxSize(), 
                contentScale = ContentScale.Fit
            )
            // Botão sutil de edição de URL no topo da imagem
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
            // UI para entrada direta de URL da imagem e escolha da galeria
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Rounded.AddPhotoAlternate, 
                    contentDescription = null, 
                    tint = colors.accent, 
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    "INSERIR IMAGEM", 
                    color = colors.textMain, 
                    fontSize = 11.sp, 
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(Modifier.height(5.dp))
                BasicTextField(
                    value = inputUrl,
                    onValueChange = { inputUrl = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.bgInput, RoundedCornerShape(6.dp))
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    textStyle = TextStyle(color = colors.textMain, fontSize = 10.sp),
                    cursorBrush = SolidColor(colors.accent),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (inputUrl.isNotBlank()) {
                                isEditingUrl = false
                                viewModel.updateBlock(block.copy(contentJson = buildJsonObject { put("url", inputUrl) }.toString()))
                            }
                        }
                    ),
                    decorationBox = { inner ->
                        if (inputUrl.isEmpty()) {
                            Text("URL da Imagem (https://...)", color = colors.textMuted, fontSize = 10.sp)
                        }
                        inner()
                    }
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f).height(28.dp)
                    ) {
                        Text("Galeria", color = colors.accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            if (inputUrl.isNotBlank()) {
                                isEditingUrl = false
                                viewModel.updateBlock(block.copy(contentJson = buildJsonObject { put("url", inputUrl) }.toString()))
                            }
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = colors.accent),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f).height(28.dp)
                    ) {
                        Text("Carregar", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
