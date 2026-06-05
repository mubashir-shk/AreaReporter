package com.areareporter.shared.sync

import com.areareporter.shared.database.ReportDao
import com.areareporter.shared.model.Report
import com.areareporter.shared.model.ReportUploadRequest
import com.areareporter.shared.model.SyncStatus
import com.areareporter.shared.network.MockApiService
import com.areareporter.shared.utils.ConnectivityObserver
import com.areareporter.shared.utils.ConnectivityStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * SyncEngine handles all sync operations with:
 * - Automatic retry with exponential backoff
 * - Connectivity awareness (auto-sync when internet returns)
 * - Optimistic UI updates via local DB
 * - Persistent sync state
 */
class SyncEngine(
    private val reportDao: ReportDao,
    private val apiService: MockApiService,
    private val connectivityObserver: ConnectivityObserver
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Max retry attempts before giving up
    private val maxRetries = 5

    // Base delay for exponential backoff (in ms)
    private val baseDelayMs = 1000L

    /**
     * Start the sync engine.
     * Observes connectivity and triggers sync when internet becomes available.
     */
    fun start() {
        scope.launch {
            connectivityObserver.connectivityStatus.collectLatest { status ->
                if (status == ConnectivityStatus.AVAILABLE) {
                    // Internet just became available – sync pending reports
                    syncPendingReports()
                }
            }
        }
    }

    /**
     * Manually trigger sync of all pending/failed reports.
     * Called after a new report is created or from WorkManager.
     */
    suspend fun syncPendingReports() {
        if (!connectivityObserver.isConnected()) return

        val pendingReports = reportDao.getPendingReports()
        pendingReports.forEach { report ->
            syncReport(report)
        }
    }

    /**
     * Sync a single report with exponential backoff retry.
     */
    private suspend fun syncReport(report: Report) {
        if (report.retryCount >= maxRetries) {
            // Exceeded max retries – mark as permanently failed
            reportDao.updateStatus(report.id, SyncStatus.FAILED)
            return
        }

        // Mark as SYNCING
        reportDao.updateStatus(report.id, SyncStatus.SYNCING)

        try {
            val request = ReportUploadRequest(
                id = report.id,
                title = report.title,
                description = report.description,
                category = report.category.name,
                latitude = report.latitude,
                longitude = report.longitude,
                imagePath = report.imagePath,
                createdAt = report.createdAt
            )

            val response = apiService.uploadReport(request)

            if (response.success) {
                // SUCCESS – update to SYNCED
                reportDao.updateStatus(report.id, SyncStatus.SYNCED)
            } else {
                // FAILURE – update with retry info and backoff
                handleSyncFailure(report)
            }
        } catch (e: Exception) {
            // Network or other exception
            handleSyncFailure(report)
        }
    }

    /**
     * Handle a failed sync attempt with exponential backoff.
     */
    private suspend fun handleSyncFailure(report: Report) {
        val newRetryCount = report.retryCount + 1
        val now = getCurrentTimeMillis()

        val newStatus = if (newRetryCount >= maxRetries) SyncStatus.FAILED else SyncStatus.PENDING

        reportDao.updateSyncInfo(
            id = report.id,
            status = newStatus,
            retryCount = newRetryCount,
            lastSyncAttempt = now
        )

        // Exponential backoff before retry
        if (newStatus == SyncStatus.PENDING) {
            val backoffDelay = baseDelayMs * (1L shl minOf(newRetryCount, 5))
            delay(backoffDelay)

            // Retry if still connected
            if (connectivityObserver.isConnected()) {
                val updatedReport = report.copy(
                    status = SyncStatus.PENDING,
                    retryCount = newRetryCount,
                    lastSyncAttempt = now
                )
                syncReport(updatedReport)
            }
        }
    }

    /**
     * Immediately retry a specific failed report.
     */
    fun retryReport(reportId: String) {
        scope.launch {
            val reports = reportDao.getPendingReports()
            val report = reports.firstOrNull { it.id == reportId }
                ?: return@launch

            // Reset retry count for manual retry
            val resetReport = report.copy(retryCount = 0, status = SyncStatus.PENDING)
            reportDao.insertReport(resetReport)
            syncReport(resetReport)
        }
    }

    private fun getCurrentTimeMillis(): Long = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
}
