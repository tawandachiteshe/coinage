# Google Drive Backup & Sheets Export

Two distinct systems — do not conflate them.

| System | Purpose | Direction | Scope |
|--------|---------|-----------|-------|
| Drive `appdata` | Full backup / restore | Bidirectional (write app → read back) | All tables as JSON |
| Google Sheets | Human-readable export / reports | One-way (app → Sheets) | Transactions + summary formulas |

---

## 1. OAuth — Current State & Gap

`GoogleAuthRepositoryImpl` already requests both scopes:

```
drive.appdata  →  hidden app folder in Drive (user cannot see it, cannot accidentally delete it)
spreadsheets   →  create / update Sheets
```

**The gap:** `requestOfflineAccess(clientId)` is called, which means the server returns an authorization **code** (not an access token). The current `handleAuthorizationResult` reads `result.accessToken` — that field is `null` when offline access was requested. The authorization code needs to be exchanged for an `access_token` + `refresh_token` pair via a POST to `https://oauth2.googleapis.com/token`.

### Fix — token exchange

In `handleAuthorizationResult`:

```kotlin
suspend fun handleAuthorizationResult(data: Intent?) {
    val result = authClient.getAuthorizationResultFromIntent(data)

    // Offline flow: exchange server auth code → access + refresh tokens
    val code = result.serverAuthCode
    if (code != null) {
        val tokens = exchangeAuthCode(code)           // Ktor POST to token endpoint
        dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN_KEY]  = tokens.accessToken
            prefs[REFRESH_TOKEN_KEY] = tokens.refreshToken
            prefs[TOKEN_EXPIRY_KEY]  = tokens.expiresAt   // epochMillis
            if (tokens.email != null) prefs[EMAIL_KEY] = tokens.email
        }
        return
    }

    // Fallback: online flow (no offline access), token returned directly
    val token = result.accessToken ?: return
    dataStore.edit { prefs -> prefs[ACCESS_TOKEN_KEY] = token }
}
```

`exchangeAuthCode` does a Ktor POST to `https://oauth2.googleapis.com/token` with:

```
grant_type=authorization_code
code=<serverAuthCode>
client_id=<clientId>
redirect_uri=<registeredUri>    // must match Google Console; use "" for mobile
```

Response: `{ access_token, refresh_token, expires_in, token_type }`.

### `getValidAccessToken` — add refresh logic

```kotlin
override suspend fun getValidAccessToken(): String? {
    val expiry = dataStore.data.map { it[TOKEN_EXPIRY_KEY] ?: 0L }.firstOrNull() ?: 0L
    val now    = Clock.System.now().toEpochMilliseconds()
    // Refresh 60 s before expiry
    if (now >= expiry - 60_000) {
        val refreshToken = dataStore.data.map { it[REFRESH_TOKEN_KEY] }.firstOrNull() ?: return null
        val newTokens = refreshAccessToken(refreshToken)   // Ktor POST
        dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN_KEY] = newTokens.accessToken
            prefs[TOKEN_EXPIRY_KEY] = newTokens.expiresAt
        }
        return newTokens.accessToken
    }
    return dataStore.data.map { it[ACCESS_TOKEN_KEY] }.firstOrNull()
}
```

Add DataStore keys:

```kotlin
private val REFRESH_TOKEN_KEY = stringPreferencesKey("google_refresh_token")
private val TOKEN_EXPIRY_KEY  = longPreferencesKey("google_token_expiry")
```

---

## 2. BackupData JSON Model

One file in Drive: `coinage_backup.json`. Full overwrite on every backup.

```kotlin
// commonMain/data/backup/BackupData.kt
@Serializable
data class BackupData(
    val version: Int = 1,
    val exportedAt: Long,                      // epoch ms
    val userProfile: BackupUserProfile?,
    val transactions: List<BackupTransaction>,
    val categories: List<BackupCategory>,
    val debts: List<BackupDebt>,
    val goals: List<BackupGoal>,
    val currencies: List<BackupCurrency>,
)

@Serializable data class BackupUserProfile(val name: String, val joinedAt: Long)

@Serializable data class BackupTransaction(
    val id: String, val amount: Double, val type: String,
    val categoryId: String, val merchant: String, val notes: String?,
    val currencyCode: String, val date: Long, val createdAt: Long,
)

@Serializable data class BackupCategory(
    val id: String, val name: String, val icon: String, val colorHex: String,
    val type: String, val budgetLimit: Double, val isDefault: Long, val isActive: Long,
)

@Serializable data class BackupDebt(
    val id: String, val creditorName: String, val debtType: String,
    val principal: Double, val currentBalance: Double, val interestRate: Double,
    val minimumPayment: Double, val dueDate: Long?, val createdAt: Long,
)

@Serializable data class BackupGoal(
    val id: String, val name: String, val icon: String,
    val targetAmount: Double, val savedAmount: Double,
    val deadline: Long?, val isCompleted: Long, val createdAt: Long,
)

@Serializable data class BackupCurrency(
    val code: String, val name: String, val symbol: String,
    val rateToUsd: Double, val isBase: Long,
)
```

---

## 3. DriveRepository

### Interface — commonMain

```kotlin
// commonMain/domain/repository/DriveRepository.kt
interface DriveRepository {
    suspend fun backup(data: BackupData): Result<Unit, DataError.Network>
    suspend fun restore(): Result<BackupData, DataError.Network>
    suspend fun lastBackupInfo(): Result<BackupFileInfo?, DataError.Network>
}

data class BackupFileInfo(val sizeBytes: Long, val modifiedAt: Long)
```

### Android impl — androidMain, Ktor + Drive API v3

**File name / ID management**: Drive `appdata` supports multiple files. We search for `coinage_backup.json` by name on first restore/backup, cache the file ID in DataStore.

```kotlin
// androidMain/data/DriveRepositoryImpl.kt
class DriveRepositoryImpl(
    private val authRepo: GoogleAuthRepository,
    private val httpClient: HttpClient,
    private val dataStore: DataStore<Preferences>,
) : DriveRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun backup(data: BackupData): Result<Unit, DataError.Network> {
        val token = authRepo.getValidAccessToken() ?: return Err(DataError.Network.UNAUTHORIZED)
        val body  = json.encodeToString(data)
        val fileId = getCachedFileId() ?: findOrCreateFile(token) ?: return Err(DataError.Network.SERVER_ERROR)

        // PATCH multipart: metadata + media body
        httpClient.patch("https://www.googleapis.com/upload/drive/v3/files/$fileId") {
            parameter("uploadType", "media")
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        return Ok(Unit)
    }

    override suspend fun restore(): Result<BackupData, DataError.Network> {
        val token  = authRepo.getValidAccessToken() ?: return Err(DataError.Network.UNAUTHORIZED)
        val fileId = getCachedFileId() ?: findFile(token) ?: return Err(DataError.Network.NOT_FOUND)
        val raw    = httpClient.get("https://www.googleapis.com/drive/v3/files/$fileId") {
            parameter("alt", "media")
            header("Authorization", "Bearer $token")
        }.body<String>()
        return Ok(json.decodeFromString(raw))
    }

    override suspend fun lastBackupInfo(): Result<BackupFileInfo?, DataError.Network> {
        val token  = authRepo.getValidAccessToken() ?: return Err(DataError.Network.UNAUTHORIZED)
        val fileId = getCachedFileId() ?: findFile(token) ?: return Ok(null)
        val meta   = httpClient.get("https://www.googleapis.com/drive/v3/files/$fileId") {
            parameter("fields", "size,modifiedTime")
            header("Authorization", "Bearer $token")
        }.body<JsonObject>()
        return Ok(BackupFileInfo(
            sizeBytes  = meta["size"]?.jsonPrimitive?.long ?: 0L,
            modifiedAt = Instant.parse(meta["modifiedTime"]!!.jsonPrimitive.content)
                             .toEpochMilliseconds(),
        ))
    }

    private suspend fun findFile(token: String): String? {
        val res = httpClient.get("https://www.googleapis.com/drive/v3/files") {
            parameter("spaces", "appDataFolder")
            parameter("q", "name = 'coinage_backup.json'")
            parameter("fields", "files(id)")
            header("Authorization", "Bearer $token")
        }.body<JsonObject>()
        val id = res["files"]?.jsonArray?.firstOrNull()?.jsonObject?.get("id")?.jsonPrimitive?.content
        if (id != null) cacheFileId(id)
        return id
    }

    private suspend fun findOrCreateFile(token: String): String? {
        findFile(token)?.let { return it }
        val res = httpClient.post("https://www.googleapis.com/drive/v3/files") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"name":"coinage_backup.json","parents":["appDataFolder"]}""")
        }.body<JsonObject>()
        val id = res["id"]?.jsonPrimitive?.content ?: return null
        cacheFileId(id)
        return id
    }

    private suspend fun getCachedFileId() =
        dataStore.data.map { it[DRIVE_FILE_ID_KEY] }.firstOrNull()

    private suspend fun cacheFileId(id: String) =
        dataStore.edit { it[DRIVE_FILE_ID_KEY] = id }

    companion object {
        private val DRIVE_FILE_ID_KEY = stringPreferencesKey("drive_backup_file_id")
    }
}
```

---

## 4. BackupOrchestrator — commonMain

Keeps the ViewModel thin. Builds `BackupData` from all repos, calls `DriveRepository.backup`.

```kotlin
// commonMain/domain/BackupOrchestrator.kt
class BackupOrchestrator(
    private val txRepo: TransactionRepository,
    private val catRepo: CategoryRepository,
    private val debtRepo: DebtRepository,
    private val goalRepo: GoalRepository,
    private val currencyRepo: CurrencyRepository,
    private val profileRepo: UserProfileRepository,
    private val driveRepo: DriveRepository,
) {
    suspend fun backup(): Result<Unit, DataError.Network> {
        val data = BackupData(
            exportedAt   = Clock.System.now().toEpochMilliseconds(),
            userProfile  = profileRepo.getProfile()?.let { BackupUserProfile(it.name, it.joinedAt) },
            transactions = txRepo.getAllOnce().map { it.toBackup() },
            categories   = catRepo.getAllOnce().map { it.toBackup() },
            debts        = debtRepo.getAllOnce().map { it.toBackup() },
            goals        = goalRepo.getAllOnce().map { it.toBackup() },
            currencies   = currencyRepo.getAllOnce().map { it.toBackup() },
        )
        return driveRepo.backup(data)
    }

    suspend fun restore(): Result<Unit, DataError.Network> {
        val result = driveRepo.restore()
        if (result is Err) return result
        val data = (result as Ok).value
        // Full wipe + re-insert — user confirmed before calling
        txRepo.deleteAll()
        catRepo.deleteAll()
        debtRepo.deleteAll()
        goalRepo.deleteAll()
        currencyRepo.deleteAll()
        data.transactions.forEach { txRepo.insert(it) }
        data.categories.forEach   { catRepo.insert(it) }
        data.debts.forEach        { debtRepo.insert(it) }
        data.goals.forEach        { goalRepo.insert(it) }
        data.currencies.forEach   { currencyRepo.insert(it) }
        data.userProfile?.let     { profileRepo.saveName(it.name, it.joinedAt) }
        return Ok(Unit)
    }
}
```

`getAllOnce()` = non-flow, single-shot reads — add to each repository:

```kotlin
// TransactionRepository
suspend fun getAllOnce(): List<SelectAll> = withContext(ioDispatcher) {
    q.selectAll().executeAsList()
}
```

---

## 5. SettingsViewModel additions

```kotlin
// SettingsUiState additions
data class SettingsUiState(
    // ... existing fields ...
    val isGoogleConnected: Boolean = false,
    val googleEmail: String? = null,
    val isSyncing: Boolean = false,
    val lastBackupLabel: String? = null,   // "May 13 · 3.2 KB" or null
    val backupError: String? = null,
)

sealed interface SettingsAction {
    // ... existing actions ...
    data object OnBackupNow        : SettingsAction
    data object OnRestoreFromDrive : SettingsAction   // shows confirm dialog first
    data object OnConfirmRestore   : SettingsAction   // called after user confirms
    data object OnSyncToSheets     : SettingsAction
    data object OnDismissBackupError : SettingsAction
}
```

In `SettingsViewModel.handleAction`:

```kotlin
is OnBackupNow -> {
    _state.update { it.copy(isSyncing = true, backupError = null) }
    viewModelScope.launch {
        when (val r = backupOrchestrator.backup()) {
            is Ok  -> refreshBackupInfo()
            is Err -> _state.update { it.copy(backupError = r.error.toMessage()) }
        }
        _state.update { it.copy(isSyncing = false) }
    }
}
```

Restore requires a confirmation dialog before calling `OnConfirmRestore` — the state should include a `showRestoreConfirm: Boolean` flag toggled by `OnRestoreFromDrive`.

---

## 6. SheetsRepository (one-way export)

### Interface — commonMain

```kotlin
interface SheetsRepository {
    suspend fun sync(data: BackupData): Result<String, DataError.Network>  // returns sheet URL
}
```

### Android impl — Sheets API v4

Sheet layout: 4 tabs — `Transactions`, `Debts`, `Goals`, `Summary`.

```kotlin
class SheetsRepositoryImpl(
    private val authRepo: GoogleAuthRepository,
    private val httpClient: HttpClient,
    private val dataStore: DataStore<Preferences>,
) : SheetsRepository {

    override suspend fun sync(data: BackupData): Result<String, DataError.Network> {
        val token    = authRepo.getValidAccessToken() ?: return Err(DataError.Network.UNAUTHORIZED)
        val sheetId  = getCachedSheetId() ?: createSheet(token) ?: return Err(DataError.Network.SERVER_ERROR)

        val txRows = listOf(listOf("Date","Merchant","Type","Category","Amount","Currency","Notes")) +
            data.transactions.map { listOf(it.date.toDateLabel(), it.merchant, it.type,
                it.categoryId, it.amount, it.currencyCode, it.notes ?: "") }

        val debtRows = listOf(listOf("Creditor","Type","Principal","Balance","Rate","Min Payment","Due")) +
            data.debts.map { listOf(it.creditorName, it.debtType, it.principal,
                it.currentBalance, it.interestRate, it.minimumPayment, it.dueDate?.toDateLabel() ?: "") }

        val goalRows = listOf(listOf("Goal","Target","Saved","Deadline","Done")) +
            data.goals.map { listOf(it.name, it.targetAmount, it.savedAmount,
                it.deadline?.toDateLabel() ?: "", if (it.isCompleted == 1L) "Yes" else "No") }

        val summaryRows = listOf(
            listOf("Metric", "Value"),
            listOf("Total transactions", data.transactions.size),
            listOf("Total debts", data.debts.sumOf { it.currentBalance }),
            listOf("Total goals", data.goals.size),
            listOf("Exported at", data.exportedAt.toDateLabel()),
        )

        httpClient.post("https://sheets.googleapis.com/v4/spreadsheets/$sheetId/values:batchUpdate") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(buildBatchUpdate(
                "Transactions!A1" to txRows,
                "Debts!A1"        to debtRows,
                "Goals!A1"        to goalRows,
                "Summary!A1"      to summaryRows,
            ))
        }

        return Ok("https://docs.google.com/spreadsheets/d/$sheetId")
    }

    private suspend fun createSheet(token: String): String? {
        val body = """
            {"properties":{"title":"Coinage Export"},
             "sheets":[
               {"properties":{"title":"Transactions"}},
               {"properties":{"title":"Debts"}},
               {"properties":{"title":"Goals"}},
               {"properties":{"title":"Summary"}}
             ]}
        """.trimIndent()
        val res = httpClient.post("https://sheets.googleapis.com/v4/spreadsheets") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body<JsonObject>()
        val id = res["spreadsheetId"]?.jsonPrimitive?.content ?: return null
        dataStore.edit { it[SHEET_ID_KEY] = id }
        return id
    }

    private suspend fun getCachedSheetId() =
        dataStore.data.map { it[SHEET_ID_KEY] }.firstOrNull()

    companion object {
        private val SHEET_ID_KEY = stringPreferencesKey("sheets_spreadsheet_id")
    }
}
```

---

## 7. Restore confirmation flow

Restore is destructive (full wipe + re-insert). The UI must show a modal before calling `OnConfirmRestore`.

Suggested approach in `GoogleConnectSection.kt`:

```kotlin
if (state.showRestoreConfirm) {
    AlertDialog(
        onDismissRequest = { onAction(SettingsAction.DismissRestoreConfirm) },
        title = { Text("Replace all data?") },
        text  = { Text("This will delete everything on this device and replace it with your Drive backup. This cannot be undone.") },
        confirmButton = {
            TextButton(onClick = { onAction(SettingsAction.OnConfirmRestore) }) {
                Text("Replace", color = TrackerColors.Cherry)
            }
        },
        dismissButton = {
            TextButton(onClick = { onAction(SettingsAction.DismissRestoreConfirm) }) {
                Text("Cancel")
            }
        }
    )
}
```

---

## 8. Koin wiring

```kotlin
// androidModule
single { DriveRepositoryImpl(get(), get(), get()) as DriveRepository }
single { SheetsRepositoryImpl(get(), get(), get()) as SheetsRepository }
single { BackupOrchestrator(get(), get(), get(), get(), get(), get(), get()) }
```

---

## 9. Implementation order

1. **Token exchange + refresh** — fix `handleAuthorizationResult` and `getValidAccessToken`; add DataStore keys. Nothing else works without a valid token.
2. **`BackupData` model** — `@Serializable` data classes in `commonMain/data/backup/`.
3. **`getAllOnce()` on each repo** — single-shot suspend reads for the orchestrator.
4. **`DriveRepository` interface + `DriveRepositoryImpl`** — backup and restore. Test manually: backup → uninstall → reinstall → restore.
5. **`BackupOrchestrator`** — coordinates the full read + write cycle.
6. **`SettingsViewModel` wiring** — add `isSyncing`, `lastBackupLabel`, `backupError`; handle new actions.
7. **Restore confirmation dialog** in `GoogleConnectSection.kt`.
8. **`SheetsRepository`** — one-way export; lower priority than backup/restore.

---

## 10. Error surface

`DataError.Network` already covers the relevant cases:

| HTTP status | Maps to |
|-------------|---------|
| 401 | `UNAUTHORIZED` — token expired and refresh failed; prompt re-auth |
| 404 | `NOT_FOUND` — no backup file exists yet |
| 403 | `FORBIDDEN` — scope not granted; prompt re-auth |
| 5xx | `SERVER_ERROR` — Drive / Sheets outage |
| No network | `NO_INTERNET` |

The UI shows `backupError` as a snackbar or inline error label below the sync button — not a blocking dialog.