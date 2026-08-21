package com.canvasstudio.ui.block

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.canvasstudio.data.repository.BlockRepository
import com.canvasstudio.data.local.entity.BlockEntity
import com.canvasstudio.data.local.preferences.UserPreferencesManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.*
import java.net.URL
import java.util.Base64
import android.util.Log
import kotlin.math.roundToInt

class BlockViewModel(
    private val blockRepository: BlockRepository,
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {
    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _appliedQuery = MutableStateFlow("")
    val appliedQuery: StateFlow<String> = _appliedQuery.asStateFlow()

    private val searchableCache = mutableMapOf<Long, String>()
    private val contentHashCache = mutableMapOf<Long, Int>()

    val modulesState = userPreferencesManager.modulesStateFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val brandTitle = userPreferencesManager.brandTitleFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Canvas Studio")

    val isDarkMode = userPreferencesManager.darkModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isGridEnabled = userPreferencesManager.gridEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isLocked = userPreferencesManager.isLockedFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val canvasDimensions = userPreferencesManager.canvasDimensionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(2000, 2000))

    fun toggleDarkMode() = viewModelScope.launch {
        userPreferencesManager.setDarkMode(!isDarkMode.value)
    }

    fun toggleGrid() = viewModelScope.launch {
        userPreferencesManager.setGridEnabled(!isGridEnabled.value)
    }

    fun toggleLock() = viewModelScope.launch {
        userPreferencesManager.setLocked(!isLocked.value)
    }

    fun setBrandTitle(title: String) = viewModelScope.launch {
        userPreferencesManager.setBrandTitle(title)
    }

    fun setCanvasDimensions(width: Int, height: Int) = viewModelScope.launch {
        userPreferencesManager.setCanvasDimensions(width, height)
    }

    private val _currentProjectId = MutableStateFlow(0L)
    val currentProjectId: StateFlow<Long> = _currentProjectId.asStateFlow()

    private val _selectedBlockId = MutableStateFlow<Long?>(null)
    val selectedBlockId: StateFlow<Long?> = _selectedBlockId.asStateFlow()

    fun selectBlock(block: BlockEntity?) {
        _selectedBlockId.value = block?.id
    }

    fun selectBlockById(id: Long?) {
        _selectedBlockId.value = id
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val selectedBlock: StateFlow<BlockEntity?> = combine(
        _currentProjectId.flatMapLatest { id -> blockRepository.getBlocksStream(id) },
        _selectedBlockId
    ) { blocks, selId ->
        if (selId == null) null else blocks.find { it.id == selId }
    }
    .flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val uiState = combine(
        _currentProjectId.flatMapLatest { id -> blockRepository.getBlocksStream(id) },
        _appliedQuery,
        userPreferencesManager.modulesStateFlow
    ) { blocks, query, modules ->
        val filteredByModule = blocks.filter { modules[it.type.lowercase()] ?: true }
        
        if (query.isBlank()) {
            BlockUiState.Success(filteredByModule)
        } else {
            val words = query.normalize().split("\\s+".toRegex()).filter { it.isNotEmpty() }
            
            val filteredBySearch = filteredByModule.filter { block ->
                val searchableText = getSearchableText(block)
                words.all { word -> searchableText.contains(word) }
            }
            BlockUiState.Success(filteredBySearch)
        }
    }
    .flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BlockUiState.Loading)

    fun toggleModule(type: String, enabled: Boolean) = viewModelScope.launch {
        userPreferencesManager.setModuleEnabled(type, enabled)
    }

    private fun getSearchableText(block: BlockEntity): String {
        val contentHash = block.title.hashCode() + block.contentJson.hashCode()
        if (contentHashCache[block.id] == contentHash) {
            return searchableCache[block.id] ?: ""
        }
        
        val contentText = try {
            val element = Json.parseToJsonElement(block.contentJson)
            extractTextFromJson(element)
        } catch (e: Exception) {
            block.contentJson
        }
        
        // Limpeza profunda de tags e formatação para busca pura
        val cleanContent = contentText
            .replace(Regex("(?s)<[^>]*>"), "")
            .replace(Regex("\\*\\*|\\*|__|_|~~"), "")
            .replace(Regex("\\[size=.*?]|\\[/size]"), "")
            .replace("|", " ")
            .normalize()
        
        val cleanTitle = block.title.normalize()
        val result = "$cleanTitle $cleanContent"
        
        contentHashCache[block.id] = contentHash
        searchableCache[block.id] = result
        return result
    }

    private fun String.normalize(): String {
        return java.text.Normalizer.normalize(this, java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .lowercase()
            .trim()
    }

    private fun extractTextFromJson(element: JsonElement): String {
        return when (element) {
            is JsonPrimitive -> element.content
            is JsonObject -> {
                // Focamos APENAS em campos de conteúdo real e visível
                // Isso evita que termos técnicos como "left", "true", "align" gerem falsos positivos
                val searchableKeys = listOf("text", "html", "title", "url", "ninjutsu", "inteligencia", "chakra", "taijutsu", "vigor", "genjutsu", "value", "headers", "rows")
                element.filter { it.key.lowercase() in searchableKeys }
                    .values.joinToString(" ") { extractTextFromJson(it) }
            }
            is JsonArray -> element.joinToString(" ") { extractTextFromJson(it) }
            is JsonNull -> ""
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        // Se o usuário limpar o campo X, volta todos os blocos imediatamente
        if (query.isBlank()) {
            _appliedQuery.value = ""
        }
    }

    fun applySearch() {
        _appliedQuery.value = _searchQuery.value
    }

    fun importSharedUri(uri: Uri, intentType: String, context: android.content.Context) = viewModelScope.launch(Dispatchers.IO) {
        try {
            Log.d("CanvasStudio", "importSharedUri starting: uri=$uri, intentType=$intentType")
            
            var fileName = "Comprovante"
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0 && cursor.moveToFirst()) {
                        val name = cursor.getString(nameIndex)
                        if (!name.isNullOrBlank()) fileName = name
                    }
                }
            } catch (e: Exception) {
                Log.w("CanvasStudio", "Could not query file name: ${e.message}")
            }

            val mime = try { context.contentResolver.getType(uri) ?: intentType } catch (e: Exception) { intentType }
            Log.d("CanvasStudio", "Resolved fileName='$fileName', mime='$mime'")

            val imagesDir = java.io.File(context.filesDir, "canvas_images").apply { mkdirs() }

            // 1. Tentar como PDF se o MIME ou nome indicar PDF ou octet-stream
            val isPdfCandidate = mime.contains("pdf", true) || fileName.endsWith(".pdf", true) || mime == "application/octet-stream" || mime == "*/*" || mime.isEmpty()
            var savedFileUri: String? = null
            var blockWidth = 280
            var blockHeight = 380
            var extractedOcrText = ""

            if (isPdfCandidate) {
                val tempPdf = java.io.File(context.cacheDir, "temp_shared_${System.currentTimeMillis()}.pdf")
                try {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        java.io.FileOutputStream(tempPdf).use { output ->
                            input.copyTo(output)
                        }
                    }
                    if (tempPdf.exists() && tempPdf.length() > 0) {
                        val pfd = android.os.ParcelFileDescriptor.open(tempPdf, android.os.ParcelFileDescriptor.MODE_READ_ONLY)
                        val renderer = android.graphics.pdf.PdfRenderer(pfd)
                        if (renderer.pageCount > 0) {
                            val page = renderer.openPage(0)
                            val scale = 2f
                            val bW = (page.width * scale).toInt().coerceAtLeast(600)
                            val bH = (page.height * scale).toInt().coerceAtLeast(800)
                            val bitmap = android.graphics.Bitmap.createBitmap(bW, bH, android.graphics.Bitmap.Config.ARGB_8888)
                            val canvas = android.graphics.Canvas(bitmap)
                            canvas.drawColor(android.graphics.Color.WHITE)
                            page.render(bitmap, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            page.close()
                            renderer.close()
                            pfd.close()

                            // OCR On-Device no comprovante renderizado
                            extractedOcrText = com.canvasstudio.ui.block.utils.ReceiptAnalyzer.extractTextFromBitmap(bitmap)

                            val destFile = java.io.File(imagesDir, "comprovante_${System.currentTimeMillis()}.jpg")
                            java.io.FileOutputStream(destFile).use { out ->
                                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 92, out)
                            }
                            savedFileUri = "file://${destFile.absolutePath}"
                            val ratio = bH.toFloat() / bW.toFloat()
                            blockWidth = 320
                            blockHeight = (320 * ratio).toInt() + 65
                            Log.d("CanvasStudio", "PDF rendered and analyzed successfully to $savedFileUri")
                        } else {
                            renderer.close()
                            pfd.close()
                        }
                    }
                } catch (e: Exception) {
                    Log.w("CanvasStudio", "PdfRenderer attempt failed: ${e.message}")
                } finally {
                    tempPdf.delete()
                }
            }

            // 2. Se não era PDF ou falhou, tentar como Imagem (Bitmap)
            if (savedFileUri == null) {
                try {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        val bitmap = android.graphics.BitmapFactory.decodeStream(input)
                        if (bitmap != null) {
                            extractedOcrText = com.canvasstudio.ui.block.utils.ReceiptAnalyzer.extractTextFromBitmap(bitmap)
                            
                            val maxDim = 1920
                            val w = bitmap.width
                            val h = bitmap.height
                            val scaled = if (w > maxDim || h > maxDim) {
                                val r = w.toFloat() / h.toFloat()
                                val (nw, nh) = if (w > h) Pair(maxDim, (maxDim / r).toInt()) else Pair((maxDim * r).toInt(), maxDim)
                                android.graphics.Bitmap.createScaledBitmap(bitmap, nw, nh, true)
                            } else bitmap

                            val destFile = java.io.File(imagesDir, "img_${System.currentTimeMillis()}.jpg")
                            java.io.FileOutputStream(destFile).use { out ->
                                scaled.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                            }
                            savedFileUri = "file://${destFile.absolutePath}"
                            val ratio = h.toFloat() / w.toFloat().coerceAtLeast(0.4f)
                            blockWidth = 320
                            blockHeight = (320 * ratio).toInt() + 65
                            Log.d("CanvasStudio", "Image saved and analyzed successfully to $savedFileUri")
                        }
                    }
                } catch (e: Exception) {
                    Log.w("CanvasStudio", "Bitmap decode attempt failed: ${e.message}")
                }
            }

            // 3. Análise Inteligente de Descrição (Título), Valor, Realizado em, Destinatário e Instituição via OCR
            val analysis = com.canvasstudio.ui.block.utils.ReceiptAnalyzer.analyze(extractedOcrText, fileName)
            val finalTitle = analysis.title
            val finalValue = analysis.value
            val finalValueFormatted = analysis.valueFormatted
            val finalRealizadoEm = analysis.realizadoEm

            val count = (uiState.value as? BlockUiState.Success)?.blocks?.size ?: 0
            val spawnX = 60f + (count % 5) * 35f
            val spawnY = 60f + (count % 5) * 35f

            val block = if (savedFileUri != null) {
                val content = buildJsonObject {
                    put("url", savedFileUri)
                    if (finalValue != null) put("valor", finalValue)
                    if (finalValueFormatted != null) put("valorFormatted", finalValueFormatted)
                    put("realizadoEm", finalRealizadoEm)
                    if (analysis.isPix) put("isPix", true)
                    if (analysis.pagador != null) put("pagador", analysis.pagador)
                    if (analysis.destinatario != null) put("destinatario", analysis.destinatario)
                    if (analysis.instituicao != null) put("instituicao", analysis.instituicao)
                    if (extractedOcrText.isNotBlank()) put("rawText", extractedOcrText)
                }.toString()
                BlockEntity(
                    projectId = _currentProjectId.value,
                    title = finalTitle,
                    type = "image",
                    posX = spawnX,
                    posY = spawnY,
                    width = blockWidth,
                    height = blockHeight,
                    contentJson = content
                )
            } else {
                val content = buildJsonObject {
                    if (finalValue != null) put("valor", finalValue)
                    if (finalValueFormatted != null) put("valorFormatted", finalValueFormatted)
                    put("realizadoEm", finalRealizadoEm)
                    if (analysis.isPix) put("isPix", true)
                    if (analysis.pagador != null) put("pagador", analysis.pagador)
                    if (analysis.destinatario != null) put("destinatario", analysis.destinatario)
                    if (analysis.instituicao != null) put("instituicao", analysis.instituicao)
                    val deStr = if (analysis.pagador != null) "\nDe (Pagador): ${analysis.pagador}" else ""
                    val paraStr = if (analysis.destinatario != null) "\nPara (Destinatário): ${analysis.destinatario}" else ""
                    val instStr = if (analysis.instituicao != null) "\nInstituição: ${analysis.instituicao}" else ""
                    put("text", "📄 **$finalTitle**\n\nValor: ${finalValueFormatted ?: "N/D"}\nRealizado em: $finalRealizadoEm$deStr$paraStr$instStr\n\n$extractedOcrText")
                    put("fontSize", 13)
                    put("align", "left")
                }.toString()
                BlockEntity(
                    projectId = _currentProjectId.value,
                    title = finalTitle,
                    type = "text",
                    posX = spawnX,
                    posY = spawnY,
                    width = 260,
                    height = 180,
                    contentJson = content
                )
            }

            blockRepository.insertBlock(block)
            Log.d("CanvasStudio", "Inserted block ${block.title} into DB with valor=$finalValue, de=${analysis.pagador}, para=${analysis.destinatario}, inst=${analysis.instituicao}")
            val msg = if (finalValueFormatted != null) "Comprovante '$finalTitle' ($finalValueFormatted) adicionado!" else "Item '$finalTitle' adicionado ao Canvas!"
            _events.emit(msg)
        } catch (e: Exception) {
            Log.e("CanvasStudio", "Error in importSharedUri: ${e.message}", e)
            _events.emit("Erro ao importar item compartilhado: ${e.message}")
        }
    }

    fun importTextShared(text: String, subject: String? = null) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val analysis = com.canvasstudio.ui.block.utils.ReceiptAnalyzer.analyze(text, subject ?: "")
            val finalTitle = analysis.title
            val finalValue = analysis.value
            val finalValueFormatted = analysis.valueFormatted
            val finalRealizadoEm = analysis.realizadoEm

            val count = (uiState.value as? BlockUiState.Success)?.blocks?.size ?: 0
            val spawnX = 60f + (count % 5) * 35f
            val spawnY = 60f + (count % 5) * 35f

            val content = buildJsonObject {
                if (finalValue != null) put("valor", finalValue)
                if (finalValueFormatted != null) put("valorFormatted", finalValueFormatted)
                put("realizadoEm", finalRealizadoEm)
                if (analysis.isPix) put("isPix", true)
                if (analysis.pagador != null) put("pagador", analysis.pagador)
                if (analysis.destinatario != null) put("destinatario", analysis.destinatario)
                if (analysis.instituicao != null) put("instituicao", analysis.instituicao)
                put("text", text)
                put("fontSize", 12)
                put("align", "left")
            }.toString()

            val block = BlockEntity(
                projectId = _currentProjectId.value,
                title = finalTitle,
                type = "text",
                posX = spawnX,
                posY = spawnY,
                width = 280,
                height = 200,
                contentJson = content
            )
            blockRepository.insertBlock(block)
            val msg = if (finalValueFormatted != null) "Comprovante '$finalTitle' ($finalValueFormatted) adicionado!" else "Texto compartilhado adicionado ao Canvas!"
            _events.emit(msg)
        } catch (e: Exception) {
            _events.emit("Erro ao processar texto compartilhado: ${e.message}")
        }
    }

    fun updateSelectedValue(newValue: Float?) {
        val selected = selectedBlock.value ?: return
        try {
            val json = try { Json.parseToJsonElement(selected.contentJson).jsonObject } catch (e: Exception) { JsonObject(emptyMap()) }
            val mutableMap = json.toMutableMap()
            if (newValue != null && newValue > 0f) {
                mutableMap["valor"] = JsonPrimitive(newValue)
                mutableMap["valorFormatted"] = JsonPrimitive(com.canvasstudio.ui.block.utils.ReceiptAnalyzer.formatCurrency(newValue))
            } else {
                mutableMap.remove("valor")
                mutableMap.remove("valorFormatted")
            }
            updateBlock(selected.copy(contentJson = JsonObject(mutableMap).toString()))
        } catch (e: Exception) {
            Log.e("BlockViewModel", "Error updating value: ${e.message}", e)
        }
    }

    fun updateSelectedRealizadoEm(newDate: String) {
        val selected = selectedBlock.value ?: return
        try {
            val json = try { Json.parseToJsonElement(selected.contentJson).jsonObject } catch (e: Exception) { JsonObject(emptyMap()) }
            val mutableMap = json.toMutableMap()
            if (newDate.isNotBlank()) {
                mutableMap["realizadoEm"] = JsonPrimitive(newDate)
            } else {
                mutableMap.remove("realizadoEm")
            }
            updateBlock(selected.copy(contentJson = JsonObject(mutableMap).toString()))
        } catch (e: Exception) {
            Log.e("BlockViewModel", "Error updating realizadoEm: ${e.message}", e)
        }
    }

    fun updateSelectedPagador(pagador: String) {
        val selected = selectedBlock.value ?: return
        try {
            val json = try { Json.parseToJsonElement(selected.contentJson).jsonObject } catch (e: Exception) { JsonObject(emptyMap()) }
            val mutableMap = json.toMutableMap()
            if (pagador.isNotBlank()) {
                mutableMap["pagador"] = JsonPrimitive(pagador)
            } else {
                mutableMap.remove("pagador")
            }
            updateBlock(selected.copy(contentJson = JsonObject(mutableMap).toString()))
        } catch (e: Exception) {
            Log.e("BlockViewModel", "Error updating pagador: ${e.message}", e)
        }
    }

    fun updateSelectedDestinatario(destinatario: String) {
        val selected = selectedBlock.value ?: return
        try {
            val json = try { Json.parseToJsonElement(selected.contentJson).jsonObject } catch (e: Exception) { JsonObject(emptyMap()) }
            val mutableMap = json.toMutableMap()
            if (destinatario.isNotBlank()) {
                mutableMap["destinatario"] = JsonPrimitive(destinatario)
            } else {
                mutableMap.remove("destinatario")
            }
            updateBlock(selected.copy(contentJson = JsonObject(mutableMap).toString()))
        } catch (e: Exception) {
            Log.e("BlockViewModel", "Error updating destinatario: ${e.message}", e)
        }
    }

    fun updateSelectedInstituicao(instituicao: String) {
        val selected = selectedBlock.value ?: return
        try {
            val json = try { Json.parseToJsonElement(selected.contentJson).jsonObject } catch (e: Exception) { JsonObject(emptyMap()) }
            val mutableMap = json.toMutableMap()
            if (instituicao.isNotBlank()) {
                mutableMap["instituicao"] = JsonPrimitive(instituicao)
            } else {
                mutableMap.remove("instituicao")
            }
            updateBlock(selected.copy(contentJson = JsonObject(mutableMap).toString()))
        } catch (e: Exception) {
            Log.e("BlockViewModel", "Error updating instituicao: ${e.message}", e)
        }
    }



    fun insertBlock(block: BlockEntity) = viewModelScope.launch { 
        blockRepository.insertBlock(block.copy(projectId = _currentProjectId.value)) 
    }
    fun updateBlock(block: BlockEntity) = viewModelScope.launch { 
        blockRepository.updateBlock(block) 
    }

    // Função para atualização imediata na UI e persistência controlada
    fun updateBlockLive(block: BlockEntity) {
        viewModelScope.launch {
            blockRepository.updateBlock(block)
        }
    }

    fun updateBlockContentText(block: BlockEntity, newText: String) {
        try {
            val json = try { Json.parseToJsonElement(block.contentJson).jsonObject } catch (e: Exception) { JsonObject(emptyMap()) }
            val mutableMap = json.toMutableMap()
            mutableMap["text"] = JsonPrimitive(newText)
            mutableMap.remove("elements") // quando o usuário edita diretamente, converte para texto puro
            val updatedContent = JsonObject(mutableMap).toString()
            updateBlockLive(block.copy(contentJson = updatedContent))
        } catch (e: Exception) {
            updateBlockLive(block.copy(contentJson = buildJsonObject { put("text", newText) }.toString()))
        }
    }

    fun updateSelectedTitle(title: String) {
        val block = selectedBlock.value ?: return
        updateBlockLive(block.copy(title = title))
    }

    fun updateSelectedFormatting(
        fontSize: Int? = null,
        isBold: Boolean? = null,
        isItalic: Boolean? = null,
        align: String? = null,
        textColor: String? = null
    ) {
        val block = selectedBlock.value ?: return
        try {
            val json = try { Json.parseToJsonElement(block.contentJson).jsonObject } catch (e: Exception) { JsonObject(emptyMap()) }
            val mutableMap = json.toMutableMap()
            fontSize?.let { mutableMap["fontSize"] = JsonPrimitive(it) }
            isBold?.let { mutableMap["isBold"] = JsonPrimitive(it) }
            isItalic?.let { mutableMap["isItalic"] = JsonPrimitive(it) }
            align?.let { mutableMap["align"] = JsonPrimitive(it) }
            textColor?.let { 
                if (it.isEmpty()) mutableMap.remove("textColor") else mutableMap["textColor"] = JsonPrimitive(it) 
            }
            val updatedContent = JsonObject(mutableMap).toString()
            updateBlock(block.copy(contentJson = updatedContent))
        } catch (e: Exception) {}
    }

    fun updateSelectedChartAttribute(attribute: String, value: Float) {
        val block = selectedBlock.value ?: return
        try {
            val json = try { Json.parseToJsonElement(block.contentJson).jsonObject } catch (e: Exception) { JsonObject(emptyMap()) }
            val mutableMap = json.toMutableMap()
            mutableMap[attribute] = JsonPrimitive(value)
            val updatedContent = JsonObject(mutableMap).toString()
            updateBlockLive(block.copy(contentJson = updatedContent))
        } catch (e: Exception) {}
    }

    fun updateSelectedImageUrl(url: String) {
        val block = selectedBlock.value ?: return
        try {
            val json = try { Json.parseToJsonElement(block.contentJson).jsonObject } catch (e: Exception) { JsonObject(emptyMap()) }
            val mutableMap = json.toMutableMap()
            mutableMap["url"] = JsonPrimitive(url)
            val updatedContent = JsonObject(mutableMap).toString()
            updateBlock(block.copy(contentJson = updatedContent))
        } catch (e: Exception) {}
    }

    fun duplicateBlock(block: BlockEntity) = viewModelScope.launch {
        val newBlock = block.copy(
            id = 0,
            title = "${block.title} (Cópia)",
            posX = block.posX + 30f,
            posY = block.posY + 30f
        )
        blockRepository.insertBlock(newBlock.copy(projectId = _currentProjectId.value))
    }

    fun deleteBlock(block: BlockEntity) = viewModelScope.launch { 
        if (_selectedBlockId.value == block.id) {
            _selectedBlockId.value = null
        }
        blockRepository.deleteBlock(block) 
        try {
            val content = Json.parseToJsonElement(block.contentJson).jsonObject
            val imgId = content["imgId"]?.jsonPrimitive?.content
            if (imgId != null) {
                blockRepository.deleteImageCache(imgId)
            }
        } catch (e: Exception) {}
    }

    fun clearCanvas() = viewModelScope.launch {
        _selectedBlockId.value = null
        blockRepository.clearCanvas(_currentProjectId.value)
    }
    
    fun getCachedImage(imgId: String, onResult: (String?) -> Unit) = viewModelScope.launch {
        onResult(blockRepository.getCachedImage(imgId))
    }

    fun cacheImage(imgId: String, url: String) = viewModelScope.launch(Dispatchers.IO) {
        if (url.startsWith("data:") || url.startsWith("blob:")) return@launch
        
        try {
            val existing = blockRepository.getCachedImage(imgId)
            if (existing != null) return@launch

            val connection = URL(url).openConnection()
            connection.connect()
            val inputStream = connection.getInputStream()
            val bytes = inputStream.readBytes()
            val base64 = "data:image/jpeg;base64," + android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
            
            blockRepository.saveImageCache(imgId, base64)
            Log.d("BlockViewModel", "Image cached: $imgId")
        } catch (e: Exception) {
            Log.e("BlockViewModel", "Failed to cache image: $url", e)
        }
    }

    fun autoOrganizeBlocks() = viewModelScope.launch {
        // 1. Buscamos os blocos do projeto atual
        val allBlocks = blockRepository.getBlocksStream(_currentProjectId.value).first()
        if (allBlocks.isEmpty()) return@launch

        // 2. Ordenamos alfabeticamente por título (Fiel ao OrganizerModule.js)
        val sortedBlocks = allBlocks.sortedWith(compareBy<BlockEntity>({ it.title.lowercase() }, { it.id }))

        val padding = 20f // gapX/gapY: 20px no Web
        val startX = 40f  // startX: 40px no Web
        val startY = 40f  // startY: 40px no Web
        val maxCanvasWidth = canvasDimensions.value.first.toFloat() - 40f
        
        var currentX = startX
        var currentY = startY
        var currentRowMaxHeight = 0f
        
        val updatedBlocks = mutableListOf<BlockEntity>()
        
        sortedBlocks.forEach { block ->
            // 3. Validação de Espaço
            if (currentX + block.width > maxCanvasWidth && currentX > startX) {
                currentX = startX
                currentY += currentRowMaxHeight + padding
                currentRowMaxHeight = 0f
            }
            
            // 4. Snap to Grid (20px)
            val posX = (currentX / 20f).roundToInt() * 20f
            val posY = (currentY / 20f).roundToInt() * 20f

            updatedBlocks.add(block.copy(
                posX = posX,
                posY = posY
            ))
            
            // 5. Atualização da Caneta
            currentX += block.width.toFloat() + padding
            
            if (block.height.toFloat() > currentRowMaxHeight) {
                currentRowMaxHeight = block.height.toFloat()
            }
        }

        // Persistimos as novas coordenadas no banco de dados.
        blockRepository.insertBlocks(updatedBlocks)
        _events.emit("Canvas auto-organizado com sucesso!")
    }

    fun exportToJson(): String {
        val state = uiState.value
        val blocks = if (state is BlockUiState.Success) state.blocks else emptyList()
        
        val json = Json { prettyPrint = true }
        val root = buildJsonObject {
            put("app_brand_title", brandTitle.value)
            put("version", "1.2.0")
            
            // Web Portability Format: Blocos dentro de um objeto 'blocks' ou 'data'
            putJsonObject("blocks") {
                blocks.forEach { block ->
                    val blockKey = "block_${block.id}"
                    put(blockKey, buildJsonObject {
                        put("id", block.id)
                        put("title", block.title)
                        put("type", block.type)
                        put("top", "${block.posY.toInt()}px")
                        put("left", "${block.posX.toInt()}px")
                        put("width", "${block.width}px")
                        put("height", "${block.height}px")
                        
                        val content = try {
                            Json.parseToJsonElement(block.contentJson).jsonObject
                        } catch (e: Exception) {
                            null
                        }

                        when (block.type.lowercase()) {
                            "text" -> {
                                val text = content?.get("text")?.jsonPrimitive?.content ?: ""
                                // Reversão: Markdown -> HTML para compatibilidade Web
                                val html = text
                                    .replace(Regex("\\*\\*(.*?)\\*\\*"), "<b>$1</b>")
                                    .replace(Regex("\\*(.*?)\\*"), "<i>$1</i>")
                                    .replace("\n", "<br>")
                                
                                putJsonArray("campos") {
                                    addJsonObject {
                                        put("html", html)
                                        put("className", "sub-campo")
                                    }
                                }
                            }
                            "chart" -> {
                                // Web espera 'status' ou 'inputs'
                                put("status", content ?: buildJsonObject {})
                            }
                            "image" -> {
                                val url = content?.get("url")?.jsonPrimitive?.content ?: ""
                                put("url", url)
                                content?.get("imgId")?.jsonPrimitive?.content?.let { put("imgId", it) }
                            }
                        }
                    })
                }
            }
        }
        return json.encodeToString(JsonObject.serializer(), root)
    }

    fun loadDefaultTemplate(context: android.content.Context, clearFirst: Boolean = false) = viewModelScope.launch {
        try {
            val jsonString = context.assets.open("default_template.json").bufferedReader().use { it.readText() }
            importFromJson(jsonString, clearFirst = clearFirst)
        } catch (e: Exception) {
            e.printStackTrace()
            _events.emit("Erro ao carregar ficha padrão: ${e.message}")
        }
    }

    fun importFromJson(jsonString: String, clearFirst: Boolean = false) = viewModelScope.launch {
        if (jsonString.isBlank()) {
            _events.emit("JSON vazio")
            return@launch
        }
        try {
            val json = Json { ignoreUnknownKeys = true }
            val jsonElement = json.parseToJsonElement(jsonString)
            val newBlocks = mutableListOf<BlockEntity>()

            // 1. Importar metadados (Título do Projeto/Brand)
            val rootObj = jsonElement.jsonObject
            val brand = rootObj["metadata"]?.jsonObject?.get("brand")?.jsonPrimitive?.content
                ?: rootObj["app_brand_title"]?.jsonPrimitive?.content
            
            brand?.let { setBrandTitle(it) }

            // 2. Lógica de busca recursiva para encontrar blocos em diferentes estruturas (Web/Nativo)
            fun extractBlocks(element: JsonElement) {
                when (element) {
                    is JsonArray -> {
                        element.forEach { 
                            if (it is JsonObject && it.containsKey("type")) {
                                newBlocks.add(parseBlockObject(it, _currentProjectId.value))
                            } else {
                                extractBlocks(it)
                            }
                        }
                    }
                    is JsonObject -> {
                        if (element.containsKey("type") && (element.containsKey("left") || element.containsKey("top") || element.containsKey("posX"))) {
                            newBlocks.add(parseBlockObject(element, _currentProjectId.value))
                        } else {
                            element.values.forEach { extractBlocks(it) }
                        }
                    }
                    else -> {}
                }
            }

            extractBlocks(jsonElement)

            if (newBlocks.isNotEmpty()) {
                if (clearFirst) {
                    blockRepository.clearCanvas(_currentProjectId.value)
                }
                blockRepository.insertBlocks(newBlocks)
                _events.emit("${newBlocks.size} blocos carregados com sucesso!")
            } else {
                _events.emit("Nenhum bloco compatível encontrado no arquivo.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _events.emit("Erro na importação: Estrutura JSON inválida.")
        }
    }

    private fun parseBlockObject(obj: JsonObject, projectId: Long): BlockEntity {
        val type = obj["type"]?.jsonPrimitive?.content ?: "text"
        val title = obj["title"]?.jsonPrimitive?.content ?: "Bloco"
        
        val contentJson = when (type.lowercase()) {
            "chart" -> {
                val status = obj["status"]?.jsonObject ?: obj["inputs"]?.jsonObject ?: obj
                buildJsonObject {
                    // Mapeamento Web -> Nativo: Captura valores reais (podem ser > 10)
                    put("ninjutsu", status["ninjutsu"]?.jsonPrimitive?.floatOrNull ?: status["nin"]?.jsonPrimitive?.floatOrNull ?: 4f)
                    put("inteligencia", status["inteligencia"]?.jsonPrimitive?.floatOrNull ?: status["int"]?.jsonPrimitive?.floatOrNull ?: 4f)
                    put("chakra", status["chakra"]?.jsonPrimitive?.floatOrNull ?: status["cha"]?.jsonPrimitive?.floatOrNull ?: status["chakraMax"]?.jsonPrimitive?.floatOrNull ?: 4f)
                    put("taijutsu", status["taijutsu"]?.jsonPrimitive?.floatOrNull ?: status["tai"]?.jsonPrimitive?.floatOrNull ?: 4f)
                    put("vigor", status["vigor"]?.jsonPrimitive?.floatOrNull ?: status["vig"]?.jsonPrimitive?.floatOrNull ?: 4f)
                    put("genjutsu", status["genjutsu"]?.jsonPrimitive?.floatOrNull ?: status["gen"]?.jsonPrimitive?.floatOrNull ?: 4f)
                }.toString()
            }
            "text" -> {
                val campos = obj["campos"]?.jsonArray
                val elements = mutableListOf<JsonElement>()
                
                if (campos != null && campos.isNotEmpty()) {
                    campos.forEach { campo ->
                        val html = campo.jsonObject["html"]?.jsonPrimitive?.content ?: ""
                        elements.addAll(parseHtmlToElements(html))
                    }
                } else {
                    val html = obj["text"]?.jsonPrimitive?.content ?: obj["html"]?.jsonPrimitive?.content ?: ""
                    elements.addAll(parseHtmlToElements(html))
                }
                
                val flatText = elements.joinToString("\n") { 
                    val el = it.jsonObject
                    if (el["type"]?.jsonPrimitive?.content == "text") el["value"]?.jsonPrimitive?.content ?: "" else ""
                }

                buildJsonObject {
                    put("elements", JsonArray(elements))
                    put("text", flatText)
                    put("titleSize", 13)
                    put("align", "left")
                }.toString()
            }
            "image" -> {
                val url = obj["url"]?.jsonPrimitive?.content ?: obj["src"]?.jsonPrimitive?.content ?: ""
                val imgId = obj["imgId"]?.jsonPrimitive?.content ?: obj["id"]?.jsonPrimitive?.content ?: ""
                buildJsonObject { 
                    put("url", url) 
                    if (imgId.isNotEmpty()) put("imgId", imgId)
                }.toString()
            }
            else -> obj.toString()
        }

        // Suporte a coordenadas com 'px' (Web) ou números puros
        val posX = obj["left"]?.jsonPrimitive?.content?.replace("px", "")?.toFloatOrNull() 
            ?: obj["posX"]?.jsonPrimitive?.floatOrNull ?: 100f
        val posY = obj["top"]?.jsonPrimitive?.content?.replace("px", "")?.toFloatOrNull() 
            ?: obj["posY"]?.jsonPrimitive?.floatOrNull ?: 100f

        return BlockEntity(
            projectId = projectId,
            title = title,
            type = type,
            posX = posX,
            posY = posY,
            width = obj["width"]?.jsonPrimitive?.content?.replace("px", "")?.toIntOrNull() 
                ?: obj["width"]?.jsonPrimitive?.intOrNull ?: 220,
            height = obj["height"]?.jsonPrimitive?.content?.replace("px", "")?.toIntOrNull() 
                ?: obj["height"]?.jsonPrimitive?.intOrNull ?: 180,
            contentJson = contentJson
        )
    }

    private fun parseHtmlToElements(html: String): List<JsonElement> {
        val elements = mutableListOf<JsonElement>()
        val tableRegex = Regex("<table[^>]*>(.*?)</table>", RegexOption.DOT_MATCHES_ALL)
        var lastIndex = 0
        
        tableRegex.findAll(html).forEach { match ->
            val textBefore = html.substring(lastIndex, match.range.first)
            if (textBefore.replace(Regex("<[^>]*>"), "").replace("&nbsp;", " ").isNotBlank()) {
                elements.add(buildJsonObject {
                    put("type", "text")
                    put("value", cleanHtmlContent(textBefore))
                })
            }
            
            val tableHtml = match.groupValues[1]
            val headers = mutableListOf<String>()
            val rows = mutableListOf<JsonArray>()
            
            Regex("<th[^>]*>(.*?)</th>", RegexOption.DOT_MATCHES_ALL).findAll(tableHtml).forEach {
                headers.add(cleanHtmlContent(it.groupValues[1]))
            }
            
            Regex("<tr[^>]*>(.*?)</tr>", RegexOption.DOT_MATCHES_ALL).findAll(tableHtml).forEach {
                val rowHtml = it.groupValues[1]
                val cells = mutableListOf<JsonPrimitive>()
                val tdMatches = Regex("<td[^>]*>(.*?)</td>", RegexOption.DOT_MATCHES_ALL).findAll(rowHtml).toList()
                if (tdMatches.isNotEmpty()) {
                    tdMatches.forEach { td ->
                        cells.add(JsonPrimitive(cleanHtmlContent(td.groupValues[1])))
                    }
                    rows.add(JsonArray(cells))
                }
            }
            
            elements.add(buildJsonObject {
                put("type", "table")
                putJsonArray("headers") { headers.forEach { add(it) } }
                putJsonArray("rows") { rows.forEach { add(it) } }
            })
            
            lastIndex = match.range.last + 1
        }
        
        val textAfter = html.substring(lastIndex)
        if (textAfter.replace(Regex("<[^>]*>"), "").replace("&nbsp;", " ").isNotBlank()) {
            elements.add(buildJsonObject {
                put("type", "text")
                put("value", cleanHtmlContent(textAfter))
            })
        }
        
        return elements
    }

    private fun cleanHtmlContent(html: String): String {
        return html
            .replace(Regex("<span[^>]*font-size:\\s*(\\d+)px[^>]*>(.*?)</span>")) { 
                "[size=${it.groupValues[1]}]${it.groupValues[2]}[/size]" 
            }
            .replace(Regex("<b[^>]*>"), "**").replace("</b>", "**")
            .replace(Regex("<strong[^>]*>"), "**").replace("</strong>", "**")
            .replace(Regex("<i[^>]*>"), "*").replace("</i>", "*")
            .replace(Regex("<em[^>]*>"), "*").replace("</em>", "*")
            .replace(Regex("<u[^>]*>"), "<u>").replace("</u>", "</u>")
            .replace("<br>", "\n")
            .replace(Regex("<pre[^>]*>"), "\n> ").replace("</pre>", "\n")
            .replace(Regex("<div[^>]*>"), "").replace("</div>", "\n")
            .replace(Regex("<p[^>]*>"), "").replace("</p>", "\n")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace(Regex("<[^>]*>"), "")
            .trim()
    }
}
