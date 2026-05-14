package com.tawandachiteshe.coinage.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import com.tawandachiteshe.coinage.db.ExpensifyDatabase
import com.tawandachiteshe.coinage.db.Iou
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class IouRepository(
    db: ExpensifyDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val q = db.iouQueries

    fun getAll(): Flow<List<Iou>> =
        q.selectAll().asFlow().mapToList(ioDispatcher)

    fun getTotalOutstandingFlow(): Flow<Double> =
        q.totalOutstanding().asFlow().mapToOne(ioDispatcher)

    suspend fun insert(
        id: String,
        personName: String,
        amount: Double,
        notes: String?,
        categoryId: String?,
        lentAt: Long,
        dueDate: Long?,
        createdAt: Long,
    ) = withContext(ioDispatcher) {
        q.insert(id, personName, amount, notes, categoryId, lentAt, dueDate, createdAt)
    }

    suspend fun recordPayment(id: String, payment: Double) =
        withContext(ioDispatcher) { q.recordPayment(payment, id) }

    suspend fun delete(id: String) = withContext(ioDispatcher) { q.delete(id) }

    suspend fun getAllOnce(): List<Iou> = withContext(ioDispatcher) {
        q.selectAll().executeAsList()
    }

    suspend fun deleteAll() = withContext(ioDispatcher) { q.deleteAll() }
}