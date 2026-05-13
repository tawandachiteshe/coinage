package com.tawandachiteshe.coinage.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.tawandachiteshe.coinage.db.ExpensifyDatabase

actual class DatabaseDriverFactory {
    actual fun create(): SqlDriver =
        JdbcSqliteDriver("jdbc:sqlite:expensify.db").also {
            ExpensifyDatabase.Schema.create(it)
        }
}