package com.tawandachiteshe.coinage.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.tawandachiteshe.coinage.db.ExpensifyDatabase
import com.tawandachiteshe.coinage.db.Goal
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class GoalRepository(
    db: ExpensifyDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    private val q = db.goalQueries

    fun getAll(): Flow<List<Goal>> =
        q.selectAll().asFlow().mapToList(ioDispatcher)

    fun getActive(): Flow<List<Goal>> =
        q.selectActive().asFlow().mapToList(ioDispatcher)

    suspend fun getProgressPercent(id: String): Double =
        withContext(ioDispatcher) {
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
    ) = withContext(ioDispatcher) {
        q.insert(id, name, icon, targetAmount, savedAmount, deadline, isCompleted, createdAt)
    }

    suspend fun addToSaved(id: String, amount: Double) =
        withContext(ioDispatcher) { q.addToSaved(amount, id) }

    suspend fun delete(id: String) = withContext(ioDispatcher) { q.delete(id) }

    suspend fun getAllOnce(): List<Goal> = withContext(ioDispatcher) {
        q.selectAll().executeAsList()
    }

    suspend fun deleteAll() = withContext(ioDispatcher) { q.deleteAll() }
}