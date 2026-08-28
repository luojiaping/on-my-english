package com.luojiaping.onmyenglish.core.model

import kotlinx.serialization.Serializable

@Serializable
data class AiProviderSettings(
    val baseUrl: String = DEFAULT_BASE_URL,
    val apiKey: String = "",
    val chatModel: String = "gpt-4.1-mini",
    val visionModel: String = "gpt-4.1-mini",
    val temperature: Double = 0.2,
) {
    val isConfigured: Boolean
        get() = baseUrl.isNotBlank() && apiKey.isNotBlank() && visionModel.isNotBlank()

    override fun toString(): String =
        "AiProviderSettings(baseUrl=$baseUrl, apiKey=<redacted>, chatModel=$chatModel, " +
            "visionModel=$visionModel, temperature=$temperature)"

    companion object {
        const val DEFAULT_BASE_URL = "https://api.openai.com/v1"
    }
}

@Serializable
data class ExtractedWord(
    val headword: String,
    val phonetic: String = "",
    val partOfSpeech: PartOfSpeech = PartOfSpeech.OTHER,
    val definition: String,
    val translation: String = "",
    val example: String = "",
    val exampleTranslation: String = "",
    val confidence: Double = 1.0,
)

@Serializable
enum class StructuredOutputMode {
    JSON_SCHEMA,
    JSON_OBJECT,
    PLAIN_TEXT,
}
