package com.tawandachiteshe.coinage.data

import android.content.Context
import android.content.Intent
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.tawandachiteshe.coinage.domain.repository.GoogleAuthRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class GoogleAuthRepositoryImpl(
    private val context: Context,
    private val dataStore: DataStore<Preferences>,
    private val clientId: String,
) : GoogleAuthRepository {

    private val authClient = Identity.getAuthorizationClient(context)

    private val tokenClient = HttpClient(Android) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    override fun isConnected(): Boolean = runBlocking {
        dataStore.data.map { it[ACCESS_TOKEN_KEY] }.firstOrNull() != null
    }

    override suspend fun getConnectedEmail(): String? =
        dataStore.data.map { it[EMAIL_KEY] }.firstOrNull()

    override suspend fun getValidAccessToken(): String? {
        val expiry = dataStore.data.map { it[TOKEN_EXPIRY_KEY] ?: 0L }.firstOrNull() ?: 0L
        val now = Clock.System.now().toEpochMilliseconds()
        if (now >= expiry - 60_000L) {
            val refresh = dataStore.data.map { it[REFRESH_TOKEN_KEY] }.firstOrNull()
                ?: return dataStore.data.map { it[ACCESS_TOKEN_KEY] }.firstOrNull()
            return try {
                val resp = tokenClient.submitForm(
                    url = TOKEN_ENDPOINT,
                    formParameters = Parameters.build {
                        append("grant_type", "refresh_token")
                        append("client_id", clientId)
                        append("refresh_token", refresh)
                    }
                ).body<TokenResponse>()
                val expiresAt = Clock.System.now().toEpochMilliseconds() + resp.expiresIn * 1000L
                dataStore.edit { prefs ->
                    prefs[ACCESS_TOKEN_KEY] = resp.accessToken
                    prefs[TOKEN_EXPIRY_KEY] = expiresAt
                }
                resp.accessToken
            } catch (_: Exception) {
                dataStore.data.map { it[ACCESS_TOKEN_KEY] }.firstOrNull()
            }
        }
        return dataStore.data.map { it[ACCESS_TOKEN_KEY] }.firstOrNull()
    }

    suspend fun requestAuthorization(): AuthorizationResult {
        val request = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(SHEETS_SCOPE), Scope(DRIVE_SCOPE)))
            .requestOfflineAccess(clientId)
            .build()
        return authClient.authorize(request).await()
    }

    suspend fun handleAuthorizationResult(data: Intent?) {
        val result = authClient.getAuthorizationResultFromIntent(data)
        // Offline flow: result.serverAuthCode contains the auth code; accessToken is null
        val code = result.serverAuthCode
        if (code != null) {
            try {
                val resp = tokenClient.submitForm(
                    url = TOKEN_ENDPOINT,
                    formParameters = Parameters.build {
                        append("grant_type", "authorization_code")
                        append("client_id", clientId)
                        append("code", code)
                        append("redirect_uri", "")
                    }
                ).body<TokenResponse>()
                val expiresAt = Clock.System.now().toEpochMilliseconds() + resp.expiresIn * 1000L
                dataStore.edit { prefs ->
                    prefs[ACCESS_TOKEN_KEY] = resp.accessToken
                    prefs[TOKEN_EXPIRY_KEY] = expiresAt
                    resp.refreshToken?.let { prefs[REFRESH_TOKEN_KEY] = it }
                    resp.email?.let { prefs[EMAIL_KEY] = it }
                }
            } catch (_: Exception) { /* token exchange failed */ }
            return
        }
        // Online flow fallback (no offline access)
        val token = result.accessToken ?: return
        dataStore.edit { prefs -> prefs[ACCESS_TOKEN_KEY] = token }
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
            prefs.remove(REFRESH_TOKEN_KEY)
            prefs.remove(TOKEN_EXPIRY_KEY)
            prefs.remove(EMAIL_KEY)
        }
    }

    @Serializable
    private data class TokenResponse(
        @SerialName("access_token")  val accessToken: String,
        @SerialName("refresh_token") val refreshToken: String? = null,
        @SerialName("expires_in")    val expiresIn: Long,
        @SerialName("id_token")      val email: String? = null,
    )

    companion object {
        const val SHEETS_SCOPE = "https://www.googleapis.com/auth/spreadsheets"
        const val DRIVE_SCOPE  = "https://www.googleapis.com/auth/drive.appdata"
        private const val TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token"
        val ACCESS_TOKEN_KEY  = stringPreferencesKey("google_access_token")
        val REFRESH_TOKEN_KEY = stringPreferencesKey("google_refresh_token")
        val TOKEN_EXPIRY_KEY  = longPreferencesKey("google_token_expiry")
        val EMAIL_KEY         = stringPreferencesKey("google_email")
    }
}