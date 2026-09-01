package com.luojiaping.onmyenglish.core.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Fts4
import androidx.room.Index

@Entity(
    tableName = "words",
    indices = [Index(value = ["normalizedHeadword"], unique = true)],
)
data class WordEntity(
    @androidx.room.PrimaryKey val id: String,
    val headword: String,
    val normalizedHeadword: String,
    val phonetic: String,
    val note: String,
    val source: String,
    val createdAtEpochMillis: Long,
)

@Entity(
    tableName = "word_tags",
    primaryKeys = ["wordId", "tag"],
    foreignKeys = [
        ForeignKey(
            entity = WordEntity::class,
            parentColumns = ["id"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("wordId")],
)
data class WordTagEntity(
    val wordId: String,
    val tag: String,
)

@Entity(
    tableName = "word_senses",
    foreignKeys = [
        ForeignKey(
            entity = WordEntity::class,
            parentColumns = ["id"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("wordId")],
)
data class WordSenseEntity(
    @androidx.room.PrimaryKey val id: String,
    val wordId: String,
    val partOfSpeech: String,
    val definition: String,
    val translation: String,
    val sortOrder: Int,
)

@Entity(
    tableName = "word_examples",
    foreignKeys = [
        ForeignKey(
            entity = WordSenseEntity::class,
            parentColumns = ["id"],
            childColumns = ["senseId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("senseId")],
)
data class WordExampleEntity(
    @androidx.room.PrimaryKey val id: String,
    val senseId: String,
    val text: String,
    val translation: String,
    val sortOrder: Int,
)

@Entity(
    tableName = "decks",
    indices = [Index(value = ["normalizedName"], unique = true)],
)
data class DeckEntity(
    @androidx.room.PrimaryKey val id: String,
    val name: String,
    val normalizedName: String,
    val description: String,
    val createdAtEpochMillis: Long,
    val category: String = "CUSTOM",
    val badge: String = "",
    val coverUri: String? = null,
)

@Entity(
    tableName = "deck_words",
    primaryKeys = ["deckId", "wordId"],
    foreignKeys = [
        ForeignKey(
            entity = DeckEntity::class,
            parentColumns = ["id"],
            childColumns = ["deckId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = WordEntity::class,
            parentColumns = ["id"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("deckId"), Index("wordId")],
)
data class DeckWordEntity(
    val deckId: String,
    val wordId: String,
    val position: Int,
    val addedAtEpochMillis: Long,
)

@Entity(
    tableName = "review_states",
    foreignKeys = [
        ForeignKey(
            entity = WordEntity::class,
            parentColumns = ["id"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class ReviewStateEntity(
    @androidx.room.PrimaryKey val wordId: String,
    val repetitions: Int,
    val intervalDays: Int,
    val easeFactor: Double,
    val dueAtEpochMillis: Long,
    val lastReviewedAtEpochMillis: Long?,
)

@Entity(
    tableName = "review_logs",
    foreignKeys = [
        ForeignKey(
            entity = WordEntity::class,
            parentColumns = ["id"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("wordId"), Index("reviewedAtEpochMillis")],
)
data class ReviewLogEntity(
    @androidx.room.PrimaryKey val id: String,
    val wordId: String,
    val grade: String,
    val reviewedAtEpochMillis: Long,
    val responseTimeMillis: Long,
)

@Entity(tableName = "import_batches")
data class ImportBatchEntity(
    @androidx.room.PrimaryKey val id: String,
    val imageUri: String,
    val status: String,
    val createdAtEpochMillis: Long,
    val errorMessage: String?,
)

@Entity(
    tableName = "import_items",
    foreignKeys = [
        ForeignKey(
            entity = ImportBatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["batchId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("batchId")],
)
data class ImportItemEntity(
    @androidx.room.PrimaryKey val id: String,
    val batchId: String,
    val headword: String,
    val phonetic: String,
    val partOfSpeech: String,
    val definition: String,
    val translation: String,
    val example: String,
    val exampleTranslation: String,
    val confidence: Double,
    val selected: Boolean,
)

@Entity(tableName = "ai_cache", indices = [Index("expiresAtEpochMillis")])
data class AiCacheEntity(
    @androidx.room.PrimaryKey val cacheKey: String,
    val payload: String,
    val createdAtEpochMillis: Long,
    val expiresAtEpochMillis: Long,
)

@Fts4
@Entity(tableName = "words_fts")
data class WordFtsEntity(
    val wordId: String,
    val headword: String,
    val searchableText: String,
)
