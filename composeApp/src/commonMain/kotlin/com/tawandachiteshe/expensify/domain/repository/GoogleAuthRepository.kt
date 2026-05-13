package com.tawandachiteshe.expensify.domain.repository

interface GoogleAuthRepository {
    fun isConnected(): Boolean
    suspend fun getConnectedEmail(): String?
    suspend fun getValidAccessToken(): String?
    suspend fun signOut()
}