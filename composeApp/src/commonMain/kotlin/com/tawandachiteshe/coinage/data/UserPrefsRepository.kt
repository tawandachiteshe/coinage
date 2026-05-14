package com.tawandachiteshe.coinage.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.tawandachiteshe.coinage.db.ExpensifyDatabase
import com.tawandachiteshe.coinage.db.UserPrefs
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class UserPrefsRepository(
    db: ExpensifyDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val q = db.userPrefsQueries

    init {
        // For fresh installs the migration hasn't run, so seed the row here.
        // INSERT OR IGNORE means existing rows (from the migration) are left untouched.
        @OptIn(kotlin.time.ExperimentalTime::class)
        q.seed(Clock.System.now().toEpochMilliseconds())
    }

    fun getFlow(): Flow<UserPrefs?> =
        q.get().asFlow().mapToOneOrNull(ioDispatcher)

    suspend fun get(): UserPrefs? = withContext(ioDispatcher) {
        q.get().executeAsOneOrNull()
    }

    suspend fun setBaseCurrency(code: String) = withContext(ioDispatcher) {
        q.setBaseCurrency(code)
    }

    suspend fun setWeekStartDay(day: Long) = withContext(ioDispatcher) {
        q.setWeekStartDay(day)
    }

    suspend fun setRoundUp(enabled: Boolean) = withContext(ioDispatcher) {
        q.setRoundUp(if (enabled) 1L else 0L)
    }

    suspend fun setBiometric(enabled: Boolean) = withContext(ioDispatcher) {
        q.setBiometric(if (enabled) 1L else 0L)
    }

    suspend fun setMonthlyBudget(budget: Double) = withContext(ioDispatcher) {
        q.setMonthlyBudget(budget)
    }

    suspend fun setSavingsRate(pct: Long) = withContext(ioDispatcher) {
        q.setSavingsRate(pct)
    }

    suspend fun setOnboardingDone() = withContext(ioDispatcher) {
        q.setOnboardingDone()
    }

    suspend fun deleteAll() = withContext(ioDispatcher) { q.deleteAll() }
}