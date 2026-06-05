package com.areareporter.shared.database

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.areareporter.shared.model.Report
import com.areareporter.shared.model.ReportCategory
import com.areareporter.shared.model.SyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Data Access Object for reports.
 * Wraps SQLDelight queries with domain model conversions.
 */
class ReportDao(driverFactory: DatabaseDriverFactory) {

    private val database = AreaReporterDatabase(driverFactory.createDriver())
    private val queries = database.reportsQueries

    /**
     * Observe all reports as a reactive Flow.
     * UI should always read from this – offline-first principle.
     */
    fun observeAllReports(): Flow<List<Report>> =
        queries.selectAllReports()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toDomain() } }

    /**
     * Observe a single report by ID.
     */
    fun observeReportById(id: String): Flow<Report?> =
        queries.selectReportById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { it?.toDomain() }

    /**
     * Get all reports with PENDING or FAILED status for the sync queue.
     */
    suspend fun getPendingReports(): List<Report> = withContext(Dispatchers.Default) {
        queries.selectPendingReports().executeAsList().map { it.toDomain() }
    }

    /**
     * Insert or replace a report in the local database.
     * This is the first step in the offline-first flow.
     */
    suspend fun insertReport(report: Report) = withContext(Dispatchers.Default) {
        queries.insertReport(
            id = report.id,
            title = report.title,
            description = report.description,
            category = report.category.name,
            latitude = report.latitude,
            longitude = report.longitude,
            imagePath = report.imagePath,
            status = report.status.name,
            createdAt = report.createdAt,
            retryCount = report.retryCount.toLong(),
            lastSyncAttempt = report.lastSyncAttempt
        )
    }

    /**
     * Update only the sync status of a report.
     */
    suspend fun updateStatus(id: String, status: SyncStatus) = withContext(Dispatchers.Default) {
        queries.updateReportStatus(status = status.name, id = id)
    }

    /**
     * Update sync status along with retry info after a failed attempt.
     */
    suspend fun updateSyncInfo(
        id: String,
        status: SyncStatus,
        retryCount: Int,
        lastSyncAttempt: Long
    ) = withContext(Dispatchers.Default) {
        queries.updateReportSyncInfo(
            status = status.name,
            retryCount = retryCount.toLong(),
            lastSyncAttempt = lastSyncAttempt,
            id = id
        )
    }

    /**
     * Delete a report from local storage.
     */
    suspend fun deleteReport(id: String) = withContext(Dispatchers.Default) {
        queries.deleteReport(id)
    }

    /**
     * Count reports that need syncing.
     */
    suspend fun countPending(): Long = withContext(Dispatchers.Default) {
        queries.countPendingReports().executeAsOne()
    }
}

// Extension to convert SQLDelight generated row to domain model
private fun com.areareporter.shared.database.Reports.toDomain(): Report = Report(
    id = id,
    title = title,
    description = description,
    category = ReportCategory.fromString(category),
    latitude = latitude,
    longitude = longitude,
    imagePath = imagePath,
    status = try { SyncStatus.valueOf(status) } catch (e: Exception) { SyncStatus.PENDING },
    createdAt = createdAt,
    retryCount = retryCount.toInt(),
    lastSyncAttempt = lastSyncAttempt
)
