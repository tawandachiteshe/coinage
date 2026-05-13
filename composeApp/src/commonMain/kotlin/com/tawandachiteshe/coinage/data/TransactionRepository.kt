package com.tawandachiteshe.coinage.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.tawandachiteshe.coinage.db.ExpensifyDatabase
import com.tawandachiteshe.coinage.db.SelectAll
import com.tawandachiteshe.coinage.db.SelectByDateRange
import com.tawandachiteshe.coinage.db.SelectByType
import com.tawandachiteshe.coinage.db.TotalByCategoryAndDateRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TransactionRepository(db: ExpensifyDatabase) {

    private val q = db.transactionQueries

    fun getAll(): Flow<List<SelectAll>> =
        q.selectAll().asFlow().mapToList(Dispatchers.IO)

    fun getByDateRange(startMs: Long, endMs: Long): Flow<List<SelectByDateRange>> =
        q.selectByDateRange(startMs, endMs).asFlow().mapToList(Dispatchers.IO)

    fun getByType(type: String): Flow<List<SelectByType>> =
        q.selectByType(type).asFlow().mapToList(Dispatchers.IO)

    suspend fun getTotalByTypeAndDateRange(type: String, startMs: Long, endMs: Long): Double =
        withContext(Dispatchers.IO) {
            q.totalByTypeAndDateRange(type, startMs, endMs).executeAsOne()
        }

    suspend fun getCategoryTotals(startMs: Long, endMs: Long): List<TotalByCategoryAndDateRange> =
        withContext(Dispatchers.IO) {
            q.totalByCategoryAndDateRange(startMs, endMs).executeAsList()
        }

    suspend fun insert(
        id: String,
        amount: Double,
        type: String,
        categoryId: String,
        merchant: String,
        notes: String?,
        date: Long,
        createdAt: Long,
    ) = withContext(Dispatchers.IO) {
        q.insert(id, amount, type, categoryId, merchant, notes, date, createdAt)
    }

    suspend fun delete(id: String) = withContext(Dispatchers.IO) { q.delete(id) }
}