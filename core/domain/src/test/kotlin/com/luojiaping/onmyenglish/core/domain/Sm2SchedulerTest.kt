package com.luojiaping.onmyenglish.core.domain

import com.luojiaping.onmyenglish.core.model.ReviewGrade
import com.luojiaping.onmyenglish.core.model.ReviewState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Sm2SchedulerTest {
    private val scheduler = Sm2Scheduler()
    private val now = 1_700_000_000_000L

    @Test
    fun `good answer follows initial one and six day intervals`() {
        val initial = state()

        val first = scheduler.schedule(initial, ReviewGrade.GOOD, now)
        val second = scheduler.schedule(first, ReviewGrade.GOOD, first.dueAtEpochMillis)

        assertEquals(1, first.intervalDays)
        assertEquals(1, first.repetitions)
        assertEquals(6, second.intervalDays)
        assertEquals(2, second.repetitions)
    }

    @Test
    fun `again resets repetitions and schedules tomorrow`() {
        val learned = state(repetitions = 5, intervalDays = 30)

        val result = scheduler.schedule(learned, ReviewGrade.AGAIN, now)

        assertEquals(0, result.repetitions)
        assertEquals(1, result.intervalDays)
        assertEquals(now + 86_400_000L, result.dueAtEpochMillis)
    }

    @Test
    fun `ease factor never falls below minimum`() {
        var current = state(easeFactor = 1.3)

        repeat(20) {
            current = scheduler.schedule(current, ReviewGrade.AGAIN, now + it)
        }

        assertTrue(current.easeFactor >= 1.3)
    }

    private fun state(
        repetitions: Int = 0,
        intervalDays: Int = 0,
        easeFactor: Double = 2.5,
    ) = ReviewState(
        wordId = "word-1",
        repetitions = repetitions,
        intervalDays = intervalDays,
        easeFactor = easeFactor,
        dueAtEpochMillis = now,
    )
}
