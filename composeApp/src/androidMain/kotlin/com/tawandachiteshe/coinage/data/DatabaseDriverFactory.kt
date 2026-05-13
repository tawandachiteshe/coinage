package com.tawandachiteshe.coinage.data

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.tawandachiteshe.coinage.db.ExpensifyDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun create(): SqlDriver =
        AndroidSqliteDriver(ExpensifyDatabase.Schema, context, "coinage.db")
}