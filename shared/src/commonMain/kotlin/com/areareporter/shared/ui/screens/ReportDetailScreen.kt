package com.areareporter.shared.ui.screens

import androidx.compose.foundation.layout.Arrangement
import kotlin.math.pow
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.areareporter.shared.model.Report
import com.areareporter.shared.model.SyncStatus
import com.areareporter.shared.ui.components.CategoryChip
import com.areareporter.shared.ui.components.SyncStatusBadge
import com.areareporter.shared.viewmodel.ReportDetailViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.parameter.parametersOf

/**
 * Screen showing full detail for a single report.
 */
data class ReportDetailScreen(val reportId: String) : Screen, KoinComponent {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberScreenModel(tag = reportId) {
            getKoin().get<ReportDetailViewModel>(parameters = { parametersOf(reportId) })
        }
        val uiState by viewModel.uiState.collectAsState()
        var showDeleteDialog by remember { mutableStateOf(false) }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Report") },
                text = { Text("Are you sure you want to delete this report? This cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteReport()
                            showDeleteDialog = false
                            navigator.pop()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
                }
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Report Details", fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                uiState.report == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Report not found")
                    }
                }

                else -> {
                    ReportDetailContent(
                        report = uiState.report!!,
                        isRetrying = uiState.isRetrying,
                        onRetry = { viewModel.retrySync() },
                        modifier = Modifier.fillMaxSize().padding(paddingValues)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportDetailContent(
    report: Report,
    isRetrying: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryChip(emoji = report.category.emoji, name = report.category.displayName)
            SyncStatusBadge(status = report.status)
        }

        Text(
            text = report.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))
                Text(text = report.description, style = MaterialTheme.typography.bodyLarge)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            InfoRow(
                icon = {
                    Icon(
                        Icons.Default.LocationOn, null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = "Location",
                value = "Lat: ${report.latitude.roundTo(5)}\nLon: ${report.longitude.roundTo(5)}"
            )
            InfoRow(
                icon = {
                    Icon(
                        Icons.Default.Schedule, null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = "Reported At",
                value = formatTimestampFull(report.createdAt)
            )
            if (report.imagePath != null) {
                InfoRow(
                    icon = {
                        Icon(
                            Icons.Default.Image, null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = "Photo",
                    value = "Attached"
                )
            }
        }

        SyncInfoCard(report = report)

        if (report.status == SyncStatus.FAILED || report.status == SyncStatus.PENDING) {
            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isRetrying,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isRetrying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                        Text("Retry Sync", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun SyncInfoCard(report: Report) {
    val (bgColor, title, message) = when (report.status) {
        SyncStatus.PENDING -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            "⏳ Sync Pending",
            "This report is queued and will sync automatically when internet is available."
        )
        SyncStatus.SYNCING -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            "🔄 Syncing...",
            "Uploading to server. Please wait."
        )
        SyncStatus.SYNCED -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            "✅ Synced",
            "Successfully uploaded to the server. Retry count: ${report.retryCount}"
        )
        SyncStatus.FAILED -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            "❌ Sync Failed",
            "Failed after ${report.retryCount} attempt(s). Tap 'Retry Sync' to try again."
        )
    }

    Surface(shape = RoundedCornerShape(12.dp), color = bgColor) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoRow(icon: @Composable () -> Unit, label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            icon()
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(text = value, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

private fun Double.roundTo(decimals: Int): String {
    val multiplier = 10.0.pow(decimals)
    val rounded = kotlin.math.round(this * multiplier).toDouble() / multiplier
    val str = rounded.toString()
    val dotIdx = str.indexOf('.')
    val intPart = if (dotIdx >= 0) str.substring(0, dotIdx) else str
    val fracRaw = if (dotIdx >= 0) str.substring(dotIdx + 1) else ""
    val fracPart = fracRaw.padEnd(decimals, '0').take(decimals)
    return "$intPart.$fracPart"
}

private fun formatTimestampFull(epochMs: Long): String {
    return try {
        val instant = Instant.fromEpochMilliseconds(epochMs)
        val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        "${local.dayOfMonth}/${local.monthNumber}/${local.year} " +
            "${local.hour.toString().padStart(2, '0')}:" +
            "${local.minute.toString().padStart(2, '0')}:" +
            local.second.toString().padStart(2, '0')
    } catch (e: Exception) {
        "Unknown"
    }
}
