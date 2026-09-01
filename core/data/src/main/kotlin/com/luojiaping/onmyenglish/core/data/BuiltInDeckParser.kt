package com.luojiaping.onmyenglish.core.data

import com.luojiaping.onmyenglish.core.common.AppError
import com.luojiaping.onmyenglish.core.common.AppResult
import com.luojiaping.onmyenglish.core.model.PartOfSpeech
import javax.inject.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class BuiltInDeckAsset(
    val id: String,
    val name: String,
    val badge: String,
    val tag: String,
    val count: Int,
    val words: List<BuiltInWordAsset>,
)

@Serializable
data class BuiltInWordAsset(
    val w: String,
    val ph: String = "",
    val tr: String = "",
    val pos: String = "OTHER",
    val frq: Int = 999999,
)

class BuiltInDeckParser @Inject constructor() {
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(text: String): AppResult<BuiltInDeckAsset> = runCatching {
        json.decodeFromString<BuiltInDeckAsset>(text)
            .also { asset ->
                require(asset.id.isNotBlank() && asset.words.isNotEmpty()) {
                    "deck asset is empty"
                }
            }
    }.fold(
        onSuccess = { AppResult.Success(it) },
        onFailure = { AppResult.Failure(AppError.Parsing("Invalid built-in deck asset", it)) },
    )

    fun partOfSpeech(raw: String): PartOfSpeech =
        runCatching { PartOfSpeech.valueOf(raw) }.getOrDefault(PartOfSpeech.OTHER)
}
