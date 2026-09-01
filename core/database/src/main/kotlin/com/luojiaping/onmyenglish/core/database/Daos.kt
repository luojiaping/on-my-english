package com.luojiaping.onmyenglish.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Transaction
    @Query("SELECT * FROM words ORDER BY headword COLLATE NOCASE")
    fun observeWords(): Flow<List<WordWithDetails>>

    @Query("SELECT * FROM words WHERE normalizedHeadword = :normalized LIMIT 1")
    suspend fun findByNormalizedHeadword(normalized: String): WordEntity?

    @Upsert
    suspend fun upsertWord(word: WordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSenses(senses: List<WordSenseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamples(examples: List<WordExampleEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTags(tags: List<WordTagEntity>)

    @Query("DELETE FROM word_senses WHERE wordId = :wordId")
    suspend fun deleteSenses(wordId: String)

    @Query("DELETE FROM word_tags WHERE wordId = :wordId")
    suspend fun deleteTags(wordId: String)

    @Query("DELETE FROM words_fts WHERE wordId = :wordId")
    suspend fun deleteSearchEntry(wordId: String)

    @Insert
    suspend fun insertSearchEntry(entry: WordFtsEntity)

    @Query(
        """
        SELECT words.* FROM words
        INNER JOIN words_fts ON words.id = words_fts.wordId
        WHERE words_fts MATCH :query
        ORDER BY words.headword COLLATE NOCASE
        """,
    )
    suspend fun search(query: String): List<WordEntity>
}

@Dao
interface DeckDao {
    @Query(
        """
        SELECT decks.id, decks.name, decks.description, decks.createdAtEpochMillis,
               decks.category, decks.badge, decks.coverUri,
               COUNT(deck_words.wordId) AS wordCount,
               SUM(CASE WHEN review_states.repetitions > 0 THEN 1 ELSE 0 END) AS learnedCount
        FROM decks
        LEFT JOIN deck_words ON decks.id = deck_words.deckId
        LEFT JOIN review_states ON deck_words.wordId = review_states.wordId
        GROUP BY decks.id
        ORDER BY decks.createdAtEpochMillis DESC
        """,
    )
    fun observeDecks(): Flow<List<DeckSummary>>

    @Query("SELECT * FROM decks WHERE id = :deckId LIMIT 1")
    fun observeDeck(deckId: String): Flow<DeckEntity?>

    @Query(
        """
        SELECT words.id AS wordId, words.headword, words.phonetic,
               senses.id AS senseId, senses.definition, senses.translation,
               senses.partOfSpeech, review_states.repetitions
        FROM deck_words
        INNER JOIN words ON deck_words.wordId = words.id
        LEFT JOIN word_senses senses ON senses.wordId = words.id AND senses.sortOrder = 0
        LEFT JOIN review_states ON words.id = review_states.wordId
        WHERE deck_words.deckId = :deckId
        ORDER BY deck_words.position
        """,
    )
    fun observeDeckWords(deckId: String): Flow<List<DeckWordEntry>>

    @Query("SELECT * FROM decks WHERE normalizedName = :normalizedName LIMIT 1")
    suspend fun findByNormalizedName(normalizedName: String): DeckEntity?

    @Query("SELECT COUNT(*) != 0 FROM decks WHERE id = :deckId")
    suspend fun deckExists(deckId: String): Boolean

    @Upsert
    suspend fun upsertDeck(deck: DeckEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDeckWord(entry: DeckWordEntity): Long

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM deck_words WHERE deckId = :deckId")
    suspend fun nextPosition(deckId: String): Int
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM review_states WHERE dueAtEpochMillis <= :now ORDER BY dueAtEpochMillis")
    fun observeDue(now: Long): Flow<List<ReviewStateEntity>>

    @Upsert
    suspend fun upsertState(state: ReviewStateEntity)

    @Insert
    suspend fun insertLog(log: ReviewLogEntity)
}

@Dao
interface ImportDao {
    @Upsert
    suspend fun upsertBatch(batch: ImportBatchEntity)

    @Upsert
    suspend fun upsertItems(items: List<ImportItemEntity>)

    @Query("SELECT * FROM import_items WHERE batchId = :batchId ORDER BY headword COLLATE NOCASE")
    fun observeItems(batchId: String): Flow<List<ImportItemEntity>>
}

@Dao
interface AiCacheDao {
    @Query("SELECT * FROM ai_cache WHERE cacheKey = :key AND expiresAtEpochMillis > :now LIMIT 1")
    suspend fun getValid(key: String, now: Long): AiCacheEntity?

    @Upsert
    suspend fun upsert(entry: AiCacheEntity)

    @Query("DELETE FROM ai_cache WHERE expiresAtEpochMillis <= :now")
    suspend fun deleteExpired(now: Long)
}
