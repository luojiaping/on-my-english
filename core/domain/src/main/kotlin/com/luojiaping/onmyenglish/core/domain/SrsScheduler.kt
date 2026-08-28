package com.luojiaping.onmyenglish.core.domain

import com.luojiaping.onmyenglish.core.model.ReviewGrade
import com.luojiaping.onmyenglish.core.model.ReviewState
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

interface SrsScheduler {
    fun schedule(
        current: ReviewState,
        grade: ReviewGrade,
        reviewedAtEpochMillis: Long,
    ): ReviewState
}

class Sm2Scheduler @Inject constructor() : SrsScheduler {
    override fun schedule(
        current: ReviewState,
        grade: ReviewGrade,
        reviewedAtEpochMillis: Long,
    ): ReviewState {
        val quality = when (grade) {
            ReviewGrade.AGAIN -> 1
            ReviewGrade.HARD -> 3
            ReviewGrade.GOOD -> 4
            ReviewGrade.EASY -> 5
        }

        val easeDelta = 0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02)
        val nextEase = max(MIN_EASE_FACTOR, current.easeFactor + easeDelta)

        val (nextRepetitions, nextInterval) = when (grade) {
            ReviewGrade.AGAIN -> 0 to 1
            ReviewGrade.HARD -> {
                val interval = max(1, ceil(max(1, current.intervalDays) * 1.2).toInt())
                max(1, current.repetitions) to interval
            }
            ReviewGrade.GOOD -> {
                val repetitions = current.repetitions + 1
                repetitions to intervalForSuccessfulReview(current, repetitions, nextEase)
            }
            ReviewGrade.EASY -> {
                val repetitions = current.repetitions + 1
                val base = intervalForSuccessfulReview(current, repetitions, nextEase)
                repetitions to max(4, (base * EASY_BONUS).roundToInt())
            }
        }

        return current.copy(
            repetitions = nextRepetitions,
            intervalDays = nextInterval,
            easeFactor = nextEase,
            dueAtEpochMillis = reviewedAtEpochMillis + nextInterval * MILLIS_PER_DAY,
            lastReviewedAtEpochMillis = reviewedAtEpochMillis,
        )
    }

    private fun intervalForSuccessfulReview(
        current: ReviewState,
        repetitions: Int,
        easeFactor: Double,
    ): Int = when (repetitions) {
        1 -> 1
        2 -> 6
        else -> max(1, (current.intervalDays * easeFactor).roundToInt())
    }

    private companion object {
        const val MIN_EASE_FACTOR = 1.3
        const val EASY_BONUS = 1.3
        const val MILLIS_PER_DAY = 86_400_000L
    }
}
