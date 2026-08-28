package com.luojiaping.onmyenglish.core.ai

import com.luojiaping.onmyenglish.core.common.AppError
import com.luojiaping.onmyenglish.core.common.AppResult
import com.luojiaping.onmyenglish.core.model.AiProviderSettings
import com.luojiaping.onmyenglish.core.model.ExtractedWord
import com.luojiaping.onmyenglish.core.model.StructuredOutputMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

@Singleton
class VisionWordExtractor @Inject constructor(
    private val imageEncoder: ImageEncoder,
    private val llmClient: LlmClient,
    private val parser: WordExtractionParser,
) {
    suspend fun extract(
        imageUri: String,
        settings: AiProviderSettings,
    ): AppResult<List<ExtractedWord>> {
        if (!settings.isConfigured) {
            return AppResult.Failure(AppError.Authentication("Configure an AI provider first"))
        }
        val image = when (val result = imageEncoder.encode(imageUri)) {
            is AppResult.Success -> result.value
            is AppResult.Failure -> return result
        }

        var lastError: AppError = AppError.Parsing("AI did not return structured vocabulary")
        for (mode in StructuredOutputMode.entries) {
            val result = llmClient.complete(settings, request(settings, image, mode))
            when (result) {
                is AppResult.Success -> when (val parsed = parser.parse(result.value.text)) {
                    is AppResult.Success -> return parsed
                    is AppResult.Failure -> lastError = parsed.error
                }
                is AppResult.Failure -> {
                    lastError = result.error
                    if (!result.error.allowsFormatFallback()) return result
                }
            }
        }
        return AppResult.Failure(lastError)
    }

    suspend fun testConnection(settings: AiProviderSettings): AppResult<Unit> {
        if (!settings.isConfigured) {
            return AppResult.Failure(AppError.Authentication("API key and model are required"))
        }
        val request = LlmRequest(
            model = settings.chatModel,
            temperature = 0.0,
            messages = listOf(
                LlmMessage(LlmRole.USER, listOf(LlmContent.Text("Reply with exactly: OK"))),
            ),
        )
        return when (val result = llmClient.complete(settings, request)) {
            is AppResult.Success -> AppResult.Success(Unit)
            is AppResult.Failure -> result
        }
    }

    private fun request(
        settings: AiProviderSettings,
        image: String,
        mode: StructuredOutputMode,
    ) = LlmRequest(
        model = settings.visionModel,
        temperature = settings.temperature,
        outputMode = mode,
        jsonSchema = WORD_SCHEMA,
        messages = listOf(
            LlmMessage(
                role = LlmRole.SYSTEM,
                content = listOf(
                    LlmContent.Text(
                        "You extract English vocabulary from images. Never invent unreadable words. " +
                            "Return concise English definitions and Simplified Chinese translations.",
                    ),
                ),
            ),
            LlmMessage(
                role = LlmRole.USER,
                content = listOf(
                    LlmContent.Text(
                        "Extract every useful English word or phrase visible in this image. " +
                            "Return JSON with a top-level words array only.",
                    ),
                    LlmContent.ImageData(image),
                ),
            ),
        ),
    )

    private fun AppError.allowsFormatFallback(): Boolean = when (this) {
        is AppError.Parsing -> true
        is AppError.Remote -> statusCode == 400 || statusCode == 404 || statusCode == 422
        else -> false
    }

    private companion object {
        val WORD_SCHEMA: JsonObject = buildJsonObject {
            put("type", "object")
            putJsonObject("properties") {
                putJsonObject("words") {
                    put("type", "array")
                    putJsonObject("items") {
                        put("type", "object")
                        putJsonObject("properties") {
                            for (field in listOf(
                                "headword",
                                "phonetic",
                                "partOfSpeech",
                                "definition",
                                "translation",
                                "example",
                                "exampleTranslation",
                            )) {
                                putJsonObject(field) { put("type", "string") }
                            }
                            putJsonObject("confidence") { put("type", "number") }
                        }
                        put("required", buildJsonArray {
                            add("headword")
                            add("definition")
                            add("translation")
                        })
                        put("additionalProperties", false)
                    }
                }
            }
            put("required", buildJsonArray { add("words") })
            put("additionalProperties", false)
        }
    }
}
