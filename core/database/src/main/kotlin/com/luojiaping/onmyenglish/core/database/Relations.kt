package com.luojiaping.onmyenglish.core.database

import androidx.room.Embedded
import androidx.room.Relation

data class WordSenseWithExamples(
    @Embedded val sense: WordSenseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "senseId",
    )
    val examples: List<WordExampleEntity>,
)

data class WordWithDetails(
    @Embedded val word: WordEntity,
    @Relation(
        entity = WordSenseEntity::class,
        parentColumn = "id",
        entityColumn = "wordId",
    )
    val senses: List<WordSenseWithExamples>,
    @Relation(
        parentColumn = "id",
        entityColumn = "wordId",
    )
    val tags: List<WordTagEntity>,
)

data class DeckSummary(
    val id: String,
    val name: String,
    val description: String,
    val createdAtEpochMillis: Long,
    val wordCount: Int,
)
