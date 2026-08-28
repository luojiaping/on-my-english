package com.luojiaping.onmyenglish.core.ai

import com.luojiaping.onmyenglish.core.common.AppResult
import com.luojiaping.onmyenglish.core.model.AiProviderSettings
import com.luojiaping.onmyenglish.core.model.StructuredOutputMode
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonObject

enum class LlmRole(val wireValue: String) {
    SYSTEM("system"),
    USER("user"),
    ASSISTANT("assistant"),
}

sealed interface LlmContent {
    data class Text(val text: String) : LlmContent

    data class ImageData(val dataUrl: String, val detail: String = "auto") : LlmContent
}

data class LlmMessage(
    val role: LlmRole,
    val content: List<LlmContent>,
)

data class LlmRequest(
    val model: String,
    val messages: List<LlmMessage>,
    val temperature: Double,
    val outputMode: StructuredOutputMode = StructuredOutputMode.PLAIN_TEXT,
    val jsonSchema: JsonObject? = null,
)

data class LlmResponse(
    val text: String,
    val promptTokens: Int? = null,
    val completionTokens: Int? = null,
)

interface LlmClient {
    suspend fun complete(
        settings: AiProviderSettings,
        request: LlmRequest,
    ): AppResult<LlmResponse>

    fun stream(
        settings: AiProviderSettings,
        request: LlmRequest,
    ): Flow<AppResult<String>>
}
