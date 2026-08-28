package com.luojiaping.onmyenglish.core.ai

import com.luojiaping.onmyenglish.core.common.AppError
import com.luojiaping.onmyenglish.core.common.AppResult
import com.luojiaping.onmyenglish.core.model.ExtractedWord
import com.luojiaping.onmyenglish.core.model.PartOfSpeech
import com.luojiaping.onmyenglish.core.network.NetworkJson
import javax.inject.Inject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

class WordExtractionParser @Inject constructor(
    @NetworkJson private val json: Json,
) {
    fun parse(raw: String): AppResult<List<ExtractedWord>> = runCatching {
        val jsonSlice = findFirstJsonValue(stripMarkdownFence(raw))
            ?: error("AI response did not contain JSON")
        val root = json.parseToJsonElement(jsonSlice)
        val words = when (root) {
            is JsonArray -> root
            is JsonObject -> root["words"]?.jsonArray ?: error("JSON object has no words array")
            else -> error("Expected a JSON object or array")
        }
        words.mapNotNull(::toExtractedWord)
            .filter { it.headword.isNotBlank() && it.definition.isNotBlank() }
            .distinctBy { it.headword.trim().lowercase() }
            .ifEmpty { error("AI response did not contain usable words") }
    }.fold(
        onSuccess = { AppResult.Success(it) },
        onFailure = { error ->
            AppResult.Failure(AppError.Parsing(error.message ?: "Could not parse AI words", error))
        },
    )

    private fun toExtractedWord(element: JsonElement): ExtractedWord? {
        val value = element as? JsonObject ?: return null
        fun string(vararg keys: String): String = keys.firstNotNullOfOrNull { key ->
            value[key]?.jsonPrimitive?.contentOrNull
        }.orEmpty().trim()

        val headword = string("headword", "word", "term")
        val definition = string("definition", "meaning", "explanation")
        if (headword.isBlank() || definition.isBlank()) return null

        val partOfSpeech = string("partOfSpeech", "part_of_speech", "pos")
            .uppercase()
            .replace(' ', '_')
            .let { raw -> PartOfSpeech.entries.firstOrNull { it.name == raw } }
            ?: PartOfSpeech.OTHER

        return ExtractedWord(
            headword = headword,
            phonetic = string("phonetic", "pronunciation"),
            partOfSpeech = partOfSpeech,
            definition = definition,
            translation = string("translation", "chinese", "meaningZh"),
            example = string("example", "exampleSentence"),
            exampleTranslation = string("exampleTranslation", "example_translation"),
            confidence = value["confidence"]?.jsonPrimitive?.doubleOrNull
                ?.coerceIn(0.0, 1.0) ?: 1.0,
        )
    }

    private fun stripMarkdownFence(value: String): String = value.trim()
        .removePrefix("```json")
        .removePrefix("```JSON")
        .removePrefix("```")
        .removeSuffix("```")
        .trim()

    internal fun findFirstJsonValue(value: String): String? {
        val start = value.indexOfFirst { it == '{' || it == '[' }
        if (start < 0) return null

        val stack = ArrayDeque<Char>()
        var inString = false
        var escaped = false
        for (index in start until value.length) {
            val char = value[index]
            if (inString) {
                when {
                    escaped -> escaped = false
                    char == '\\' -> escaped = true
                    char == '"' -> inString = false
                }
                continue
            }
            when (char) {
                '"' -> inString = true
                '{', '[' -> stack.addLast(char)
                '}' -> {
                    if (stack.removeLastOrNull() != '{') return null
                    if (stack.isEmpty()) return value.substring(start, index + 1)
                }
                ']' -> {
                    if (stack.removeLastOrNull() != '[') return null
                    if (stack.isEmpty()) return value.substring(start, index + 1)
                }
            }
        }
        return null
    }
}
