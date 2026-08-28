package com.luojiaping.onmyenglish.core.data

import com.luojiaping.onmyenglish.core.ai.VisionWordExtractor
import com.luojiaping.onmyenglish.core.common.AppError
import com.luojiaping.onmyenglish.core.common.AppResult
import com.luojiaping.onmyenglish.core.datastore.AiSettingsDataSource
import com.luojiaping.onmyenglish.core.domain.AiSettingsRepository
import com.luojiaping.onmyenglish.core.domain.AiVocabularyRepository
import com.luojiaping.onmyenglish.core.model.AiProviderSettings
import com.luojiaping.onmyenglish.core.model.ExtractedWord
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@Singleton
class DefaultAiSettingsRepository @Inject constructor(
    private val dataSource: AiSettingsDataSource,
) : AiSettingsRepository {
    override val settings: Flow<AiProviderSettings> = dataSource.settings

    override suspend fun save(settings: AiProviderSettings): AppResult<Unit> = runCatching {
        dataSource.save(settings)
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { error ->
            AppResult.Failure(AppError.Storage(error.message ?: "Could not save AI settings", error))
        },
    )
}

@Singleton
class DefaultAiVocabularyRepository @Inject constructor(
    private val settingsRepository: AiSettingsRepository,
    private val extractor: VisionWordExtractor,
) : AiVocabularyRepository {
    override suspend fun extractWords(imageUri: String): AppResult<List<ExtractedWord>> =
        extractor.extract(imageUri, settingsRepository.settings.first())

    override suspend fun testConnection(settings: AiProviderSettings): AppResult<Unit> =
        extractor.testConnection(settings)
}
