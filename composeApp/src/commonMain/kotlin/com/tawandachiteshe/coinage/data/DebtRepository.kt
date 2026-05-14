package com.tawandachiteshe.coinage.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.tawandachiteshe.coinage.db.Debt
import com.tawandachiteshe.coinage.db.ExpensifyDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class DebtRepository(
    db: ExpensifyDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    private val q = db.debtQueries

    fun getAll(): Flow<List<Debt>> =
        q.selectAll().asFlow().mapToList(ioDispatcher)

    fun getSnowballOrder(): Flow<List<Debt>> =
        q.snowballOrder().asFlow().mapToList(ioDispatcher)

    fun getAvalancheOrder(): Flow<List<Debt>> =
        q.avalancheOrder().asFlow().mapToList(ioDispatcher)

    suspend fun getSnowballList(): List<Debt> =
        withContext(ioDispatcher) { q.snowballOrder().executeAsList() }

    suspend fun getAvalancheList(): List<Debt> =
        withContext(ioDispatcher) { q.avalancheOrder().executeAsList() }

    suspend fun getTotalOwed(): Double =
        withContext(ioDispatcher) {
            q.totalOwed().executeAsOne()
        }

    suspend fun insert(
        id: String,
        creditorName: String,
        debtType: String,
        principal: Double,
        currentBalance: Double,
        interestRate: Double,
        minimumPayment: Double,
        dueDate: Long?,
        createdAt: Long,
    ) = withContext(ioDispatcher) {
        q.insert(id, creditorName, debtType, principal, currentBalance, interestRate, minimumPayment, dueDate, createdAt)
    }

    suspend fun updateBalance(id: String, newBalance: Double) =
        withContext(ioDispatcher) { q.updateBalance(newBalance, id) }

    suspend fun updateDueDate(id: String, dueDate: Long?) =
        withContext(ioDispatcher) { q.updateDueDate(dueDate, id) }

    suspend fun delete(id: String) = withContext(ioDispatcher) { q.delete(id) }

    suspend fun getAllOnce(): List<Debt> = withContext(ioDispatcher) {
        q.selectAll().executeAsList()
    }

    suspend fun deleteAll() = withContext(ioDispatcher) { q.deleteAll() }
}