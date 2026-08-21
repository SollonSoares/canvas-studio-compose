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

    val themeStyle = userPreferencesManager.themeStyleFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "cupertino")

    val galleryBaseUrl = userPreferencesManager.galleryBaseUrlFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "https://sollonsoares.github.io/galeria/imagens/")

    val githubToken = userPreferencesManager.githubTokenFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val canvasDimensions = userPreferencesManager.canvasDimensionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(2000, 2000))

    fun toggleDarkMode() = viewModelScope.launch {
        userPreferencesManager.setDarkMode(!isDarkMode.value)
    }

    fun setThemeStyle(style: String) = viewModelScope.launch {
        userPreferencesManager.setThemeStyle(style)
    }

    fun setGalleryBaseUrl(url: String) = viewModelScope.launch {
        userPreferencesManager.setGalleryBaseUrl(url)
    }

    fun setGithubToken(token: String) = viewModelScope.launch {
        userPreferencesManager.setGithubToken(token)
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

    private val _canvasConfig = MutableStateFlow(com.canvasstudio.domain.model.CanvasConfig())
    val canvasConfig: StateFlow<com.canvasstudio.domain.model.CanvasConfig> = _canvasConfig.asStateFlow()

    fun updateCanvasConfig(transform: (com.canvasstudio.domain.model.CanvasConfig) -> com.canvasstudio.domain.model.CanvasConfig) {
        _canvasConfig.value = transform(_canvasConfig.value)
    }

    fun toggleAutoOcr(enabled: Boolean) {
        updateCanvasConfig { it.copy(autoOcrEnabled = enabled) }
    }

    fun toggleFinancialBadges(enabled: Boolean) {
        updateCanvasConfig { it.copy(showFinancialBadges = enabled) }
    }

    fun togglePartyDetails(enabled: Boolean) {
        updateCanvasConfig { it.copy(showPartyDetails = enabled) }
    }

    fun toggleFitAspectRatio(enabled: Boolean) {
        updateCanvasConfig { it.copy(fitOriginalAspectRatio = enabled) }
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

    fun importSharedUri(context: android.content.Context, uri: Uri) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val importer = com.canvasstudio.domain.service.SharedMediaImporter(context)
            val result = importer.importMedia(uri, autoOcrEnabled = _canvasConfig.value.autoOcrEnabled)
            val count = (uiState.value as? BlockUiState.Success)?.blocks?.size ?: 0
            val spawnX = 60f + (count % 5) * 35f
            val spawnY = 60f + (count % 5) * 35f

            val block = if (result.savedFileUri != null) {
                val content = buildJsonObject {
                    put("url", result.savedFileUri)
                    if (result.analysis.value != null) put("valor", result.analysis.value)
                    if (result.analysis.valueFormatted != null) put("valorFormatted", result.analysis.valueFormatted)
                    put("realizadoEm", result.analysis.realizadoEm)
                    if (result.analysis.isPix) put("isPix", true)
                    if (result.analysis.pagador != null) put("pagador", result.analysis.pagador)
                    if (result.analysis.destinatario != null) put("destinatario", result.analysis.destinatario)
                    if (result.analysis.instituicao != null) put("instituicao", result.analysis.instituicao)
                    if (result.analysis.rawText.isNotBlank()) put("rawText", result.analysis.rawText)
                }.toString()
                BlockEntity(
                    projectId = _currentProjectId.value,
                    title = result.analysis.title,
                    type = "image",
                    posX = spawnX,
                    posY = spawnY,
                    width = result.blockWidth,
                    height = result.blockHeight,
                    contentJson = content
                )
            } else {
                val content = buildJsonObject {
                    if (result.analysis.value != null) put("valor", result.analysis.value)
                    if (result.analysis.valueFormatted != null) put("valorFormatted", result.analysis.valueFormatted)
                    put("realizadoEm", result.analysis.realizadoEm)
                    if (result.analysis.isPix) put("isPix", true)
                    if (result.analysis.pagador != null) put("pagador", result.analysis.pagador)
                    if (result.analysis.destinatario != null) put("destinatario", result.analysis.destinatario)
                    if (result.analysis.instituicao != null) put("instituicao", result.analysis.instituicao)
                    val deStr = if (result.analysis.pagador != null) "\nDe (Pagador): ${result.analysis.pagador}" else ""
                    val paraStr = if (result.analysis.destinatario != null) "\nPara (Destinatário): ${result.analysis.destinatario}" else ""
                    val instStr = if (result.analysis.instituicao != null) "\nInstituição: ${result.analysis.instituicao}" else ""
                    put("text", "📄 **${result.analysis.title}**\n\nValor: ${result.analysis.valueFormatted ?: "N/D"}\nRealizado em: ${result.analysis.realizadoEm}$deStr$paraStr$instStr\n\n${result.analysis.rawText}")
                    put("fontSize", 13)
                    put("align", "left")
                }.toString()
                BlockEntity(
                    projectId = _currentProjectId.value,
                    title = result.analysis.title,
                    type = "text",
                    posX = spawnX,
                    posY = spawnY,
                    width = 260,
                    height = 180,
                    contentJson = content
                )
            }

            blockRepository.insertBlock(block)
            Log.d("CanvasStudio", "Inserted block ${block.title} into DB with valor=${result.analysis.value}, de=${result.analysis.pagador}, para=${result.analysis.destinatario}")
            val msg = if (result.analysis.valueFormatted != null) "Comprovante '${result.analysis.title}' (${result.analysis.valueFormatted}) adicionado!" else "Item '${result.analysis.title}' adicionado ao Canvas!"
            _events.emit(msg)
        } catch (e: Exception) {
            Log.e("CanvasStudio", "Error in importSharedUri: ${e.message}", e)
            _events.emit("Erro ao importar item compartilhado: ${e.message}")
        }
    }

    fun updateBlockImageFromUri(block: BlockEntity, uri: Uri, context: android.content.Context) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val imagesDir = java.io.File(context.filesDir, "canvas_images").apply { if (!exists()) mkdirs() }
            val destFile = java.io.File(imagesDir, "img_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                java.io.FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            if (destFile.exists() && destFile.length() > 0) {
                val currentObj = try {
                    Json.parseToJsonElement(block.contentJson).jsonObject.toMutableMap()
                } catch (e: Exception) {
                    mutableMapOf()
                }
                currentObj["url"] = JsonPrimitive("file://${destFile.absolutePath}")
                val updatedBlock = block.copy(contentJson = JsonObject(currentObj).toString())
                blockRepository.updateBlock(updatedBlock)
                _events.emit("Imagem atualizada com sucesso!")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _events.emit("Erro ao atualizar imagem: ${e.message}")
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
        return com.canvasstudio.features.export_portability.JsonPortabilityService.exportToJson(brandTitle.value, blocks)
    }

    fun syncToGallery(context: android.content.Context, onShareZip: (java.io.File) -> Unit = {}) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val state = uiState.value
            val currentBlocks = if (state is BlockUiState.Success) state.blocks else emptyList()
            if (currentBlocks.none { it.type.equals("image", ignoreCase = true) }) {
                _events.emit("Nenhuma imagem encontrada no Canvas para sincronizar com a Galeria.")
                return@launch
            }

            val syncService = com.canvasstudio.features.export_portability.GallerySyncService(context)
            val result = syncService.syncBlocksToGallery(
                blocks = currentBlocks,
                galleryBaseUrl = galleryBaseUrl.value,
                githubToken = githubToken.value
            )

            if (result.syncedCount > 0) {
                // Atualizar os blocos convertidos no banco de dados Room
                result.updatedBlocks.forEach { block ->
                    blockRepository.updateBlock(block)
                }
                
                if (result.githubUploadedCount > 0) {
                    val msg = "🚀 ${result.githubUploadedCount} imagem(ns) enviadas e comitadas no GitHub com sucesso!"
                    _events.emit(msg)
                } else if (result.errorMessages.isNotEmpty()) {
                    val errorSummary = result.errorMessages.firstOrNull() ?: ""
                    _events.emit("URLs atualizadas, mas aviso do GitHub: $errorSummary")
                    if (result.zipFile != null) {
                        onShareZip(result.zipFile)
                    }
                } else {
                    val msg = "${result.syncedCount} imagem(ns) preparadas para a Galeria! URLs públicas geradas."
                    _events.emit(msg)
                    if (result.zipFile != null) {
                        onShareZip(result.zipFile)
                    }
                }
            } else {
                _events.emit("Todas as imagens do Canvas já possuem links públicos da Galeria!")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _events.emit("Erro ao sincronizar galeria: ${e.message}")
        }
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
            val (brand, newBlocks) = com.canvasstudio.features.export_portability.JsonPortabilityService.parseBlocksFromJson(jsonString, _currentProjectId.value)
            brand?.let { setBrandTitle(it) }

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
}
