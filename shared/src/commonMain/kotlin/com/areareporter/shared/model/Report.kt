package com.areareporter.shared.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Sync status for a report.
 * - PENDING: Saved locally, not yet attempted to sync
 * - SYNCING: Currently being uploaded
 * - SYNCED: Successfully uploaded to server
 * - FAILED: Sync attempted but failed (will retry)
 */
enum class SyncStatus {
    PENDING,
    SYNCING,
    SYNCED,
    FAILED
}

/**
 * Category of the area problem being reported.
 */
enum class ReportCategory(val displayName: String, val emoji: String) {
    POTHOLE("Pothole", "🕳️"),
    GARBAGE("Garbage", "🗑️"),
    WATER_LEAKAGE("Water Leakage", "💧"),
    BROKEN_STREET_LIGHT("Broken Street Light", "💡"),
    TRAFFIC_SIGNAL("Traffic Signal", "🚦"),
    DRAINAGE("Drainage", "🌊");

    companion object {
        fun fromString(value: String): ReportCategory =
            entries.firstOrNull { it.name == value } ?: POTHOLE
    }
}

/**
 * Domain model for an area problem report.
 */
@Serializable
data class Report(
    val id: String,
    val title: String,
    val description: String,
    val category: ReportCategory,
    val latitude: Double,
    val longitude: Double,
    val imagePath: String? = null,
    val status: SyncStatus = SyncStatus.PENDING,
    val createdAt: Long,
    val retryCount: Int = 0,
    val lastSyncAttempt: Long? = null
)

/**
 * Request payload sent to the mock backend API.
 */
@Serializable
data class ReportUploadRequest(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val imagePath: String? = null,
    val createdAt: Long
)

/**
 * Response from the mock backend API.
 */
@Serializable
data class ReportUploadResponse(
    val success: Boolean,
    val serverId: String? = null,
    val message: String = ""
)
