package com.luojiaping.onmyenglish.core.domain

import com.luojiaping.onmyenglish.core.common.AppError
import com.luojiaping.onmyenglish.core.common.AppResult
import com.luojiaping.onmyenglish.core.model.AiProviderSettings
import com.luojiaping.onmyenglish.core.model.Deck
import com.luojiaping.onmyenglish.core.model.ExtractedWord
import com.luojiaping.onmyenglish.core.model.Word
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveWordsUseCase @Inject constructor(
    private val repository: WordRepository,
) {
    operator fun invoke(): Flow<List<Word>> = repository.observeWords()
}

class ObserveDecksUseCase @Inject constructor(
    private val repository: WordRepository,
) {
    operator fun invoke(): Flow<List<Deck>> = repository.observeDecks()
}

class ExtractWordsFromImageUseCase @Inject constructor(
    private val repository: AiVocabularyRepository,
) {
    suspend operator fun invoke(imageUri: String): AppResult<List<ExtractedWord>> =
        repository.extractWords(imageUri)
}

class ImportWordsUseCase @Inject constructor(
    private val repository: WordRepository,
) {
    suspend operator fun invoke(
        deckName: String,
        candidates: List<ExtractedWord>,
    ): AppResult<Int> {
        if (deckName.isBlank()) {
            return AppResult.Failure(
                AppError.Validation("Deck name is required"),
            )
        }
        val normalized = candidates
            .filter { it.headword.isNotBlank() && it.definition.isNotBlank() }
            .distinctBy { it.headword.trim().lowercase() }
        return repository.importWords(deckName.trim(), normalized)
    }
}

class SaveAiSettingsUseCase @Inject constructor(
    private val repository: AiSettingsRepository,
) {
    suspend operator fun invoke(settings: AiProviderSettings): AppResult<Unit> {
        settings.validationError()?.let { return AppResult.Failure(it) }
        return repository.save(settings.copy(baseUrl = settings.baseUrl.trimEnd('/')))
    }
}

class TestAiConnectionUseCase @Inject constructor(
    private val repository: AiVocabularyRepository,
) {
    suspend operator fun invoke(settings: AiProviderSettings): AppResult<Unit> {
        settings.validationError()?.let { return AppResult.Failure(it) }
        return repository.testConnection(settings.copy(baseUrl = settings.baseUrl.trimEnd('/')))
    }
}

private fun AiProviderSettings.validationError(): AppError.Validation? = when {
    !baseUrl.startsWith("https://") && !baseUrl.startsWith("http://") ->
        AppError.Validation(
            "Base URL must start with http:// or https://",
        )
    chatModel.isBlank() || visionModel.isBlank() ->
        AppError.Validation("Model names are required")
    temperature !in 0.0..2.0 ->
        AppError.Validation(
            "Temperature must be between 0 and 2",
        )
    baseUrl.startsWith("http://") && apiKey.isNotBlank() ->
        AppError.Validation(
            "HTTP endpoints can only be used without an API key",
        )
    else -> null
}
