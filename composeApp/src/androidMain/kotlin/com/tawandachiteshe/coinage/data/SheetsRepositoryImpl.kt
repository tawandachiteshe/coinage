package com.tawandachiteshe.coinage.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import co.touchlab.kermit.Logger
import com.tawandachiteshe.coinage.data.backup.BackupData
import com.tawandachiteshe.coinage.domain.DataError
import com.tawandachiteshe.coinage.domain.Result
import com.tawandachiteshe.coinage.domain.repository.GoogleAuthRepository
import com.tawandachiteshe.coinage.domain.repository.SheetsRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class SheetsRepositoryImpl(
    private val authRepo: GoogleAuthRepository,
    private val dataStore: DataStore<Preferences>,
) : SheetsRepository {

    private val log = Logger.withTag("SheetsSync")

    private val client = HttpClient(Android) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun sync(data: BackupData): Result<String, DataError.Network> {
        val token = authRepo.getValidAccessToken() ?: return Result.Error(DataError.Network.UNAUTHORIZED)
        return try {
            val sheetId = resolveSheetId(token)
                ?: return Result.Error(DataError.Network.SERVER_ERROR)
            val updates = buildBatchUpdateBody(data)
            log.d { "Syncing ${data.transactions.size} transactions to sheet $sheetId" }
            val response = client.post("https://sheets.googleapis.com/v4/spreadsheets/$sheetId/values:batchUpdate") {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(updates)
            }
            val body = response.body<String>()
            log.d { "batchUpdate status=${response.status.value}" }
            if (!response.status.isSuccess()) {
                log.e { "batchUpdate failed: $body" }
                return when (response.status.value) {
                    401, 403 -> Result.Error(DataError.Network.UNAUTHORIZED)
                    404 -> {
                        dataStore.edit { it.remove(SHEET_ID_KEY) }
                        Result.Error(DataError.Network.SERVER_ERROR)
                    }
                    else -> Result.Error(DataError.Network.SERVER_ERROR)
                }
            }
            Result.Success("https://docs.google.com/spreadsheets/d/$sheetId")
        } catch (e: Exception) {
            log.e(e) { "sync exception" }
            Result.Error(DataError.Network.UNKNOWN)
        }
    }

    // Returns cached sheet ID, or creates a new one if the cached ID no longer resolves.
    private suspend fun resolveSheetId(token: String): String? {
        val cached = getCachedSheetId() ?: return createSheet(token)
        // Verify the sheet still exists with a lightweight metadata probe.
        return try {
            val probe = client.post(
                "https://sheets.googleapis.com/v4/spreadsheets/$cached/values:batchClear"
            ) {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody("""{"ranges":["Transactions!A1:Z","Debts!A1:Z","Goals!A1:Z","Summary!A1:Z"]}""")
            }
            probe.body<String>() // consume
            if (probe.status.value == 404) {
                log.w { "Cached sheet $cached not found — creating new one" }
                dataStore.edit { it.remove(SHEET_ID_KEY) }
                createSheet(token)
            } else {
                cached
            }
        } catch (_: Exception) { cached }
    }

    private suspend fun createSheet(token: String): String? {
        return try {
            val res = client.post("https://sheets.googleapis.com/v4/spreadsheets") {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody("""{"properties":{"title":"Coinage Export"},"sheets":[{"properties":{"title":"Transactions"}},{"properties":{"title":"Debts"}},{"properties":{"title":"Goals"}},{"properties":{"title":"Summary"}}]}""")
            }.body<String>()
            val id = json.parseToJsonElement(res).jsonObject["spreadsheetId"]?.jsonPrimitive?.content
            if (id != null) dataStore.edit { it[SHEET_ID_KEY] = id }
            id
        } catch (_: Exception) { null }
    }

    private suspend fun getCachedSheetId() =
        dataStore.data.map { it[SHEET_ID_KEY] }.firstOrNull()

    private fun buildBatchUpdateBody(data: BackupData): String {
        val txHeader = listOf("Date", "Merchant", "Type", "Category", "Amount", "Currency", "Notes")
        val txRows = data.transactions.map { t ->
            listOf(t.date.toString(), t.merchant, t.type, t.categoryId,
                t.amount.toString(), t.currencyCode, t.notes ?: "")
        }
        val debtHeader = listOf("Creditor", "Type", "Principal", "Balance", "Rate", "Min Payment", "Due Date")
        val debtRows = data.debts.map { d ->
            listOf(d.creditorName, d.debtType, d.principal.toString(), d.currentBalance.toString(),
                d.interestRate.toString(), d.minimumPayment.toString(), d.dueDate?.toString() ?: "")
        }
        val goalHeader = listOf("Goal", "Target", "Saved", "Deadline", "Done")
        val goalRows = data.goals.map { g ->
            listOf(g.name, g.targetAmount.toString(), g.savedAmount.toString(),
                g.deadline?.toString() ?: "", if (g.isCompleted == 1L) "Yes" else "No")
        }
        val summaryRows = listOf(
            listOf("Metric", "Value"),
            listOf("Transactions", data.transactions.size.toString()),
            listOf("Total debts", data.debts.sumOf { it.currentBalance }.toString()),
            listOf("Goals", data.goals.size.toString()),
            listOf("Exported at", data.exportedAt.toString()),
        )
        fun rangeUpdate(range: String, rows: List<List<String>>) =
            """{"range":"$range","majorDimension":"ROWS","values":${rowsToJson(rows)}}"""
        val updates = listOf(
            rangeUpdate("Transactions!A1", listOf(txHeader) + txRows),
            rangeUpdate("Debts!A1", listOf(debtHeader) + debtRows),
            rangeUpdate("Goals!A1", listOf(goalHeader) + goalRows),
            rangeUpdate("Summary!A1", summaryRows),
        )
        return """{"valueInputOption":"RAW","data":[${updates.joinToString(",")}]}"""
    }

    private fun rowsToJson(rows: List<List<String>>): String =
        "[${rows.joinToString(",") { row -> "[${row.joinToString(",") { cell -> "\"${cell.replace("\"", "\\\"")}\"" }}]" }}]"

    companion object {
        private val SHEET_ID_KEY = stringPreferencesKey("sheets_spreadsheet_id")
    }
}