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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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
    val githubToken = userPreferencesManager.githubTokenFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val canvasDimensions = userPreferencesManager.canvasDimensionsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(2000, 2000))

    private val _canvasConfig = MutableStateFlow(CanvasConfig())
    val canvasConfig: StateFlow<CanvasConfig> = _canvasConfig.asStateFlow()

    private val _currentProjectId = MutableStateFlow(0L)
    val currentProjectId: StateFlow<Long> = _currentProjectId.asStateFlow()

    private val _selectedBlockId = MutableStateFlow<Long?>(null)
    val selectedBlockId: StateFlow<Long?> = _selectedBlockId.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val selectedBlock: StateFlow<BlockEntity?> = combine(_currentProjectId.flatMapLatest { blockRepository.getBlocksStream(it) }, _selectedBlockId) { blocks, selId ->
        if (selId == null) null else blocks.find { it.id == selId }
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val uiState = combine(_currentProjectId.flatMapLatest { blockRepository.getBlocksStream(it) }, appliedQuery, userPreferencesManager.modulesStateFlow) { blocks, query, modules ->
        BlockUiState.Success(searchEngine.filterBlocks(blocks, query, modules))
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BlockUiState.Loading)

    // Preference Actions
    fun toggleDarkMode() = viewModelScope.launch { userPreferencesManager.setDarkMode(!isDarkMode.value) }
    fun setThemeStyle(style: String) = viewModelScope.launch { userPreferencesManager.setThemeStyle(style) }
    fun setGalleryBaseUrl(url: String) = viewModelScope.launch { userPreferencesManager.setGalleryBaseUrl(url) }
    fun setGithubToken(token: String) = viewModelScope.launch { userPreferencesManager.setGithubToken(token) }
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

    fun importTextShared(text: String, subject: String? = null) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val count = (uiState.value as? BlockUiState.Success)?.blocks?.size ?: 0
            val spawn = 60f + (count % 5) * 35f
            val (block, msg) = CanvasMediaCoordinator.importText(text, subject, spawn, spawn, _currentProjectId.value)
            blockRepository.insertBlock(block)
            _events.emit(msg)
        } catch (e: Exception) { _events.emit("Erro ao processar texto: ${e.message}") }
    }

    fun getCachedImage(imgId: String, onResult: (String?) -> Unit) = viewModelScope.launch { onResult(blockRepository.getCachedImage(imgId)) }
    fun cacheImage(imgId: String, url: String) = viewModelScope.launch(Dispatchers.IO) {
        if (blockRepository.getCachedImage(imgId) != null) return@launch
        CanvasMediaCoordinator.downloadImageBase64(url)?.let { blockRepository.saveImageCache(imgId, it) }
    }

    // Auto-organize
    fun autoOrganizeBlocks() = viewModelScope.launch {
        val allBlocks = blockRepository.getBlocksStream(_currentProjectId.value).first()
        val updated = CanvasAutoOrganizer.organize(allBlocks, canvasDimensions.value.first)
        if (updated.isNotEmpty()) {
            blockRepository.insertBlocks(updated)
            _events.emit("Canvas auto-organizado com sucesso!")
        }
    }

    // Export & Portability
    fun exportToJson(): String {
        val blocks = (uiState.value as? BlockUiState.Success)?.blocks ?: emptyList()
        return JsonPortabilityService.exportToJson(brandTitle.value, blocks)
    }

    fun syncToGallery(context: Context, onShareZip: (java.io.File) -> Unit = {}) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val currentBlocks = (uiState.value as? BlockUiState.Success)?.blocks ?: emptyList()
            CanvasExportSyncCoordinator.syncToGallery(context, currentBlocks, galleryBaseUrl.value, githubToken.value, blockRepository, onShareZip) { _events.emit(it) }
        } catch (e: Exception) { _events.emit("Erro ao sincronizar galeria: ${e.message}") }
    }

    fun loadDefaultTemplate(context: Context, clearFirst: Boolean = false) = viewModelScope.launch {
        try {
            val jsonString = context.assets.open("default_template.json").bufferedReader().use { it.readText() }
            importFromJson(jsonString, clearFirst)
        } catch (e: Exception) { _events.emit("Erro ao carregar ficha padrão: ${e.message}") }
    }

    fun importFromJson(jsonString: String, clearFirst: Boolean = false) = viewModelScope.launch {
        CanvasExportSyncCoordinator.importFromJson(jsonString, _currentProjectId.value, clearFirst, blockRepository, { setBrandTitle(it) }) { _events.emit(it) }
    }
}
