package com.tawandachiteshe.coinage.data

interface TokenStorage {
    suspend fun saveAccessToken(token: String)
    suspend fun getAccessToken(): String?
    suspend fun clearTokens()
}