package com.luojiaping.onmyenglish.core.data

import androidx.room.withTransaction
import com.luojiaping.onmyenglish.core.common.AppDispatcher
import com.luojiaping.onmyenglish.core.common.AppError
import com.luojiaping.onmyenglish.core.common.AppResult
import com.luojiaping.onmyenglish.core.common.Dispatcher
import com.luojiaping.onmyenglish.core.database.DeckEntity
import com.luojiaping.onmyenglish.core.database.DeckWordEntity
import com.luojiaping.onmyenglish.core.database.OnMyEnglishDatabase
import com.luojiaping.onmyenglish.core.database.ReviewStateEntity
import com.luojiaping.onmyenglish.core.database.WordEntity
import com.luojiaping.onmyenglish.core.database.WordExampleEntity
import com.luojiaping.onmyenglish.core.database.WordFtsEntity
import com.luojiaping.onmyenglish.core.database.WordSenseEntity
import com.luojiaping.onmyenglish.core.database.WordTagEntity
import com.luojiaping.onmyenglish.core.domain.DeckWord
import com.luojiaping.onmyenglish.core.domain.WordRepository
import com.luojiaping.onmyenglish.core.model.Deck
import com.luojiaping.onmyenglish.core.model.ExtractedWord
import com.luojiaping.onmyenglish.core.model.ReviewState
import com.luojiaping.onmyenglish.core.model.Word
import com.luojiaping.onmyenglish.core.model.WordSource
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
class RoomWordRepository @Inject constructor(
    private val database: OnMyEnglishDatabase,
    @Dispatcher(AppDispatcher.IO) private val ioDispatcher: CoroutineDispatcher,
) : WordRepository {
    override fun observeWords(): Flow<List<Word>> = database.wordDao()
        .observeWords()
        .map { words -> words.map { it.asModel() } }
        .flowOn(ioDispatcher)

    override fun observeDecks(): Flow<List<Deck>> = database.deckDao()
        .observeDecks()
        .map { decks -> decks.map { it.asModel() } }
        .flowOn(ioDispatcher)

    override fun observeDeckWords(deckId: String): Flow<List<DeckWord>> =
        database.deckDao()
            .observeDeckWords(deckId)
            .map { entries ->
                entries.map { entry ->
                    DeckWord(
                        wordId = entry.wordId,
                        headword = entry.headword,
                        phonetic = entry.phonetic,
                        definition = entry.definition,
                        translation = entry.translation,
                        learned = (entry.repetitions ?: 0) > 0,
                    )
                }
            }
            .flowOn(ioDispatcher)

    override suspend fun importWords(
        deckName: String,
        candidates: List<ExtractedWord>,
    ): AppResult<Int> = withContext(ioDispatcher) {
        runCatching {
            database.withTransaction {
                val now = System.currentTimeMillis()
                val deckDao = database.deckDao()
                val wordDao = database.wordDao()
                val reviewDao = database.reviewDao()
                val normalizedDeckName = deckName.normalized()
                val deck = deckDao.findByNormalizedName(normalizedDeckName) ?: run {
                    val newDeck = DeckEntity(
                        id = UUID.randomUUID().toString(),
                        name = deckName,
                        normalizedName = normalizedDeckName,
                        description = "",
                        createdAtEpochMillis = now,
                        category = "AI_VISION",
                        badge = "图",
                        coverUri = null,
                    )
                    deckDao.upsertDeck(newDeck)
                    newDeck
                }

                var position = deckDao.nextPosition(deck.id)
                var inserted = 0
                for (candidate in candidates) {
                    val normalizedHeadword = candidate.headword.normalized()
                    val existing = wordDao.findByNormalizedHeadword(normalizedHeadword)
                    val wordId = existing?.id ?: UUID.randomUUID().toString()
                    val senseId = UUID.randomUUID().toString()

                    wordDao.upsertWord(
                        WordEntity(
                            id = wordId,
                            headword = candidate.headword.trim(),
                            normalizedHeadword = normalizedHeadword,
                            phonetic = candidate.phonetic.trim(),
                            note = existing?.note.orEmpty(),
                            source = WordSource.AI_VISION.name,
                            createdAtEpochMillis = existing?.createdAtEpochMillis ?: now,
                        ),
                    )
                    wordDao.deleteSenses(wordId)
                    wordDao.insertSenses(
                        listOf(
                            WordSenseEntity(
                                id = senseId,
                                wordId = wordId,
                                partOfSpeech = candidate.partOfSpeech.name,
                                definition = candidate.definition.trim(),
                                translation = candidate.translation.trim(),
                                sortOrder = 0,
                            ),
                        ),
                    )
                    if (candidate.example.isNotBlank()) {
                        wordDao.insertExamples(
                            listOf(
                                WordExampleEntity(
                                    id = UUID.randomUUID().toString(),
                                    senseId = senseId,
                                    text = candidate.example.trim(),
                                    translation = candidate.exampleTranslation.trim(),
                                    sortOrder = 0,
                                ),
                            ),
                        )
                    }
                    wordDao.insertTags(listOf(WordTagEntity(wordId, AI_IMPORT_TAG)))
                    wordDao.deleteSearchEntry(wordId)
                    wordDao.insertSearchEntry(
                        WordFtsEntity(
                            wordId = wordId,
                            headword = candidate.headword,
                            searchableText = listOf(candidate.definition, candidate.translation)
                                .filter(String::isNotBlank)
                                .joinToString(" "),
                        ),
                    )

                    val linkResult = deckDao.insertDeckWord(
                        DeckWordEntity(
                            deckId = deck.id,
                            wordId = wordId,
                            position = position,
                            addedAtEpochMillis = now,
                        ),
                    )
                    if (linkResult != -1L) {
                        position += 1
                        inserted += 1
                        reviewDao.upsertState(
                            ReviewStateEntity(
                                wordId = wordId,
                                repetitions = 0,
                                intervalDays = 0,
                                easeFactor = ReviewState.DEFAULT_EASE_FACTOR,
                                dueAtEpochMillis = now,
                                lastReviewedAtEpochMillis = null,
                            ),
                        )
                    }
                }
                inserted
            }
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { error ->
                AppResult.Failure(AppError.Storage(error.message ?: "Could not import words", error))
            },
        )
    }

    private fun String.normalized(): String = trim().lowercase(Locale.ROOT)

    private companion object {
        const val AI_IMPORT_TAG = "AI import"
    }
}
