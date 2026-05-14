package com.tawandachiteshe.coinage.domain.repository

import com.tawandachiteshe.coinage.data.backup.BackupData
import com.tawandachiteshe.coinage.domain.DataError
import com.tawandachiteshe.coinage.domain.Result

interface SheetsRepository {
    suspend fun sync(data: BackupData): Result<String, DataError.Network>  // returns sheet URL
}
