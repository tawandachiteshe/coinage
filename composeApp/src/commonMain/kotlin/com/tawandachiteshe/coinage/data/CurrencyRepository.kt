package com.tawandachiteshe.coinage.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.tawandachiteshe.coinage.db.Currency
import com.tawandachiteshe.coinage.db.ExpensifyDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CurrencyRepository(db: ExpensifyDatabase) {

    private val q = db.currencyQueries

    init {
        seedDefaults()
    }

    fun getAll(): Flow<List<Currency>> =
        q.selectAll().asFlow().mapToList(Dispatchers.IO)

    fun getBase(): Flow<Currency?> =
        q.selectBase().asFlow().mapToOneOrNull(Dispatchers.IO)

    suspend fun setBase(code: String) = withContext(Dispatchers.IO) {
        q.clearBase()
        q.setBase(code)
    }

    suspend fun updateRate(code: String, rateToUsd: Double) = withContext(Dispatchers.IO) {
        q.updateRate(rateToUsd, code)
    }

    private fun seedDefaults() {
        DEFAULT_CURRENCIES.forEach { c ->
            q.insert(c.code, c.name, c.symbol, c.rateToUsd, c.isBase)
        }
    }

    companion object {
        data class CurrencySeed(
            val code: String,
            val name: String,
            val symbol: String,
            val rateToUsd: Double,
            val isBase: Long,
        )

        val DEFAULT_CURRENCIES = listOf(
            CurrencySeed("USD", "US Dollar",          "$",   1.0,    1L),
            CurrencySeed("EUR", "Euro",                "€",   0.93,   0L),
            CurrencySeed("GBP", "British Pound",       "£",   0.79,   0L),
            CurrencySeed("ZAR", "South African Rand",  "R",   18.6,   0L),
            CurrencySeed("CAD", "Canadian Dollar",     "C$",  1.36,   0L),
            CurrencySeed("AUD", "Australian Dollar",   "A$",  1.54,   0L),
            CurrencySeed("JPY", "Japanese Yen",        "¥",   149.0,  0L),
            CurrencySeed("CHF", "Swiss Franc",         "Fr",  0.91,   0L),
            CurrencySeed("CNY", "Chinese Yuan",        "¥",   7.2,    0L),
            CurrencySeed("NGN", "Nigerian Naira",      "₦",   1500.0, 0L),
            CurrencySeed("KES", "Kenyan Shilling",     "KSh", 128.0,  0L),
            CurrencySeed("GHS", "Ghanaian Cedi",       "₵",   12.5,   0L),
            CurrencySeed("INR", "Indian Rupee",        "₹",   83.0,   0L),
            CurrencySeed("BRL", "Brazilian Real",      "R$",  5.0,    0L),
            CurrencySeed("MXN", "Mexican Peso",        "MX$", 17.0,   0L),
        )
    }
}