package com.areareporter.shared.database

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.Double
import kotlin.Long
import kotlin.String

public class ReportsQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> selectAllReports(mapper: (
    id: String,
    title: String,
    description: String,
    category: String,
    latitude: Double,
    longitude: Double,
    imagePath: String?,
    status: String,
    createdAt: Long,
    retryCount: Long,
    lastSyncAttempt: Long?,
  ) -> T): Query<T> = Query(278_454_910, arrayOf("reports"), driver, "Reports.sq",
      "selectAllReports", "SELECT * FROM reports ORDER BY createdAt DESC") { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getDouble(4)!!,
      cursor.getDouble(5)!!,
      cursor.getString(6),
      cursor.getString(7)!!,
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getLong(10)
    )
  }

  public fun selectAllReports(): Query<Reports> = selectAllReports { id, title, description,
      category, latitude, longitude, imagePath, status, createdAt, retryCount, lastSyncAttempt ->
    Reports(
      id,
      title,
      description,
      category,
      latitude,
      longitude,
      imagePath,
      status,
      createdAt,
      retryCount,
      lastSyncAttempt
    )
  }

  public fun <T : Any> selectReportById(id: String, mapper: (
    id: String,
    title: String,
    description: String,
    category: String,
    latitude: Double,
    longitude: Double,
    imagePath: String?,
    status: String,
    createdAt: Long,
    retryCount: Long,
    lastSyncAttempt: Long?,
  ) -> T): Query<T> = SelectReportByIdQuery(id) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getDouble(4)!!,
      cursor.getDouble(5)!!,
      cursor.getString(6),
      cursor.getString(7)!!,
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getLong(10)
    )
  }

  public fun selectReportById(id: String): Query<Reports> = selectReportById(id) { id_, title,
      description, category, latitude, longitude, imagePath, status, createdAt, retryCount,
      lastSyncAttempt ->
    Reports(
      id_,
      title,
      description,
      category,
      latitude,
      longitude,
      imagePath,
      status,
      createdAt,
      retryCount,
      lastSyncAttempt
    )
  }

  public fun <T : Any> selectReportsByStatus(status: String, mapper: (
    id: String,
    title: String,
    description: String,
    category: String,
    latitude: Double,
    longitude: Double,
    imagePath: String?,
    status: String,
    createdAt: Long,
    retryCount: Long,
    lastSyncAttempt: Long?,
  ) -> T): Query<T> = SelectReportsByStatusQuery(status) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getDouble(4)!!,
      cursor.getDouble(5)!!,
      cursor.getString(6),
      cursor.getString(7)!!,
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getLong(10)
    )
  }

  public fun selectReportsByStatus(status: String): Query<Reports> = selectReportsByStatus(status) {
      id, title, description, category, latitude, longitude, imagePath, status_, createdAt,
      retryCount, lastSyncAttempt ->
    Reports(
      id,
      title,
      description,
      category,
      latitude,
      longitude,
      imagePath,
      status_,
      createdAt,
      retryCount,
      lastSyncAttempt
    )
  }

  public fun <T : Any> selectPendingReports(mapper: (
    id: String,
    title: String,
    description: String,
    category: String,
    latitude: Double,
    longitude: Double,
    imagePath: String?,
    status: String,
    createdAt: Long,
    retryCount: Long,
    lastSyncAttempt: Long?,
  ) -> T): Query<T> = Query(1_835_418_056, arrayOf("reports"), driver, "Reports.sq",
      "selectPendingReports",
      "SELECT * FROM reports WHERE status = 'PENDING' OR status = 'FAILED' ORDER BY createdAt ASC") {
      cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getDouble(4)!!,
      cursor.getDouble(5)!!,
      cursor.getString(6),
      cursor.getString(7)!!,
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getLong(10)
    )
  }

  public fun selectPendingReports(): Query<Reports> = selectPendingReports { id, title, description,
      category, latitude, longitude, imagePath, status, createdAt, retryCount, lastSyncAttempt ->
    Reports(
      id,
      title,
      description,
      category,
      latitude,
      longitude,
      imagePath,
      status,
      createdAt,
      retryCount,
      lastSyncAttempt
    )
  }

  public fun countPendingReports(): Query<Long> = Query(-1_691_738_861, arrayOf("reports"), driver,
      "Reports.sq", "countPendingReports",
      "SELECT COUNT(*) FROM reports WHERE status = 'PENDING' OR status = 'FAILED'") { cursor ->
    cursor.getLong(0)!!
  }

  public fun insertReport(
    id: String,
    title: String,
    description: String,
    category: String,
    latitude: Double,
    longitude: Double,
    imagePath: String?,
    status: String,
    createdAt: Long,
    retryCount: Long,
    lastSyncAttempt: Long?,
  ) {
    driver.execute(1_282_935_505, """
        |INSERT OR REPLACE INTO reports (
        |    id, title, description, category,
        |    latitude, longitude, imagePath,
        |    status, createdAt, retryCount, lastSyncAttempt
        |) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimMargin(), 11) {
          bindString(0, id)
          bindString(1, title)
          bindString(2, description)
          bindString(3, category)
          bindDouble(4, latitude)
          bindDouble(5, longitude)
          bindString(6, imagePath)
          bindString(7, status)
          bindLong(8, createdAt)
          bindLong(9, retryCount)
          bindLong(10, lastSyncAttempt)
        }
    notifyQueries(1_282_935_505) { emit ->
      emit("reports")
    }
  }

  public fun updateReportStatus(status: String, id: String) {
    driver.execute(-667_909_069, """UPDATE reports SET status = ? WHERE id = ?""", 2) {
          bindString(0, status)
          bindString(1, id)
        }
    notifyQueries(-667_909_069) { emit ->
      emit("reports")
    }
  }

  public fun updateReportSyncInfo(
    status: String,
    retryCount: Long,
    lastSyncAttempt: Long?,
    id: String,
  ) {
    driver.execute(-1_412_770_326, """
        |UPDATE reports
        |SET status = ?,
        |    retryCount = ?,
        |    lastSyncAttempt = ?
        |WHERE id = ?
        """.trimMargin(), 4) {
          bindString(0, status)
          bindLong(1, retryCount)
          bindLong(2, lastSyncAttempt)
          bindString(3, id)
        }
    notifyQueries(-1_412_770_326) { emit ->
      emit("reports")
    }
  }

  public fun deleteReport(id: String) {
    driver.execute(-231_190_461, """DELETE FROM reports WHERE id = ?""", 1) {
          bindString(0, id)
        }
    notifyQueries(-231_190_461) { emit ->
      emit("reports")
    }
  }

  private inner class SelectReportByIdQuery<out T : Any>(
    public val id: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("reports", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("reports", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-276_380_474, """SELECT * FROM reports WHERE id = ?""", mapper, 1) {
      bindString(0, id)
    }

    override fun toString(): String = "Reports.sq:selectReportById"
  }

  private inner class SelectReportsByStatusQuery<out T : Any>(
    public val status: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("reports", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("reports", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-2_140_857_240,
        """SELECT * FROM reports WHERE status = ? ORDER BY createdAt DESC""", mapper, 1) {
      bindString(0, status)
    }

    override fun toString(): String = "Reports.sq:selectReportsByStatus"
  }
}
