package com.areareporter.shared.repository

import com.areareporter.shared.database.ReportDao
import com.areareporter.shared.model.Report
import com.areareporter.shared.model.ReportCategory
import com.areareporter.shared.model.SyncStatus
import com.areareporter.shared.sync.SyncEngine
import com.benasher44.uuid.uuid4
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock

/**
 * Repository is the single source of truth for reports.
 *
 * OFFLINE-FIRST FLOW:
 * 1. Save to local DB immediately
 * 2. Update UI instantly (observers see the change)
 * 3. Mark as PENDING
 * 4. Queue sync via SyncEngine
 * 5. SyncEngine handles retry + backoff automatically
 *
 * UI always reads from local DB, never directly from network.
 */
class ReportRepository(
    private val reportDao: ReportDao,
    private val syncEngine: SyncEngine
) {

    /**
     * Observe all reports as a reactive stream.
     * This Flow automatically updates when DB changes.
     */
    fun observeAllReports(): Flow<List<Report>> = reportDao.observeAllReports()

    /**
     * Observe a single report by ID.
     */
    fun observeReportById(id: String): Flow<Report?> = reportDao.observeReportById(id)

    /**
     * Create a new report.
     *
     * OFFLINE-FIRST:
     * 1. Generate a UUID locally
     * 2. Save to local DB with PENDING status
     * 3. Return immediately (UI updates optimistically)
     * 4. Queue sync in background
     */
    suspend fun createReport(
        title: String,
        description: String,
        category: ReportCategory,
        latitude: Double,
        longitude: Double,
        imagePath: String? = null
    ): Report {
        val report = Report(
            id = uuid4().toString(),
            title = title,
            description = description,
            category = category,
            latitude = latitude,
            longitude = longitude,
            imagePath = imagePath,
            status = SyncStatus.PENDING,
            createdAt = Clock.System.now().toEpochMilliseconds(),
            retryCount = 0,
            lastSyncAttempt = null
        )

        // Step 1: Save locally immediately
        reportDao.insertReport(report)

        // Step 2: Trigger sync in background (non-blocking)
        syncEngine.syncPendingReports()

        return report
    }

    /**
     * Manually retry a failed report sync.
     */
    fun retrySync(reportId: String) {
        syncEngine.retryReport(reportId)
    }

    /**
     * Trigger a full sync of all pending reports.
     */
    suspend fun syncAll() {
        syncEngine.syncPendingReports()
    }

    /**
     * Delete a report from local storage.
     */
    suspend fun deleteReport(id: String) {
        reportDao.deleteReport(id)
    }

    /**
     * Get count of reports waiting to sync.
     */
    suspend fun getPendingCount(): Long = reportDao.countPending()
}
