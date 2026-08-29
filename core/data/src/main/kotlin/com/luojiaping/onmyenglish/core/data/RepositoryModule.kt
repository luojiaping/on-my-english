package com.luojiaping.onmyenglish.core.data

import com.luojiaping.onmyenglish.core.domain.AiSettingsRepository
import com.luojiaping.onmyenglish.core.domain.AiVocabularyRepository
import com.luojiaping.onmyenglish.core.domain.Sm2Scheduler
import com.luojiaping.onmyenglish.core.domain.SrsScheduler
import com.luojiaping.onmyenglish.core.domain.WordRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindWordRepository(implementation: RoomWordRepository): WordRepository

    @Binds
    @Singleton
    abstract fun bindAiSettingsRepository(
        implementation: DefaultAiSettingsRepository,
    ): AiSettingsRepository

    @Binds
    @Singleton
    abstract fun bindAiVocabularyRepository(
        implementation: DefaultAiVocabularyRepository,
    ): AiVocabularyRepository

    @Binds
    @Singleton
    abstract fun bindSrsScheduler(implementation: Sm2Scheduler): SrsScheduler
}
