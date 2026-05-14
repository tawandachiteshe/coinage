package com.tawandachiteshe.coinage.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
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

    private val client = HttpClient(Android) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun sync(data: BackupData): Result<String, DataError.Network> {
        val token = authRepo.getValidAccessToken() ?: return Result.Error(DataError.Network.UNAUTHORIZED)
        return try {
            val sheetId = getCachedSheetId() ?: createSheet(token)
                ?: return Result.Error(DataError.Network.SERVER_ERROR)
            val updates = buildBatchUpdateBody(data)
            client.post("https://sheets.googleapis.com/v4/spreadsheets/$sheetId/values:batchUpdate") {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(updates)
            }
            Result.Success("https://docs.google.com/spreadsheets/d/$sheetId")
        } catch (_: Exception) {
            Result.Error(DataError.Network.UNKNOWN)
        }
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