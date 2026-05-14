package com.tawandachiteshe.coinage.data

import com.tawandachiteshe.coinage.db.ExpensifyDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class UserProfileRepository(
    db: ExpensifyDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    private val q = db.userProfileQueries

    suspend fun getName(): String? = withContext(ioDispatcher) {
        q.getName().executeAsOneOrNull()
    }

    suspend fun saveName(name: String) = withContext(ioDispatcher) {
        q.upsertName(name, Clock.System.now().toEpochMilliseconds())
    }

    suspend fun getJoinedAt(): Long? = withContext(ioDispatcher) {
        q.getJoinedAt().executeAsOneOrNull()
    }

    suspend fun restoreProfile(name: String, joinedAt: Long) = withContext(ioDispatcher) {
        q.upsertName(name, joinedAt)
    }
}