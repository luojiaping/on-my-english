package com.luojiaping.onmyenglish.core.domain

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
                com.luojiaping.onmyenglish.core.common.AppError.Validation("Deck name is required"),
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
        if (!settings.baseUrl.startsWith("https://") && !settings.baseUrl.startsWith("http://")) {
            return AppResult.Failure(
                com.luojiaping.onmyenglish.core.common.AppError.Validation(
                    "Base URL must start with http:// or https://",
                ),
            )
        }
        if (settings.temperature !in 0.0..2.0) {
            return AppResult.Failure(
                com.luojiaping.onmyenglish.core.common.AppError.Validation(
                    "Temperature must be between 0 and 2",
                ),
            )
        }
        return repository.save(settings.copy(baseUrl = settings.baseUrl.trimEnd('/')))
    }
}
