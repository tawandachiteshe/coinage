package com.tawandachiteshe.coinage.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class DatastoreTokenStorage(
    private val dataStore: DataStore<Preferences>
) : TokenStorage {

    private val accessTokenKey = stringPreferencesKey("access_token")

    override suspend fun saveAccessToken(token: String) {
        dataStore.edit { it[accessTokenKey] = token }
    }

    override suspend fun getAccessToken(): String? {
        return dataStore.data.map { it[accessTokenKey] }.firstOrNull()
    }

    override suspend fun clearTokens() {
        dataStore.edit { it.remove(accessTokenKey) }
    }
}