package com.luojiaping.onmyenglish.core.data

import android.content.Context
import androidx.room.withTransaction
import com.luojiaping.onmyenglish.core.common.AppDispatcher
import com.luojiaping.onmyenglish.core.common.Dispatcher
import com.luojiaping.onmyenglish.core.database.DeckEntity
import com.luojiaping.onmyenglish.core.database.DeckWordEntity
import com.luojiaping.onmyenglish.core.database.OnMyEnglishDatabase
import com.luojiaping.onmyenglish.core.database.ReviewStateEntity
import com.luojiaping.onmyenglish.core.database.WordEntity
import com.luojiaping.onmyenglish.core.database.WordFtsEntity
import com.luojiaping.onmyenglish.core.database.WordSenseEntity
import com.luojiaping.onmyenglish.core.database.WordTagEntity
import com.luojiaping.onmyenglish.core.model.ReviewState
import com.luojiaping.onmyenglish.core.model.WordSource
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import java.util.zip.GZIPInputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@Singleton
class BuiltInDeckSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: OnMyEnglishDatabase,
    private val parser: BuiltInDeckParser,
    @Dispatcher(AppDispatcher.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun seedIfNeeded() = withContext(ioDispatcher) {
        val deckDao = database.deckDao()
        for (assetId in BUILT_IN_ASSETS) {
            val deckId = "builtin-$assetId"
            if (deckDao.deckExists(deckId)) continue

            val text = readAsset(assetId) ?: continue
            val asset = when (val parsed = parser.parse(text)) {
                is com.luojiaping.onmyenglish.core.common.AppResult.Success -> parsed.value
                else -> continue
            }

            database.withTransaction {
                val now = System.currentTimeMillis()
                val wordDao = database.wordDao()
                val reviewDao = database.reviewDao()
                deckDao.upsertDeck(
                    DeckEntity(
                        id = deckId,
                        name = asset.name,
                        normalizedName = deckId,
                        description = "ECDICT ${asset.tag.uppercase(Locale.ROOT)}",
                        createdAtEpochMillis = now,
                        category = "BUILT_IN",
                        badge = asset.badge,
                        coverUri = null,
                    ),
                )
                var position = deckDao.nextPosition(deckId)

                for (word in asset.words) {
                    val normalized = word.w.trim().lowercase(Locale.ROOT)
                    val existing = wordDao.findByNormalizedHeadword(normalized)
                    val wordId = existing?.id ?: "w-${asset.id}-$normalized"
                    val senseId = "$wordId-s0"

                    wordDao.upsertWord(
                        WordEntity(
                            id = wordId,
                            headword = word.w,
                            normalizedHeadword = normalized,
                            phonetic = word.ph,
                            note = "",
                            source = WordSource.BUILT_IN.name,
                            createdAtEpochMillis = existing?.createdAtEpochMillis ?: now,
                        ),
                    )
                    if (existing == null) {
                        wordDao.deleteSenses(wordId)
                        wordDao.insertSenses(
                            listOf(
                                WordSenseEntity(
                                    id = senseId,
                                    wordId = wordId,
                                    partOfSpeech = word.pos,
                                    definition = word.tr,
                                    translation = word.tr.substringAfter(' ', ""),
                                    sortOrder = 0,
                                ),
                            ),
                        )
                        wordDao.insertTags(listOf(WordTagEntity(wordId, asset.tag)))
                        wordDao.deleteSearchEntry(wordId)
                        wordDao.insertSearchEntry(
                            WordFtsEntity(
                                wordId = wordId,
                                headword = word.w,
                                searchableText = word.tr,
                            ),
                        )
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
                    if (deckDao.insertDeckWord(
                            DeckWordEntity(
                                deckId = deckId,
                                wordId = wordId,
                                position = position,
                                addedAtEpochMillis = now,
                            ),
                        ) != -1L
                    ) {
                        position += 1
                    }
                }
            }
        }
    }

    private fun readAsset(assetId: String): String? {
        // AGP strips the .gz suffix when packaging compressed assets, so plain
        // .json is the canonical name; keep gzip fallback for local variants.
        val candidates = listOf("decks/$assetId.json", "decks/$assetId.json.gz")
        for (name in candidates) {
            val stream = runCatching { context.assets.open(name) }.getOrNull() ?: continue
            return runCatching {
                stream.use { input ->
                    if (name.endsWith(".gz")) {
                        GZIPInputStream(input).readBytes().toString(Charsets.UTF_8)
                    } else {
                        input.readBytes().toString(Charsets.UTF_8)
                    }
                }
            }.getOrNull()
        }
        return null
    }

    private companion object {
        val BUILT_IN_ASSETS = listOf("cet4", "cet6", "kaoyan")
    }
}
