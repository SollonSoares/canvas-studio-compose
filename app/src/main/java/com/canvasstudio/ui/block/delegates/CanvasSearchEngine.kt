package com.canvasstudio.ui.block.delegates

import com.canvasstudio.data.local.entity.BlockEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.*
import java.text.Normalizer

class CanvasSearchEngine {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _appliedQuery = MutableStateFlow("")
    val appliedQuery: StateFlow<String> = _appliedQuery.asStateFlow()

    private val searchableCache = mutableMapOf<Long, String>()
    private val contentHashCache = mutableMapOf<Long, Int>()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _appliedQuery.value = ""
        }
    }

    fun applySearch() {
        _appliedQuery.value = _searchQuery.value
    }

    fun filterBlocks(blocks: List<BlockEntity>, query: String, modules: Map<String, Boolean>): List<BlockEntity> {
        val filteredByModule = blocks.filter { modules[it.type.lowercase()] ?: true }
        if (query.isBlank()) {
            return filteredByModule
        }

        val words = query.normalize().split("\\s+".toRegex()).filter { it.isNotEmpty() }
        return filteredByModule.filter { block ->
            val searchableText = getSearchableText(block)
            words.all { word -> searchableText.contains(word) }
        }
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

    private fun extractTextFromJson(element: JsonElement): String {
        return when (element) {
            is JsonPrimitive -> element.content
            is JsonObject -> {
                val searchableKeys = listOf("text", "html", "title", "url", "ninjutsu", "inteligencia", "chakra", "taijutsu", "vigor", "genjutsu", "value", "headers", "rows")
                element.filter { it.key.lowercase() in searchableKeys }
                    .values.joinToString(" ") { extractTextFromJson(it) }
            }
            is JsonArray -> element.joinToString(" ") { extractTextFromJson(it) }
            is JsonNull -> ""
        }
    }

    private fun String.normalize(): String {
        return Normalizer.normalize(this, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .lowercase()
            .trim()
    }
}
