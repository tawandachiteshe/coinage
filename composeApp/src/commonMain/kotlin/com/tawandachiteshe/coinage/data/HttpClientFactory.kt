package com.tawandachiteshe.coinage.data

import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createHttpClient(tokenStorage: TokenStorage): HttpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
        })
    }
    install(Logging) {
        level = LogLevel.ALL
        logger = object : Logger {
            override fun log(message: String) {
                co.touchlab.kermit.Logger.d("HTTP") { message }
            }
        }
    }
    install(Auth) {
        bearer {
            loadTokens {
                val access = tokenStorage.getAccessToken() ?: return@loadTokens null
                BearerTokens(accessToken = access, refreshToken = "")
            }
            refreshTokens {
                val access = tokenStorage.getAccessToken() ?: return@refreshTokens null
                BearerTokens(accessToken = access, refreshToken = "")
            }
        }
    }
}