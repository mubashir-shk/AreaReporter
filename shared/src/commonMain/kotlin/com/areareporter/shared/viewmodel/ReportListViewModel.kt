package com.areareporter.shared.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.areareporter.shared.model.Report
import com.areareporter.shared.model.SyncStatus
import com.areareporter.shared.repository.ReportRepository
import com.areareporter.shared.utils.ConnectivityObserver
import com.areareporter.shared.utils.ConnectivityStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the reports list screen.
 */
data class ReportListUiState(
    val reports: List<Report> = emptyList(),
    val isLoading: Boolean = true,
    val isConnected: Boolean = true,
    val pendingCount: Int = 0,
    val errorMessage: String? = null
)

/**
 * ViewModel for the reports list screen.
 * Uses Voyager ScreenModel lifecycle.
 */
class ReportListViewModel(
    private val repository: ReportRepository,
    private val connectivityObserver: ConnectivityObserver
) : ScreenModel {

    private val _uiState = MutableStateFlow(ReportListUiState())
    val uiState: StateFlow<ReportListUiState> = _uiState.asStateFlow()

    init {
        observeReports()
        observeConnectivity()
    }

    private fun observeReports() {
        screenModelScope.launch {
            repository.observeAllReports().collect { reports ->
                _uiState.update { state ->
                    state.copy(
                        reports = reports,
                        isLoading = false,
                        pendingCount = reports.count {
                            it.status == SyncStatus.PENDING || it.status == SyncStatus.SYNCING
                        }
                    )
                }
            }
        }
    }

    private fun observeConnectivity() {
        screenModelScope.launch {
            connectivityObserver.connectivityStatus.collect { status ->
                _uiState.update { it.copy(isConnected = status == ConnectivityStatus.AVAILABLE) }

                // Auto-sync when connection returns
                if (status == ConnectivityStatus.AVAILABLE) {
                    syncAll()
                }
            }
        }
    }

    /**
     * Trigger manual sync of all pending reports.
     */
    fun syncAll() {
        screenModelScope.launch {
            try {
                repository.syncAll()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Sync failed: ${e.message}") }
            }
        }
    }

    /**
     * Clear error message after showing it.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
