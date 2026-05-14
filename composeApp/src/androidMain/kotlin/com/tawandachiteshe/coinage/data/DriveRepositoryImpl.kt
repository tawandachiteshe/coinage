package com.tawandachiteshe.coinage.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tawandachiteshe.coinage.data.backup.BackupData
import com.tawandachiteshe.coinage.domain.DataError
import com.tawandachiteshe.coinage.domain.Result
import com.tawandachiteshe.coinage.domain.repository.BackupFileInfo
import com.tawandachiteshe.coinage.domain.repository.DriveRepository
import com.tawandachiteshe.coinage.domain.repository.GoogleAuthRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

class DriveRepositoryImpl(
    private val authRepo: GoogleAuthRepository,
    private val dataStore: DataStore<Preferences>,
) : DriveRepository {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun backup(data: BackupData): Result<Unit, DataError.Network> {
        val token = authRepo.getValidAccessToken() ?: return Result.Error(DataError.Network.UNAUTHORIZED)
        return try {
            val body = json.encodeToString(BackupData.serializer(), data)
            val fileId = getCachedFileId() ?: findOrCreateFile(token)
                ?: return Result.Error(DataError.Network.SERVER_ERROR)
            client.patch("https://www.googleapis.com/upload/drive/v3/files/$fileId") {
                parameter("uploadType", "media")
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(body)
            }
            Result.Success(Unit)
        } catch (_: Exception) {
            Result.Error(DataError.Network.UNKNOWN)
        }
    }

    override suspend fun restore(): Result<BackupData, DataError.Network> {
        val token = authRepo.getValidAccessToken() ?: return Result.Error(DataError.Network.UNAUTHORIZED)
        return try {
            val fileId = getCachedFileId() ?: findFile(token)
                ?: return Result.Error(DataError.Network.NOT_FOUND)
            val raw = client.get("https://www.googleapis.com/drive/v3/files/$fileId") {
                parameter("alt", "media")
                header("Authorization", "Bearer $token")
            }.body<String>()
            Result.Success(json.decodeFromString(raw))
        } catch (_: Exception) {
            Result.Error(DataError.Network.UNKNOWN)
        }
    }

    override suspend fun lastBackupInfo(): BackupFileInfo? {
        val token = authRepo.getValidAccessToken() ?: return null
        return try {
            val fileId = getCachedFileId() ?: findFile(token) ?: return null
            val meta = client.get("https://www.googleapis.com/drive/v3/files/$fileId") {
                parameter("fields", "size,modifiedTime")
                header("Authorization", "Bearer $token")
            }.body<String>()
            val obj = json.parseToJsonElement(meta).jsonObject
            val size = obj["size"]?.jsonPrimitive?.long ?: 0L
            val modified = obj["modifiedTime"]?.jsonPrimitive?.content
                ?.let { Instant.parse(it).toEpochMilliseconds() } ?: 0L
            BackupFileInfo(sizeBytes = size, modifiedAt = modified)
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun findFile(token: String): String? {
        return try {
            val res = client.get("https://www.googleapis.com/drive/v3/files") {
                parameter("spaces", "appDataFolder")
                parameter("q", "name = 'coinage_backup.json'")
                parameter("fields", "files(id)")
                header("Authorization", "Bearer $token")
            }.body<String>()
            val id = json.parseToJsonElement(res).jsonObject["files"]
                ?.jsonArray?.firstOrNull()?.jsonObject?.get("id")?.jsonPrimitive?.content
            if (id != null) cacheFileId(id)
            id
        } catch (_: Exception) { null }
    }

    private suspend fun findOrCreateFile(token: String): String? {
        findFile(token)?.let { return it }
        return try {
            val res = client.post("https://www.googleapis.com/drive/v3/files") {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody("""{"name":"coinage_backup.json","parents":["appDataFolder"]}""")
            }.body<String>()
            val id = json.parseToJsonElement(res).jsonObject["id"]?.jsonPrimitive?.content
            if (id != null) cacheFileId(id)
            id
        } catch (_: Exception) { null }
    }

    private suspend fun getCachedFileId() =
        dataStore.data.map { it[DRIVE_FILE_ID_KEY] }.firstOrNull()

    private suspend fun cacheFileId(id: String) =
        dataStore.edit { it[DRIVE_FILE_ID_KEY] = id }

    companion object {
        private val DRIVE_FILE_ID_KEY = stringPreferencesKey("drive_backup_file_id")
    }
}