package com.luojiaping.onmyenglish.core.data

import com.luojiaping.onmyenglish.core.database.DeckSummary
import com.luojiaping.onmyenglish.core.database.WordWithDetails
import com.luojiaping.onmyenglish.core.model.Deck
import com.luojiaping.onmyenglish.core.model.DeckCategory
import com.luojiaping.onmyenglish.core.model.PartOfSpeech
import com.luojiaping.onmyenglish.core.model.Word
import com.luojiaping.onmyenglish.core.model.WordExample
import com.luojiaping.onmyenglish.core.model.WordSense
import com.luojiaping.onmyenglish.core.model.WordSource

internal fun WordWithDetails.asModel(): Word = Word(
    id = word.id,
    headword = word.headword,
    phonetic = word.phonetic,
    senses = senses
        .sortedBy { it.sense.sortOrder }
        .map { relation ->
            WordSense(
                id = relation.sense.id,
                partOfSpeech = relation.sense.partOfSpeech.toPartOfSpeech(),
                definition = relation.sense.definition,
                translation = relation.sense.translation,
                examples = relation.examples
                    .sortedBy { it.sortOrder }
                    .map { example ->
                        WordExample(
                            id = example.id,
                            text = example.text,
                            translation = example.translation,
                        )
                    },
            )
        },
    note = word.note,
    tags = tags.mapTo(linkedSetOf()) { it.tag },
    source = runCatching { WordSource.valueOf(word.source) }.getOrDefault(WordSource.MANUAL),
    createdAtEpochMillis = word.createdAtEpochMillis,
)

internal fun DeckSummary.asModel(): Deck = Deck(
    id = id,
    name = name,
    description = description,
    wordCount = wordCount,
    createdAtEpochMillis = createdAtEpochMillis,
    category = runCatching { DeckCategory.valueOf(category) }
        .getOrDefault(DeckCategory.CUSTOM),
    badge = badge,
    coverUri = coverUri,
    learnedCount = learnedCount,
)

private fun String.toPartOfSpeech(): PartOfSpeech =
    runCatching { PartOfSpeech.valueOf(this) }.getOrDefault(PartOfSpeech.OTHER)
