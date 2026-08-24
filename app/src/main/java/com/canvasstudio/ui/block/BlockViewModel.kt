package com.canvasstudio.ui.block

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.canvasstudio.data.local.entity.BlockEntity
import com.canvasstudio.data.local.preferences.UserPreferencesManager
import com.canvasstudio.data.repository.BlockRepository
import com.canvasstudio.domain.model.CanvasConfig
import com.canvasstudio.features.export_portability.JsonPortabilityService
import com.canvasstudio.ui.block.delegates.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*

class BlockViewModel(
    private val blockRepository: BlockRepository,
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    private val searchEngine = CanvasSearchEngine()
    val searchQuery: StateFlow<String> = searchEngine.searchQuery
    val appliedQuery: StateFlow<String> = searchEngine.appliedQuery

    val modulesState = userPreferencesManager.modulesStateFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
    val brandTitle = userPreferencesManager.brandTitleFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Canvas Studio")
    val isDarkMode = userPreferencesManager.darkModeFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val isGridEnabled = userPreferencesManager.gridEnabledFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val isLocked = userPreferencesManager.isLockedFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val themeStyle = userPreferencesManager.themeStyleFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "cupertino")
    val galleryBaseUrl = userPreferencesManager.galleryBaseUrlFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "https://sollonsoares.github.io/galeria/imagens/")
    val authorName = userPreferencesManager.authorNameFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val canvasDimensions = userPreferencesManager.canvasDimensionsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(10000, 10000))

    private val _canvasConfig = MutableStateFlow(CanvasConfig())
    val canvasConfig: StateFlow<CanvasConfig> = _canvasConfig.asStateFlow()

    private val _currentProjectId = MutableStateFlow(0L)
    val currentProjectId: StateFlow<Long> = _currentProjectId.asStateFlow()

    private val _selectedBlockId = MutableStateFlow<Long?>(null)
    val selectedBlockId: StateFlow<Long?> = _selectedBlockId.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val selectedBlock: StateFlow<BlockEntity?> = combine(_currentProjectId.flatMapLatest { blockRepository.getBlocksStream(it) }, _selectedBlockId) { blocks, selId ->
        if (selId == null) null else blocks.find { it.id == selId }
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.Eagerly, null)

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val uiState = combine(_currentProjectId.flatMapLatest { blockRepository.getBlocksStream(it) }, appliedQuery, userPreferencesManager.modulesStateFlow) { blocks, query, modules ->
        BlockUiState.Success(searchEngine.filterBlocks(blocks, query, modules))
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BlockUiState.Loading)

    // Preference Actions
    fun toggleDarkMode() = viewModelScope.launch { userPreferencesManager.setDarkMode(!isDarkMode.value) }
    fun setThemeStyle(style: String) = viewModelScope.launch { userPreferencesManager.setThemeStyle(style) }
    fun setGalleryBaseUrl(url: String) = viewModelScope.launch { userPreferencesManager.setGalleryBaseUrl(url) }
    fun setGithubToken(token: String) = viewModelScope.launch { userPreferencesManager.setGithubToken(token) }
    fun setAuthorName(name: String) = viewModelScope.launch { userPreferencesManager.setAuthorName(name) }
    fun toggleGrid() = viewModelScope.launch { userPreferencesManager.setGridEnabled(!isGridEnabled.value) }
    fun toggleLock() = viewModelScope.launch { userPreferencesManager.setLocked(!isLocked.value) }
    fun setBrandTitle(title: String) = viewModelScope.launch { userPreferencesManager.setBrandTitle(title) }
    fun setCanvasDimensions(w: Int, h: Int) = viewModelScope.launch { userPreferencesManager.setCanvasDimensions(w, h) }
    fun toggleModule(type: String, enabled: Boolean) = viewModelScope.launch { userPreferencesManager.setModuleEnabled(type, enabled) }

    fun updateCanvasConfig(transform: (CanvasConfig) -> CanvasConfig) { _canvasConfig.value = transform(_canvasConfig.value) }
    fun toggleAutoOcr(enabled: Boolean) = updateCanvasConfig { it.copy(autoOcrEnabled = enabled) }
    fun toggleFinancialBadges(enabled: Boolean) = updateCanvasConfig { it.copy(showFinancialBadges = enabled) }
    fun togglePartyDetails(enabled: Boolean) = updateCanvasConfig { it.copy(showPartyDetails = enabled) }
    fun toggleFitAspectRatio(enabled: Boolean) = updateCanvasConfig { it.copy(fitOriginalAspectRatio = enabled) }

    // Search Controls
    fun setSearchQuery(query: String) = searchEngine.setSearchQuery(query)
    fun applySearch() = searchEngine.applySearch()

    // Selection & Block CRUD
    fun selectBlock(block: BlockEntity?) { _selectedBlockId.value = block?.id }
    fun selectBlockById(id: Long?) { _selectedBlockId.value = id }
    fun insertBlock(block: BlockEntity) = viewModelScope.launch { blockRepository.insertBlock(block.copy(projectId = _currentProjectId.value)) }
    fun updateBlock(block: BlockEntity) = viewModelScope.launch { blockRepository.updateBlock(block) }
    fun updateBlockLive(block: BlockEntity) = viewModelScope.launch { blockRepository.updateBlock(block) }

    fun duplicateBlock(block: BlockEntity) = viewModelScope.launch {
        blockRepository.insertBlock(block.copy(id = 0, title = "${block.title} (Cópia)", posX = block.posX + 30f, posY = block.posY + 30f, projectId = _currentProjectId.value))
    }

    fun deleteBlock(block: BlockEntity) = viewModelScope.launch {
        if (_selectedBlockId.value == block.id) _selectedBlockId.value = null
        blockRepository.deleteBlock(block)
        try {
            val content = Json.parseToJsonElement(block.contentJson).jsonObject
            content["imgId"]?.jsonPrimitive?.content?.let { blockRepository.deleteImageCache(it) }
        } catch (e: Exception) {}
    }

    fun clearCanvas() = viewModelScope.launch {
        _selectedBlockId.value = null
        blockRepository.clearCanvas(_currentProjectId.value)
    }

    // Block Property Updates
    fun updateBlockContentText(block: BlockEntity, newText: String) = updateBlockLive(BlockPropertyUpdater.updateContentText(block, newText))
    fun updateSelectedTitle(title: String) { selectedBlock.value?.let { updateBlockLive(it.copy(title = title)) } }
    fun updateSelectedValue(newValue: Float?) { selectedBlock.value?.let { updateBlock(BlockPropertyUpdater.updateValue(it, newValue)) } }
    fun updateSelectedRealizadoEm(newDate: String) { selectedBlock.value?.let { updateBlock(BlockPropertyUpdater.updateRealizadoEm(it, newDate)) } }
    fun updateSelectedPagador(pagador: String) { selectedBlock.value?.let { updateBlock(BlockPropertyUpdater.updatePartyField(it, "pagador", pagador)) } }
    fun updateSelectedDestinatario(dest: String) { selectedBlock.value?.let { updateBlock(BlockPropertyUpdater.updatePartyField(it, "destinatario", dest)) } }
    fun updateSelectedInstituicao(inst: String) { selectedBlock.value?.let { updateBlock(BlockPropertyUpdater.updatePartyField(it, "instituicao", inst)) } }
    fun updateSelectedFormatting(sz: Int? = null, b: Boolean? = null, isItalic: Boolean? = null, al: String? = null, col: String? = null) {
        selectedBlock.value?.let { block -> updateBlock(BlockPropertyUpdater.updateFormatting(block, sz, b, isItalic, al, col)) }
    }
    fun updateSelectedChartAttribute(attr: String, value: Float) { selectedBlock.value?.let { updateBlockLive(BlockPropertyUpdater.updateChartAttribute(it, attr, value)) } }
    fun updateSelectedImageUrl(url: String) { selectedBlock.value?.let { updateBlock(BlockPropertyUpdater.updateImageUrl(it, url)) } }

    fun insertTable(block: BlockEntity) {
        val currentContent = try {
            val json = Json.parseToJsonElement(block.contentJson).jsonObject
            val campos = json["campos"]?.jsonArray
            if (!campos.isNullOrEmpty()) {
                campos.joinToString("\n") { (it as? JsonObject)?.get("html")?.jsonPrimitive?.contentOrNull ?: it.jsonPrimitive.contentOrNull ?: "" }
            } else {
                json["text"]?.jsonPrimitive?.contentOrNull ?: json["html"]?.jsonPrimitive?.contentOrNull ?: ""
            }
        } catch (e: Exception) { "" }

        val tableHtml = """<table style="color: rgb(240, 242, 248); width: 100%; border-collapse: collapse; font-family: sans-serif; font-size: 11px; margin: 6px 0;"><thead><tr style="background: rgba(45, 45, 45, 0.9); color: rgb(79, 195, 247);"><th style="border: 1px solid rgba(255, 255, 255, 0.15); padding: 4px 6px; text-align: left;">Coluna 1</th><th style="border: 1px solid rgba(255, 255, 255, 0.15); padding: 4px 6px; text-align: left;">Coluna 2</th></tr></thead><tbody><tr><td style="border: 1px solid rgba(255, 255, 255, 0.15); padding: 4px 6px; background-color: rgba(30, 30, 30, 0.6);">Item 1</td><td style="border: 1px solid rgba(255, 255, 255, 0.15); padding: 4px 6px; background-color: rgba(30, 30, 30, 0.6);">Valor 1</td></tr><tr><td style="border: 1px solid rgba(255, 255, 255, 0.15); padding: 4px 6px; background-color: rgba(30, 30, 30, 0.6);">Item 2</td><td style="border: 1px solid rgba(255, 255, 255, 0.15); padding: 4px 6px; background-color: rgba(30, 30, 30, 0.6);">Valor 2</td></tr></tbody></table><div><br></div>"""
        val newContent = if (currentContent.isNotBlank()) "$currentContent\n$tableHtml" else tableHtml
        updateBlockContentText(block, newContent)
    }

    fun insertCallout(block: BlockEntity) {
        val currentContent = try {
            val json = Json.parseToJsonElement(block.contentJson).jsonObject
            json["text"]?.jsonPrimitive?.contentOrNull ?: json["html"]?.jsonPrimitive?.contentOrNull ?: ""
        } catch (e: Exception) { "" }
        val calloutHtml = """<div style="background: rgba(2,132,199,0.12); border: 1px solid rgba(2,132,199,0.35); border-radius: 6px; padding: 6px 10px; margin: 6px 0; color: #f0f2f8; font-size: 12px;">Texto destacado...</div><div><br></div>"""
        val newContent = if (currentContent.isNotBlank()) "$currentContent\n$calloutHtml" else calloutHtml
        updateBlockContentText(block, newContent)
    }

    fun insertCollapsible(block: BlockEntity) {
        val currentContent = try {
            val json = Json.parseToJsonElement(block.contentJson).jsonObject
            json["text"]?.jsonPrimitive?.contentOrNull ?: json["html"]?.jsonPrimitive?.contentOrNull ?: ""
        } catch (e: Exception) { "" }
        val collapsibleHtml = """<details style="background: rgba(2,132,199,0.08); border: 1px solid rgba(2,132,199,0.3); border-radius: 6px; padding: 6px 10px; margin: 6px 0;"><summary style="cursor: pointer; font-weight: bold; color: #0284c7;">Título Expansível</summary><div style="margin-top: 4px; font-size: 12px;">Linha 1 do texto detalhado.<br>Linha 2 do texto detalhado.<br>Linha 3 do texto detalhado.<br>Linha 4 do texto expandido...</div></details><div><br></div>"""
        val newContent = if (currentContent.isNotBlank()) "$currentContent\n$collapsibleHtml" else collapsibleHtml
        updateBlockContentText(block, newContent)
    }

    fun insertList(block: BlockEntity) {
        val currentContent = try {
            val json = Json.parseToJsonElement(block.contentJson).jsonObject
            json["text"]?.jsonPrimitive?.contentOrNull ?: json["html"]?.jsonPrimitive?.contentOrNull ?: ""
        } catch (e: Exception) { "" }
        val listHtml = """<ul style="margin: 6px 0; padding-left: 18px;"><li>Item 1</li><li>Item 2</li></ul><div><br></div>"""
        val newContent = if (currentContent.isNotBlank()) "$currentContent\n$listHtml" else listHtml
        updateBlockContentText(block, newContent)
    }

    fun insertTableIntoSelected() { selectedBlock.value?.let { insertTable(it) } }
    fun insertCalloutIntoSelected() { selectedBlock.value?.let { insertCallout(it) } }
    fun insertCollapsibleIntoSelected() { selectedBlock.value?.let { insertCollapsible(it) } }
    fun insertListIntoSelected() { selectedBlock.value?.let { insertList(it) } }

    // Media & Import Controls
    fun importSharedUri(context: Context, uri: Uri) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val count = (uiState.value as? BlockUiState.Success)?.blocks?.size ?: 0
            val spawn = 60f + (count % 5) * 35f
            val (block, msg) = CanvasMediaCoordinator.importMedia(context, uri, _canvasConfig.value.autoOcrEnabled, spawn, spawn, _currentProjectId.value)
            blockRepository.insertBlock(block)
            _events.emit(msg)
        } catch (e: Exception) { _events.emit("Erro ao importar item: ${e.message}") }
    }

    fun updateBlockImageFromUri(block: BlockEntity, uri: Uri, context: Context) = viewModelScope.launch(Dispatchers.IO) {
        try {
            CanvasMediaCoordinator.updateImageFromUri(context, block, uri)?.let {
                blockRepository.updateBlock(it)
                _events.emit("Imagem atualizada com sucesso!")
            }
        } catch (e: Exception) { _events.emit("Erro ao atualizar imagem: ${e.message}") }
    }

    // Export Controls
    fun exportToJson(): String {
        val blocks = (uiState.value as? BlockUiState.Success)?.blocks ?: emptyList()
        return JsonPortabilityService.exportToJson(brandTitle.value, blocks)
    }

    fun importFromJson(jsonString: String) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val (brand, blocks) = JsonPortabilityService.parseBlocksFromJson(jsonString, _currentProjectId.value)
            brand?.let { userPreferencesManager.setBrandTitle(it) }
            blockRepository.insertBlocks(blocks)
            _events.emit("Importados ${blocks.size} blocos com sucesso!")
        } catch (e: Exception) {
            _events.emit("Erro ao importar JSON: ${e.message}")
        }
    }

    fun exportToPdf(context: Context, destinationUri: Uri) = viewModelScope.launch(Dispatchers.IO) {
        val blocks = (uiState.value as? BlockUiState.Success)?.blocks ?: emptyList()
        if (blocks.isEmpty()) {
            _events.emit("O canvas está vazio. Nada para exportar.")
            return@launch
        }
        val isDark = userPreferencesManager.darkModeFlow.first()
        val author = userPreferencesManager.authorNameFlow.first()
        PdfExporter.exportBlocksToPdf(context, blocks, destinationUri, brandTitle.value, author, isDark) { success, msg ->
            viewModelScope.launch { _events.emit(msg) }
        }
    }

    fun sharePdf(context: Context, filename: String) = viewModelScope.launch(Dispatchers.IO) {
        val blocks = (uiState.value as? BlockUiState.Success)?.blocks ?: emptyList()
        if (blocks.isEmpty()) {
            _events.emit("O canvas está vazio. Nada para compartilhar.")
            return@launch
        }
        val isDark = userPreferencesManager.darkModeFlow.first()
        val author = userPreferencesManager.authorNameFlow.first()
        PdfExporter.exportAndSharePdf(context, blocks, filename, brandTitle.value, author, isDark) { success, msg ->
            viewModelScope.launch { _events.emit(msg) }
        }
    }

    fun syncToGallery(context: Context) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val blocks = (uiState.value as? BlockUiState.Success)?.blocks ?: emptyList()
            val token = userPreferencesManager.githubTokenFlow.first()
            val author = userPreferencesManager.authorNameFlow.first()
            val baseUrl = userPreferencesManager.galleryBaseUrlFlow.first()
            val res = CanvasMediaCoordinator.syncImagesToGallery(context, blocks, token, author, baseUrl) { updated ->
                blockRepository.updateBlocks(updated)
            }
            _events.emit(res)
        } catch (e: Exception) {
            _events.emit("Erro ao sincronizar galeria: ${e.message}")
        }
    }

    fun autoOrganizeBlocks() = viewModelScope.launch(Dispatchers.Default) {
        val blocks = (uiState.value as? BlockUiState.Success)?.blocks ?: return@launch
        val organized = CanvasAutoOrganizer.organize(blocks, _canvasConfig.value.canvasWidth)
        blockRepository.updateBlocks(organized)
    }
}
