package com.luojiaping.onmyenglish.core.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
internal data class OpenAiChatRequest(
    val model: String,
    val messages: List<OpenAiMessage>,
    val temperature: Double,
    val stream: Boolean,
    @SerialName("response_format") val responseFormat: JsonObject? = null,
)

@Serializable
internal data class OpenAiMessage(
    val role: String,
    val content: JsonElement,
)

@Serializable
internal data class OpenAiChatResponse(
    val choices: List<OpenAiChoice> = emptyList(),
    val usage: OpenAiUsage? = null,
)

@Serializable
internal data class OpenAiChoice(
    val message: OpenAiResponseMessage? = null,
    val delta: OpenAiResponseMessage? = null,
)

@Serializable
internal data class OpenAiResponseMessage(
    val content: String? = null,
)

@Serializable
internal data class OpenAiUsage(
    @SerialName("prompt_tokens") val promptTokens: Int? = null,
    @SerialName("completion_tokens") val completionTokens: Int? = null,
)

@Serializable
internal data class OpenAiModelsResponse(
    val data: List<OpenAiModelSummary> = emptyList(),
)

@Serializable
internal data class OpenAiModelSummary(
    val id: String? = null,
)

@Serializable
internal data class OpenAiErrorEnvelope(
    val error: OpenAiError? = null,
)

@Serializable
internal data class OpenAiError(
    val message: String? = null,
)
