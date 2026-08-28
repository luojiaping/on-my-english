package com.luojiaping.onmyenglish.core.model

import kotlinx.serialization.Serializable

@Serializable
data class ReviewState(
    val wordId: String,
    val repetitions: Int = 0,
    val intervalDays: Int = 0,
    val easeFactor: Double = DEFAULT_EASE_FACTOR,
    val dueAtEpochMillis: Long,
    val lastReviewedAtEpochMillis: Long? = null,
) {
    companion object {
        const val DEFAULT_EASE_FACTOR = 2.5
    }
}

@Serializable
enum class ReviewGrade {
    AGAIN,
    HARD,
    GOOD,
    EASY,
}

@Serializable
data class ReviewLog(
    val id: String,
    val wordId: String,
    val grade: ReviewGrade,
    val reviewedAtEpochMillis: Long,
    val responseTimeMillis: Long,
)

@Serializable
data class StudySession(
    val id: String,
    val startedAtEpochMillis: Long,
    val completedAtEpochMillis: Long? = null,
    val reviewedCount: Int = 0,
    val correctCount: Int = 0,
)
