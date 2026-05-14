package com.tawandachiteshe.coinage.data

import android.content.Context
import android.content.Intent
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.tawandachiteshe.coinage.domain.repository.GoogleAuthRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class GoogleAuthRepositoryImpl(
    private val context: Context,
    private val dataStore: DataStore<Preferences>,
    private val clientId: String,
) : GoogleAuthRepository {

    private val authClient = Identity.getAuthorizationClient(context)

    override fun isConnected(): Boolean = runBlocking {
        dataStore.data.map { it[ACCESS_TOKEN_KEY] }.firstOrNull() != null
    }

    override suspend fun getConnectedEmail(): String? =
        dataStore.data.map { it[EMAIL_KEY] }.firstOrNull()

    override suspend fun getValidAccessToken(): String? =
        dataStore.data.map { it[ACCESS_TOKEN_KEY] }.firstOrNull()

    // Requests Drive + Sheets scopes. Returns a result the caller inspects:
    //   hasResolution() → launch the PendingIntent (consent screen)
    //   accessToken != null → token already available, no UI needed
    suspend fun requestAuthorization(): AuthorizationResult {
        val request = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(SHEETS_SCOPE), Scope(DRIVE_SCOPE)))
            .build()
        return authClient.authorize(request).await()
    }

    // Called after the consent-screen Activity returns RESULT_OK.
    // Saves the access token (and email if present in the result).
    suspend fun handleAuthorizationResult(data: Intent?) {
        val result = authClient.getAuthorizationResultFromIntent(data)
        val token = result.accessToken ?: return
        dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN_KEY] = token
        }
    }

    suspend fun saveToken(token: String, email: String?) {
        dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN_KEY] = token
            if (email != null) prefs[EMAIL_KEY] = email
        }
    }

    override suspend fun signOut() {
        dataStore.edit { prefs ->
            prefs.remove(ACCESS_TOKEN_KEY)
            prefs.remove(EMAIL_KEY)
        }
    }

    companion object {
        const val SHEETS_SCOPE = "https://www.googleapis.com/auth/spreadsheets"
        const val DRIVE_SCOPE  = "https://www.googleapis.com/auth/drive.appdata"
        val ACCESS_TOKEN_KEY   = stringPreferencesKey("google_access_token")
        val EMAIL_KEY          = stringPreferencesKey("google_email")
    }
}