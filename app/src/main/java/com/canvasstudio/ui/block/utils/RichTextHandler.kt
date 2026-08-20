package com.canvasstudio.ui.block.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

fun parseRichText(text: String, defaultSize: Int = 14): AnnotatedString {
    if (text.startsWith("{") && text.endsWith("}")) {
        try {
            val json = Json.parseToJsonElement(text).jsonObject
            val extracted = json["text"]?.jsonPrimitive?.content ?: text
            return parseRichText(extracted, defaultSize)
        } catch (e: Exception) {}
    }

    val builder = AnnotatedString.Builder()
    var currentIndex = 0
    val regex = Regex("""(\*\*.*?\*\*)|(\*.*?\*)|(<u>.*?</u>)|(\[size=\d+\].*?\[/size\])""")
    val matches = regex.findAll(text)

    matches.forEach { match ->
        if (match.range.first > currentIndex) {
            builder.append(text.substring(currentIndex, match.range.first))
        }
        val tagText = match.value
        when {
            tagText.startsWith("**") -> builder.withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(tagText.removeSurrounding("**")) }
            tagText.startsWith("*") -> builder.withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(tagText.removeSurrounding("*")) }
            tagText.startsWith("<u>") -> builder.withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) { append(tagText.removePrefix("<u>").removeSuffix("</u>")) }
            tagText.startsWith("[size=") -> {
                val size = tagText.substringAfter("=").substringBefore("]").toIntOrNull() ?: defaultSize
                builder.withStyle(SpanStyle(fontSize = size.sp)) { append(tagText.substringAfter("]").removeSuffix("[/size]")) }
            }
        }
        currentIndex = match.range.last + 1
    }
    if (currentIndex < text.length) builder.append(text.substring(currentIndex))
    return builder.toAnnotatedString()
}
