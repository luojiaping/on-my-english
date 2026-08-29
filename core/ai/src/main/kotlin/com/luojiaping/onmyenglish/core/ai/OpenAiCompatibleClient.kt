package com.luojiaping.onmyenglish.core.ai

import com.luojiaping.onmyenglish.core.common.AppError
import com.luojiaping.onmyenglish.core.common.AppResult
import com.luojiaping.onmyenglish.core.model.AiProviderSettings
import com.luojiaping.onmyenglish.core.model.StructuredOutputMode
import com.luojiaping.onmyenglish.core.network.NetworkJson
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.utils.io.readUTF8Line
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

@Singleton
class OpenAiCompatibleClient @Inject constructor(
    private val httpClient: HttpClient,
    @NetworkJson private val json: Json,
) : LlmClient {
    override suspend fun complete(
        settings: AiProviderSettings,
        request: LlmRequest,
    ): AppResult<LlmResponse> {
        insecureCredentialError(settings)?.let { return AppResult.Failure(it) }
        return runCatching {
            val response = httpClient.post(endpoint(settings)) {
                addAuthorization(settings)
                setBody(request.toOpenAiRequest(stream = false))
            }
            val rawBody = response.bodyAsText()
            if (!response.status.isSuccess()) {
                return mapHttpError(response.status.value, rawBody)
            }
            val payload = json.decodeFromString<OpenAiChatResponse>(rawBody)
            val text = payload.choices.firstOrNull()?.message?.content
                ?: return AppResult.Failure(
                    AppError.Parsing("AI response did not contain message content"),
                )
            AppResult.Success(
                LlmResponse(
                    text = text,
                    promptTokens = payload.usage?.promptTokens,
                    completionTokens = payload.usage?.completionTokens,
                ),
            )
        }.getOrElse { error ->
            AppResult.Failure(AppError.Network(error.message ?: "AI request failed", error))
        }
    }

    override fun stream(
        settings: AiProviderSettings,
        request: LlmRequest,
    ): Flow<AppResult<String>> = flow {
        insecureCredentialError(settings)?.let {
            emit(AppResult.Failure(it))
            return@flow
        }
        runCatching {
            httpClient.preparePost(endpoint(settings)) {
                addAuthorization(settings)
                setBody(request.toOpenAiRequest(stream = true))
            }.execute { response ->
                if (!response.status.isSuccess()) {
                    emit(mapHttpError(response.status.value, response.bodyAsText()))
                    return@execute
                }
                val channel = response.bodyAsChannel()
                while (!channel.isClosedForRead) {
                    val line = channel.readLine() ?: break
                    if (!line.startsWith(SSE_PREFIX)) continue
                    val data = line.removePrefix(SSE_PREFIX).trim()
                    if (data == SSE_DONE) break
                    val chunk = json.decodeFromString<OpenAiChatResponse>(data)
                    chunk.choices.firstOrNull()?.delta?.content
                        ?.takeIf(String::isNotEmpty)
                        ?.let { emit(AppResult.Success(it)) }
                }
            }
        }.onFailure { error ->
            emit(AppResult.Failure(AppError.Network(error.message ?: "AI stream failed", error)))
        }
    }

    private fun LlmRequest.toOpenAiRequest(stream: Boolean): OpenAiChatRequest =
        OpenAiChatRequest(
            model = model,
            messages = messages.map { message ->
                OpenAiMessage(
                    role = message.role.wireValue,
                    content = message.content.toJson(),
                )
            },
            temperature = temperature,
            stream = stream,
            responseFormat = responseFormat(),
        )

    private fun List<LlmContent>.toJson(): JsonElement {
        if (size == 1 && first() is LlmContent.Text) {
            return JsonPrimitive((first() as LlmContent.Text).text)
        }
        return buildJsonArray {
            forEach { part ->
                when (part) {
                    is LlmContent.Text -> add(
                        buildJsonObject {
                            put("type", "text")
                            put("text", part.text)
                        },
                    )
                    is LlmContent.ImageData -> add(
                        buildJsonObject {
                            put("type", "image_url")
                            putJsonObject("image_url") {
                                put("url", part.dataUrl)
                                put("detail", part.detail)
                            }
                        },
                    )
                }
            }
        }
    }

    private fun HttpRequestBuilder.addAuthorization(
        settings: AiProviderSettings,
    ) {
        if (settings.apiKey.isNotBlank()) {
            header(HttpHeaders.Authorization, "Bearer ${settings.apiKey}")
        }
    }

    private fun LlmRequest.responseFormat(): JsonObject? = when (outputMode) {
        StructuredOutputMode.JSON_SCHEMA -> buildJsonObject {
            put("type", "json_schema")
            putJsonObject("json_schema") {
                put("name", "vocabulary_extraction")
                put("strict", true)
                put("schema", jsonSchema ?: JsonObject(emptyMap()))
            }
        }
        StructuredOutputMode.JSON_OBJECT -> buildJsonObject { put("type", "json_object") }
        StructuredOutputMode.PLAIN_TEXT -> null
    }

    private fun mapHttpError(statusCode: Int, rawBody: String): AppResult.Failure {
        val providerMessage = runCatching {
            json.decodeFromString<OpenAiErrorEnvelope>(rawBody).error?.message
        }.getOrNull()?.take(300)
        val message = providerMessage ?: "AI provider returned HTTP $statusCode"
        val error = when (statusCode) {
            401, 403 -> AppError.Authentication(message)
            429 -> AppError.RateLimited(message)
            else -> AppError.Remote(
                statusCode = statusCode,
                message = message,
                retryable = statusCode >= 500,
            )
        }
        return AppResult.Failure(error)
    }

    private fun endpoint(settings: AiProviderSettings): String =
        "${settings.baseUrl.trimEnd('/')}/chat/completions"

    private fun insecureCredentialError(settings: AiProviderSettings): AppError.Validation? =
        if (settings.baseUrl.startsWith("http://") && settings.apiKey.isNotBlank()) {
            AppError.Validation("Refusing to send an API key over HTTP")
        } else {
            null
        }

    private companion object {
        const val SSE_DONE = "[DONE]"
        const val SSE_PREFIX = "data:"
    }
}
