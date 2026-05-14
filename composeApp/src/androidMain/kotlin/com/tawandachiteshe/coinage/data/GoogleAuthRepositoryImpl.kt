package com.tawandachiteshe.coinage.data

import android.content.Context
import android.content.Intent
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import co.touchlab.kermit.Logger
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.tawandachiteshe.coinage.domain.repository.GoogleAuthRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

private val log = Logger.withTag("GoogleAuth")

class GoogleAuthRepositoryImpl(
    private val context: Context,
    private val dataStore: DataStore<Preferences>,
    private val clientId: String,
) : GoogleAuthRepository {

    private val authClient = Identity.getAuthorizationClient(context)

    override fun isConnected(): Boolean = runBlocking {
        val token = dataStore.data.map { it[ACCESS_TOKEN_KEY] }.firstOrNull()
        log.d { "isConnected check → token ${if (token != null) "present (${token.take(10)}…)" else "null"}" }
        token != null
    }

    override suspend fun getConnectedEmail(): String? =
        dataStore.data.map { it[EMAIL_KEY] }.firstOrNull()

    override suspend fun getValidAccessToken(): String? =
        dataStore.data.map { it[ACCESS_TOKEN_KEY] }.firstOrNull()

    suspend fun requestAuthorization(): AuthorizationResult {
        log.d { "requestAuthorization: building request with DRIVE + SHEETS scopes" }
        val request = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(SHEETS_SCOPE), Scope(DRIVE_SCOPE)))
            .build()
        return try {
            val result = authClient.authorize(request).await()
            log.d {
                "requestAuthorization result → " +
                "hasResolution=${result.hasResolution()} " +
                "accessToken=${result.accessToken?.take(10)?.plus("…") ?: "null"} " +
                "pendingIntent=${result.pendingIntent}"
            }
            result
        } catch (e: Exception) {
            log.e(e) { "requestAuthorization THREW: ${e.message}" }
            throw e
        }
    }

    suspend fun handleAuthorizationResult(data: Intent?) {
        log.d { "handleAuthorizationResult: data=${data?.extras?.keySet()?.joinToString()}" }
        if (data != null) {
            try {
                val authResult = authClient.getAuthorizationResultFromIntent(data)
                val token = authResult.accessToken
                log.d { "getAuthorizationResultFromIntent → token=${token?.take(10)?.plus("…") ?: "null"}" }
                if (token != null) {
                    dataStore.edit { prefs -> prefs[ACCESS_TOKEN_KEY] = token }
                    log.i { "token saved from intent result ✓" }
                    return
                }
            } catch (e: Exception) {
                log.w(e) { "getAuthorizationResultFromIntent threw: ${e.message}" }
            }
        } else {
            log.d { "handleAuthorizationResult: data intent is null, skipping intent parse" }
        }
        log.d { "falling through to trySilentAuth" }
        trySilentAuth()
    }

    suspend fun trySilentAuth() {
        log.d { "trySilentAuth: calling authorize() silently" }
        try {
            val request = AuthorizationRequest.builder()
                .setRequestedScopes(listOf(Scope(SHEETS_SCOPE), Scope(DRIVE_SCOPE)))
                .build()
            val result = authClient.authorize(request).await()
            log.d {
                "trySilentAuth result → " +
                "hasResolution=${result.hasResolution()} " +
                "accessToken=${result.accessToken?.take(10)?.plus("…") ?: "null"}"
            }
            val token = result.accessToken ?: run {
                log.w { "trySilentAuth: accessToken still null — consent may not have been recorded yet" }
                return
            }
            dataStore.edit { prefs -> prefs[ACCESS_TOKEN_KEY] = token }
            log.i { "trySilentAuth: token saved ✓" }
        } catch (e: Exception) {
            log.e(e) { "trySilentAuth THREW: ${e.message}" }
        }
    }

    suspend fun saveToken(token: String, email: String?) {
        log.d { "saveToken: email=$email token=${token.take(10)}…" }
        dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN_KEY] = token
            if (email != null) prefs[EMAIL_KEY] = email
        }
    }

    override suspend fun signOut() {
        log.d { "signOut: clearing token and email from DataStore" }
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