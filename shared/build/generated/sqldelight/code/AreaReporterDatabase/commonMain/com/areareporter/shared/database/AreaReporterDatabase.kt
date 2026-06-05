package com.areareporter.shared.database

import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.areareporter.shared.database.shared.newInstance
import com.areareporter.shared.database.shared.schema
import kotlin.Unit

public interface AreaReporterDatabase : Transacter {
  public val reportsQueries: ReportsQueries

  public companion object {
    public val Schema: SqlSchema<QueryResult.Value<Unit>>
      get() = AreaReporterDatabase::class.schema

    public operator fun invoke(driver: SqlDriver): AreaReporterDatabase =
        AreaReporterDatabase::class.newInstance(driver)
  }
}
