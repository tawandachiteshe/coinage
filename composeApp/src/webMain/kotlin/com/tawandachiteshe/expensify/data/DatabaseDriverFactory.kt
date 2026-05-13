package com.tawandachiteshe.expensify.data

import app.cash.sqldelight.db.SqlDriver

actual class DatabaseDriverFactory {
    actual fun create(): SqlDriver =
        throw UnsupportedOperationException("SQLite is not supported on Web targets.")
}