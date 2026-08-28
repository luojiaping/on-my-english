package com.luojiaping.onmyenglish.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        AiCacheEntity::class,
        DeckEntity::class,
        DeckWordEntity::class,
        ImportBatchEntity::class,
        ImportItemEntity::class,
        ReviewLogEntity::class,
        ReviewStateEntity::class,
        WordEntity::class,
        WordExampleEntity::class,
        WordFtsEntity::class,
        WordSenseEntity::class,
        WordTagEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class OnMyEnglishDatabase : RoomDatabase() {
    abstract fun aiCacheDao(): AiCacheDao
    abstract fun deckDao(): DeckDao
    abstract fun importDao(): ImportDao
    abstract fun reviewDao(): ReviewDao
    abstract fun wordDao(): WordDao
}
