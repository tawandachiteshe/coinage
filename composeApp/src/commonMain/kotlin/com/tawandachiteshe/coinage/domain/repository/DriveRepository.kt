package com.tawandachiteshe.coinage.domain.repository

import com.tawandachiteshe.coinage.data.backup.BackupData
import com.tawandachiteshe.coinage.domain.DataError
import com.tawandachiteshe.coinage.domain.Result

data class BackupFileInfo(val sizeBytes: Long, val modifiedAt: Long)

interface DriveRepository {
    suspend fun backup(data: BackupData): Result<Unit, DataError.Network>
    suspend fun restore(): Result<BackupData, DataError.Network>
    suspend fun lastBackupInfo(): BackupFileInfo?
}