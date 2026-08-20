package com.canvasstudio.ui.block

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
    fun deleteBlock(block: BlockEntity) = viewModelScope.launch { 
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

    fun generateTestBlocks(count: Int) = viewModelScope.launch {
        val testBlocks = mutableListOf<BlockEntity>()
        val basePosX = 100f
        val basePosY = 100f
        
        for (i in 1..count) {
            val row = (i - 1) / 5
            val col = (i - 1) % 5
            val type = when(i % 3) {
                0 -> "text"
                1 -> "chart"
                else -> "image"
            }
            val content = when(type) {
                "text" -> buildJsonObject { 
                    put("text", "Conteúdo de teste para o bloco $i. **Importante** validar a performance.")
                    put("titleSize", 13)
                    put("align", "left")
                }.toString()
                "chart" -> buildJsonObject {
                    put("ninjutsu", (2..8).random().toFloat())
                    put("inteligencia", (2..8).random().toFloat())
                    put("chakra", (2..8).random().toFloat())
                    put("taijutsu", (2..8).random().toFloat())
                    put("vigor", (2..8).random().toFloat())
                    put("genjutsu", (2..8).random().toFloat())
                }.toString()
                "image" -> buildJsonObject { put("url", "https://picsum.photos/seed/$i/200/200") }.toString()
                else -> ""
            }
            
            testBlocks.add(BlockEntity(
                projectId = _currentProjectId.value,
                title = "Bloco Teste $i",
                type = type,
                posX = basePosX + (col * 250f),
                posY = basePosY + (row * 220f),
                width = 220,
                height = 180,
                contentJson = content
            ))
        }
        blockRepository.insertBlocks(testBlocks)
        _events.emit("$count blocos de teste gerados!")
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

    fun importFromJson(jsonString: String) = viewModelScope.launch {
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
                blockRepository.insertBlocks(newBlocks)
                _events.emit("${newBlocks.size} blocos importados com sucesso!")
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
