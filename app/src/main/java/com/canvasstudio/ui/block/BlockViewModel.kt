package com.canvasstudio.ui.block

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.canvasstudio.data.local.dao.BlockDao
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
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.*

class BlockViewModel(
    private val blockDao: BlockDao,
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

    fun setBrandTitle(title: String) = viewModelScope.launch {
        userPreferencesManager.setBrandTitle(title)
    }

    @OptIn(FlowPreview::class)
    val uiState = combine(
        blockDao.getAllBlocks(),
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
                val searchableKeys = listOf("text", "html", "title", "url", "ninjutsu", "inteligencia", "chakra", "taijutsu", "vigor", "genjutsu")
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

    fun insertBlock(block: BlockEntity) = viewModelScope.launch { blockDao.insert(block) }
    fun updateBlock(block: BlockEntity) = viewModelScope.launch { 
        blockDao.update(block) 
    }

    // Função para atualização imediata na UI e persistência controlada
    fun updateBlockLive(block: BlockEntity) {
        viewModelScope.launch {
            blockDao.update(block)
        }
    }
    fun deleteBlock(block: BlockEntity) = viewModelScope.launch { blockDao.delete(block) }
    fun clearCanvas() = viewModelScope.launch { 
        blockDao.clearCanvas() 
        searchableCache.clear()
        contentHashCache.clear()
    }

    fun autoOrganizeBlocks() = viewModelScope.launch {
        // 1. Buscamos TODOS os blocos do banco para garantir que a organização seja total
        // e não cause sobreposição com blocos ocultos por filtros.
        val allBlocks = blockDao.getAllBlocks().first()
        if (allBlocks.isEmpty()) return@launch

        // 2. Ordenamos por posição para preservar a sequência lógica pretendida pelo usuário
        val sortedBlocks = allBlocks.sortedWith(compareBy<BlockEntity>({ it.posY }, { it.posX }))

        val padding = 60f
        val startX = 100f
        val startY = 100f
        val maxCanvasWidth = 1600f 
        
        var currentX = startX
        var currentY = startY
        var currentRowMaxHeight = 0f
        
        val updatedBlocks = mutableListOf<BlockEntity>()
        
        sortedBlocks.forEach { block ->
            // 3. Validação de Espaço: Se o bloco + sua largura ultrapassa o limite da "folha",
            // movemos a "caneta" para o início da próxima linha.
            if (currentX + block.width > maxCanvasWidth && currentX > startX) {
                currentX = startX
                currentY += currentRowMaxHeight + padding
                currentRowMaxHeight = 0f
            }
            
            // 4. Posicionamento Absoluto Baseado no Relativo:
            // Colocamos o bloco na posição atual da caneta.
            updatedBlocks.add(block.copy(
                posX = currentX,
                posY = currentY
            ))
            
            // 5. Atualização da Caneta: Movemos o X para o final deste bloco + padding.
            // O próximo bloco será posicionado EXATAMENTE em relação a este.
            currentX += block.width.toFloat() + padding
            
            // 6. Controle de Colisão Vertical: 
            // Registramos o bloco mais alto da linha para que a próxima linha comece abaixo dele.
            if (block.height.toFloat() > currentRowMaxHeight) {
                currentRowMaxHeight = block.height.toFloat()
            }
        }

        // Persistimos as novas coordenadas no banco de dados.
        blockDao.insertAll(updatedBlocks)
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
                projectId = 0,
                title = "Bloco Teste $i",
                type = type,
                posX = basePosX + (col * 250f),
                posY = basePosY + (row * 220f),
                width = 220,
                height = 180,
                contentJson = content
            ))
        }
        blockDao.insertAll(testBlocks)
        _events.emit("$count blocos de teste gerados!")
    }

    fun exportToJson(): String {
        val state = uiState.value
        val blocks = if (state is BlockUiState.Success) state.blocks else emptyList()
        
        val json = Json { prettyPrint = true }
        val root = buildJsonObject {
            put("app_brand_title", "Canvas Studio Export")
            blocks.forEachIndexed { index, block ->
                val blockKey = "data_${block.id.takeIf { it > 0 } ?: index}"
                put(blockKey, buildJsonObject {
                    put("id", block.id.toString())
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
                            putJsonArray("campos") {
                                addJsonObject {
                                    put("html", text) // Mantemos o texto processado (markdown)
                                    put("className", "sub-campo")
                                }
                            }
                        }
                        "chart" -> {
                            put("inputs", content ?: buildJsonObject {})
                        }
                        "image" -> {
                            put("url", content?.get("url")?.jsonPrimitive?.content ?: "")
                        }
                        else -> {
                            // Para tipos desconhecidos, tenta colocar o conteúdo original
                            content?.forEach { (k, v) -> put(k, v) }
                        }
                    }
                })
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
            
            if (jsonElement is JsonObject) {
                jsonElement.forEach { (_, element) ->
                    if (element is JsonObject) {
                        newBlocks.add(parseBlockObject(element))
                    }
                }
            } else if (jsonElement is JsonArray) {
                jsonElement.forEach { element ->
                    if (element is JsonObject) {
                        newBlocks.add(parseBlockObject(element))
                    }
                }
            }

            if (newBlocks.isNotEmpty()) {
                blockDao.insertAll(newBlocks)
                _events.emit("${newBlocks.size} blocos importados!")
            } else {
                _events.emit("Nenhum bloco encontrado no formato correto")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _events.emit("Erro na importação: ${e.message}")
        }
    }

    private fun parseBlockObject(obj: JsonObject): BlockEntity {
        val type = obj["type"]?.jsonPrimitive?.content ?: "text"
        val title = obj["title"]?.jsonPrimitive?.content ?: "Bloco"
        
        val contentJson = when (type.lowercase()) {
            "chart" -> {
                val inputs = obj["inputs"]?.jsonObject ?: obj["status"]?.jsonObject ?: obj
                buildJsonObject {
                    put("ninjutsu", inputs["ninjutsu"]?.jsonPrimitive?.floatOrNull ?: 4f)
                    put("inteligencia", inputs["inteligencia"]?.jsonPrimitive?.floatOrNull ?: 4f)
                    put("chakra", inputs["chakra"]?.jsonPrimitive?.floatOrNull ?: inputs["chakraMax"]?.jsonPrimitive?.floatOrNull ?: 4f)
                    put("taijutsu", inputs["taijutsu"]?.jsonPrimitive?.floatOrNull ?: 4f)
                    put("vigor", inputs["vigor"]?.jsonPrimitive?.floatOrNull ?: 4f)
                    put("genjutsu", inputs["genjutsu"]?.jsonPrimitive?.floatOrNull ?: 4f)
                }.toString()
            }
            "text" -> {
                val campos = obj["campos"]?.jsonArray
                val rawText = if (campos != null) {
                    campos.joinToString("\n") { 
                        it.jsonObject["html"]?.jsonPrimitive?.content ?: "" 
                    }
                } else {
                    obj["text"]?.jsonPrimitive?.content ?: obj["html"]?.jsonPrimitive?.content ?: ""
                }
                
                // Conversão de HTML para o formato de Rich Text do app
                val processedText = rawText
                    .replace("<b>", "**").replace("</b>", "**")
                    .replace("<strong>", "**").replace("</strong>", "**")
                    .replace("<i>", "*").replace("</i>", "*")
                    .replace("<em>", "*").replace("</em>", "*")
                    .replace("&nbsp;", " ")
                    .replace("&amp;", "&")
                    .replace("<br>", "\n")
                    .replace("<div>", "").replace("</div>", "\n")
                    .replace(Regex("<span[^>]*>"), "")
                    .replace("</span>", "")
                    .replace(Regex("<[^>]*>"), "") // Remove qualquer outra tag HTML restante
                
                buildJsonObject {
                    put("text", processedText.trim())
                    put("titleSize", 13)
                    put("titleBold", true)
                    put("align", "left")
                }.toString()
            }
            "image" -> {
                val url = obj["url"]?.jsonPrimitive?.content ?: obj["src"]?.jsonPrimitive?.content ?: ""
                buildJsonObject { put("url", url) }.toString()
            }
            else -> obj.toString()
        }

        return BlockEntity(
            projectId = 0,
            title = title,
            type = type,
            posX = obj["left"]?.jsonPrimitive?.content?.replace("px", "")?.toFloatOrNull() ?: 100f,
            posY = obj["top"]?.jsonPrimitive?.content?.replace("px", "")?.toFloatOrNull() ?: 100f,
            width = obj["width"]?.jsonPrimitive?.content?.replace("px", "")?.toIntOrNull() ?: 220,
            height = obj["height"]?.jsonPrimitive?.content?.replace("px", "")?.toIntOrNull() ?: 180,
            contentJson = contentJson
        )
    }
}