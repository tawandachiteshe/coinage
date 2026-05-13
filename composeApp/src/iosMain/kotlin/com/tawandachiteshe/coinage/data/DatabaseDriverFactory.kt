package com.tawandachiteshe.coinage.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.tawandachiteshe.coinage.db.ExpensifyDatabase

actual class DatabaseDriverFactory {
    actual fun create(): SqlDriver =
        NativeSqliteDriver(ExpensifyDatabase.Schema, "coinage.db")
}