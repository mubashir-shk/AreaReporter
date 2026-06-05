package com.areareporter.shared.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

/**
 * Android implementation of the database driver factory.
 * Uses the AndroidSqliteDriver from SQLDelight.
 */
actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = AreaReporterDatabase.Schema,
            context = context,
            name = "area_reporter.db"
        )
    }
}
