package com.tawandachiteshe.coinage.feature.profile

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BadgeLogicTest {

    @Test
    fun emptySetHasNoStreak() {
        assertFalse(hasConsecutiveDayStreak(emptySet(), 7))
    }

    @Test
    fun fewerDaysThanRequiredHasNoStreak() {
        val days = setOf(0L, 1L, 2L, 3L, 4L, 5L) // 6 consecutive days
        assertFalse(hasConsecutiveDayStreak(days, 7))
    }

    @Test
    fun exactlyNConsecutiveDaysUnlocks() {
        val days = setOf(0L, 1L, 2L, 3L, 4L, 5L, 6L) // 7 consecutive
        assertTrue(hasConsecutiveDayStreak(days, 7))
    }

    @Test
    fun moreThanNConsecutiveDaysUnlocks() {
        val days = setOf(10L, 11L, 12L, 13L, 14L, 15L, 16L, 17L)
        assertTrue(hasConsecutiveDayStreak(days, 7))
    }

    @Test
    fun gapInDaysBreaksStreak() {
        // 6 consecutive, gap, then 6 more — never reaches 7
        val days = setOf(0L, 1L, 2L, 3L, 4L, 5L, 7L, 8L, 9L, 10L, 11L, 12L)
        assertFalse(hasConsecutiveDayStreak(days, 7))
    }

    @Test
    fun streakAfterGapUnlocks() {
        // gap at day 3, then 7 consecutive starting at day 5
        val days = setOf(0L, 1L, 2L, 5L, 6L, 7L, 8L, 9L, 10L, 11L)
        assertTrue(hasConsecutiveDayStreak(days, 7))
    }

    @Test
    fun duplicateDaysAreCollapsedBySet() {
        // Same 3 days repeated — only 3 unique days, no streak of 7
        val days = setOf(0L, 0L, 1L, 1L, 2L, 2L) // set deduplicates to {0,1,2}
        assertFalse(hasConsecutiveDayStreak(days, 7))
    }

    @Test
    fun outOfOrderDaysAreHandled() {
        val days = setOf(6L, 0L, 4L, 2L, 5L, 1L, 3L) // unsorted 0–6
        assertTrue(hasConsecutiveDayStreak(days, 7))
    }

    @Test
    fun singleDayDoesNotMeetMultiDayStreak() {
        assertFalse(hasConsecutiveDayStreak(setOf(42L), 2))
    }

    @Test
    fun singleDayMeetsSingleDayStreak() {
        assertTrue(hasConsecutiveDayStreak(setOf(42L), 1))
    }
}