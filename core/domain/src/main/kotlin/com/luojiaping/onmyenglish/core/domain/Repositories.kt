package com.luojiaping.onmyenglish.core.domain

import com.luojiaping.onmyenglish.core.common.AppResult
import com.luojiaping.onmyenglish.core.model.AiProviderSettings
import com.luojiaping.onmyenglish.core.model.Deck
import com.luojiaping.onmyenglish.core.model.ExtractedWord
import com.luojiaping.onmyenglish.core.model.Word
import kotlinx.coroutines.flow.Flow

interface WordRepository {
    fun observeWords(): Flow<List<Word>>

    fun observeDecks(): Flow<List<Deck>>

    suspend fun importWords(
        deckName: String,
        candidates: List<ExtractedWord>,
    ): AppResult<Int>
}

interface AiSettingsRepository {
    val settings: Flow<AiProviderSettings>

    suspend fun save(settings: AiProviderSettings): AppResult<Unit>
}

interface AiVocabularyRepository {
    suspend fun extractWords(imageUri: String): AppResult<List<ExtractedWord>>

    suspend fun testConnection(settings: AiProviderSettings): AppResult<Unit>
}
