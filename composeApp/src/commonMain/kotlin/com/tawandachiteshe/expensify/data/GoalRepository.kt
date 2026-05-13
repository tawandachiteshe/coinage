package com.tawandachiteshe.expensify.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.tawandachiteshe.expensify.db.ExpensifyDatabase
import com.tawandachiteshe.expensify.db.Goal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class GoalRepository(db: ExpensifyDatabase) {

    private val q = db.goalQueries

    fun getAll(): Flow<List<Goal>> =
        q.selectAll().asFlow().mapToList(Dispatchers.IO)

    fun getActive(): Flow<List<Goal>> =
        q.selectActive().asFlow().mapToList(Dispatchers.IO)

    suspend fun getProgressPercent(id: String): Double =
        withContext(Dispatchers.IO) {
            q.progressPercent(id).executeAsOne().percent ?: 0.0
        }

    suspend fun insert(
        id: String,
        name: String,
        icon: String,
        targetAmount: Double,
        savedAmount: Double,
        deadline: Long?,
        isCompleted: Long,
        createdAt: Long,
    ) = withContext(Dispatchers.IO) {
        q.insert(id, name, icon, targetAmount, savedAmount, deadline, isCompleted, createdAt)
    }

    suspend fun addToSaved(id: String, amount: Double) =
        withContext(Dispatchers.IO) { q.addToSaved(amount, id) }

    suspend fun delete(id: String) = withContext(Dispatchers.IO) { q.delete(id) }
}