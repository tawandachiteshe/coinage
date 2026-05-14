package com.tawandachiteshe.coinage.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import com.tawandachiteshe.coinage.db.ExpensifyDatabase
import com.tawandachiteshe.coinage.db.BiggestExpenseInRange
import com.tawandachiteshe.coinage.db.SelectAll
import com.tawandachiteshe.coinage.db.SelectByDateRange
import com.tawandachiteshe.coinage.db.SelectByType
import com.tawandachiteshe.coinage.db.TopMerchantInRange
import com.tawandachiteshe.coinage.db.TotalByCategoryAndDateRange
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class TransactionRepository(
    db: ExpensifyDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    private val q = db.transactionQueries

    fun getAll(): Flow<List<SelectAll>> =
        q.selectAll().asFlow().mapToList(ioDispatcher)

    fun getByDateRange(startMs: Long, endMs: Long): Flow<List<SelectByDateRange>> =
        q.selectByDateRange(startMs, endMs).asFlow().mapToList(ioDispatcher)

    fun getByType(type: String): Flow<List<SelectByType>> =
        q.selectByType(type).asFlow().mapToList(ioDispatcher)

    suspend fun getTotalByTypeAndDateRange(type: String, startMs: Long, endMs: Long): Double =
        withContext(ioDispatcher) {
            q.totalByTypeAndDateRange(type, startMs, endMs).executeAsOne()
        }

    // Reactive: re-emits income+expense totals whenever the Transaction table changes.
    fun getTotalsFlow(startMs: Long, endMs: Long): Flow<Pair<Double, Double>> =
        q.totalsByDateRange(startMs, endMs)
            .asFlow()
            .mapToOne(ioDispatcher)
            .map { it.income_total to it.expense_total }

    suspend fun getCategoryTotals(startMs: Long, endMs: Long): List<TotalByCategoryAndDateRange> =
        withContext(ioDispatcher) {
            q.totalByCategoryAndDateRange(startMs, endMs).executeAsList()
        }

    suspend fun getSpendingByCategory(startMs: Long, endMs: Long): Map<String, Double> =
        withContext(ioDispatcher) {
            q.totalByCategoryAndDateRange(startMs, endMs)
                .executeAsList()
                .associate { it.category_id to it.total }
        }

    suspend fun getBiggestExpense(startMs: Long, endMs: Long): BiggestExpenseInRange? =
        withContext(ioDispatcher) {
            q.biggestExpenseInRange(startMs, endMs).executeAsOneOrNull()
        }

    suspend fun getTopMerchant(startMs: Long, endMs: Long): TopMerchantInRange? =
        withContext(ioDispatcher) {
            q.topMerchantInRange(startMs, endMs).executeAsOneOrNull()
        }

    suspend fun countInRange(startMs: Long, endMs: Long): Long =
        withContext(ioDispatcher) {
            q.countInRange(startMs, endMs).executeAsOne()
        }

    suspend fun insert(
        id: String,
        amount: Double,
        type: String,
        categoryId: String,
        merchant: String,
        notes: String?,
        currencyCode: String = "USD",
        date: Long,
        createdAt: Long,
    ) = withContext(ioDispatcher) {
        q.insert(id, amount, type, categoryId, merchant, notes, currencyCode, date, createdAt)
    }

    suspend fun delete(id: String) = withContext(ioDispatcher) { q.delete(id) }

    suspend fun getAllOnce(): List<SelectAll> = withContext(ioDispatcher) {
        q.selectAll().executeAsList()
    }

    suspend fun deleteAll() = withContext(ioDispatcher) { q.deleteAll() }
}