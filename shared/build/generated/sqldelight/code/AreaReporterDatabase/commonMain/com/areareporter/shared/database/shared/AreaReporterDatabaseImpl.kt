package com.areareporter.shared.database.shared

import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.areareporter.shared.database.AreaReporterDatabase
import com.areareporter.shared.database.ReportsQueries
import kotlin.Long
import kotlin.Unit
import kotlin.reflect.KClass

internal val KClass<AreaReporterDatabase>.schema: SqlSchema<QueryResult.Value<Unit>>
  get() = AreaReporterDatabaseImpl.Schema

internal fun KClass<AreaReporterDatabase>.newInstance(driver: SqlDriver): AreaReporterDatabase =
    AreaReporterDatabaseImpl(driver)

private class AreaReporterDatabaseImpl(
  driver: SqlDriver,
) : TransacterImpl(driver), AreaReporterDatabase {
  override val reportsQueries: ReportsQueries = ReportsQueries(driver)

  public object Schema : SqlSchema<QueryResult.Value<Unit>> {
    override val version: Long
      get() = 1

    override fun create(driver: SqlDriver): QueryResult.Value<Unit> {
      driver.execute(null, """
          |CREATE TABLE IF NOT EXISTS reports (
          |    id TEXT NOT NULL PRIMARY KEY,
          |    title TEXT NOT NULL,
          |    description TEXT NOT NULL,
          |    category TEXT NOT NULL,
          |    latitude REAL NOT NULL,
          |    longitude REAL NOT NULL,
          |    imagePath TEXT,
          |    status TEXT NOT NULL DEFAULT 'PENDING',
          |    createdAt INTEGER NOT NULL,
          |    retryCount INTEGER NOT NULL DEFAULT 0,
          |    lastSyncAttempt INTEGER
          |)
          """.trimMargin(), 0)
      return QueryResult.Unit
    }

    override fun migrate(
      driver: SqlDriver,
      oldVersion: Long,
      newVersion: Long,
      vararg callbacks: AfterVersion,
    ): QueryResult.Value<Unit> = QueryResult.Unit
  }
}
