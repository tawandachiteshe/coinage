package com.tawandachiteshe.coinage.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.tawandachiteshe.coinage.db.Debt
import com.tawandachiteshe.coinage.db.ExpensifyDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class DebtRepository(db: ExpensifyDatabase) {

    private val q = db.debtQueries

    fun getAll(): Flow<List<Debt>> =
        q.selectAll().asFlow().mapToList(Dispatchers.IO)

    fun getSnowballOrder(): Flow<List<Debt>> =
        q.snowballOrder().asFlow().mapToList(Dispatchers.IO)

    fun getAvalancheOrder(): Flow<List<Debt>> =
        q.avalancheOrder().asFlow().mapToList(Dispatchers.IO)

    suspend fun getTotalOwed(): Double =
        withContext(Dispatchers.IO) {
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
    ) = withContext(Dispatchers.IO) {
        q.insert(id, creditorName, debtType, principal, currentBalance, interestRate, minimumPayment, dueDate, createdAt)
    }

    suspend fun updateBalance(id: String, newBalance: Double) =
        withContext(Dispatchers.IO) { q.updateBalance(newBalance, id) }

    suspend fun updateDueDate(id: String, dueDate: Long?) =
        withContext(Dispatchers.IO) { q.updateDueDate(dueDate, id) }

    suspend fun delete(id: String) = withContext(Dispatchers.IO) { q.delete(id) }
}