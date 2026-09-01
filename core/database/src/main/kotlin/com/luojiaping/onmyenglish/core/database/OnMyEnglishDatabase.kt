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
    version = 2,
    exportSchema = true,
)
abstract class OnMyEnglishDatabase : RoomDatabase() {
    abstract fun aiCacheDao(): AiCacheDao
    abstract fun deckDao(): DeckDao
    abstract fun importDao(): ImportDao
    abstract fun reviewDao(): ReviewDao
    abstract fun wordDao(): WordDao

    companion object {
        val MIGRATION_1_2: androidx.room.migration.Migration =
            object : androidx.room.migration.Migration(1, 2) {
                override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    db.execSQL(
                        "ALTER TABLE decks ADD COLUMN category TEXT NOT NULL DEFAULT 'CUSTOM'",
                    )
                    db.execSQL("ALTER TABLE decks ADD COLUMN badge TEXT NOT NULL DEFAULT ''")
                    db.execSQL("ALTER TABLE decks ADD COLUMN coverUri TEXT")
                }
            }
    }
}
