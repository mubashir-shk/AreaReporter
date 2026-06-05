package com.areareporter.shared.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.areareporter.shared.model.Report
import com.areareporter.shared.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the report detail screen.
 */
data class ReportDetailUiState(
    val report: Report? = null,
    val isLoading: Boolean = true,
    val isRetrying: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel for the report detail screen.
 */
class ReportDetailViewModel(
    private val reportId: String,
    private val repository: ReportRepository
) : ScreenModel {

    private val _uiState = MutableStateFlow(ReportDetailUiState())
    val uiState: StateFlow<ReportDetailUiState> = _uiState.asStateFlow()

    init {
        observeReport()
    }

    private fun observeReport() {
        screenModelScope.launch {
            repository.observeReportById(reportId).collect { report ->
                _uiState.update { state ->
                    state.copy(
                        report = report,
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Manually retry syncing this report.
     */
    fun retrySync() {
        _uiState.update { it.copy(isRetrying = true) }
        repository.retrySync(reportId)

        screenModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _uiState.update { it.copy(isRetrying = false) }
        }
    }

    /**
     * Delete this report from local storage.
     */
    fun deleteReport() {
        screenModelScope.launch {
            try {
                repository.deleteReport(reportId)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to delete: ${e.message}") }
            }
        }
    }
}
