package com.tawandachiteshe.coinage.data

import com.tawandachiteshe.coinage.db.ExpensifyDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class UserProfileRepository(db: ExpensifyDatabase) {

    private val q = db.userProfileQueries

    suspend fun getName(): String? = withContext(Dispatchers.IO) {
        q.getName().executeAsOneOrNull()
    }

    suspend fun saveName(name: String) = withContext(Dispatchers.IO) {
        q.upsertName(name, Clock.System.now().toEpochMilliseconds())
    }
}