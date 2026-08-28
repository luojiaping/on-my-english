package com.luojiaping.onmyenglish.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Word(
    val id: String,
    val headword: String,
    val phonetic: String = "",
    val senses: List<WordSense> = emptyList(),
    val note: String = "",
    val tags: Set<String> = emptySet(),
    val source: WordSource = WordSource.MANUAL,
    val createdAtEpochMillis: Long,
)

@Serializable
data class WordSense(
    val id: String,
    val partOfSpeech: PartOfSpeech = PartOfSpeech.OTHER,
    val definition: String,
    val translation: String = "",
    val examples: List<WordExample> = emptyList(),
)

@Serializable
data class WordExample(
    val id: String,
    val text: String,
    val translation: String = "",
)

@Serializable
enum class PartOfSpeech {
    NOUN,
    VERB,
    ADJECTIVE,
    ADVERB,
    PRONOUN,
    PREPOSITION,
    CONJUNCTION,
    INTERJECTION,
    PHRASE,
    OTHER,
}

@Serializable
enum class WordSource {
    MANUAL,
    AI_VISION,
    AI_GENERATED,
    IMPORTED,
}

@Serializable
data class Deck(
    val id: String,
    val name: String,
    val description: String = "",
    val wordCount: Int = 0,
    val createdAtEpochMillis: Long,
)
