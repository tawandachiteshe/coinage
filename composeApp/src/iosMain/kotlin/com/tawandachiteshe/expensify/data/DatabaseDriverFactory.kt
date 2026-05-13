package com.tawandachiteshe.expensify.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.tawandachiteshe.expensify.db.ExpensifyDatabase

actual class DatabaseDriverFactory {
    actual fun create(): SqlDriver =
        NativeSqliteDriver(ExpensifyDatabase.Schema, "expensify.db")
}