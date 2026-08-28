package com.luojiaping.onmyenglish.core.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): OnMyEnglishDatabase = Room.databaseBuilder(
        context,
        OnMyEnglishDatabase::class.java,
        "on-my-english.db",
    ).build()

    @Provides
    fun provideWordDao(database: OnMyEnglishDatabase): WordDao = database.wordDao()

    @Provides
    fun provideDeckDao(database: OnMyEnglishDatabase): DeckDao = database.deckDao()

    @Provides
    fun provideReviewDao(database: OnMyEnglishDatabase): ReviewDao = database.reviewDao()

    @Provides
    fun provideImportDao(database: OnMyEnglishDatabase): ImportDao = database.importDao()

    @Provides
    fun provideAiCacheDao(database: OnMyEnglishDatabase): AiCacheDao = database.aiCacheDao()
}
