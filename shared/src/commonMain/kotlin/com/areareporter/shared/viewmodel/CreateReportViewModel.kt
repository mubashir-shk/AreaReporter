package com.areareporter.shared.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.areareporter.shared.model.ReportCategory
import com.areareporter.shared.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the create report screen.
 */
data class CreateReportUiState(
    val title: String = "",
    val description: String = "",
    val selectedCategory: ReportCategory = ReportCategory.POTHOLE,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val imagePath: String? = null,
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
    val errorMessage: String? = null,
    val locationAcquired: Boolean = false
)

/**
 * ViewModel for creating a new report.
 * Handles form state and submission.
 */
class CreateReportViewModel(
    private val repository: ReportRepository
) : ScreenModel {

    private val _uiState = MutableStateFlow(CreateReportUiState())
    val uiState: StateFlow<CreateReportUiState> = _uiState.asStateFlow()

    fun onTitleChange(value: String) {
        _uiState.update { it.copy(title = value, errorMessage = null) }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(description = value, errorMessage = null) }
    }

    fun onCategorySelected(category: ReportCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onLocationUpdated(latitude: Double, longitude: Double) {
        _uiState.update {
            it.copy(
                latitude = latitude,
                longitude = longitude,
                locationAcquired = true
            )
        }
    }

    fun onImageSelected(path: String?) {
        _uiState.update { it.copy(imagePath = path) }
    }

    /**
     * Validate and submit the report.
     *
     * OFFLINE-FIRST:
     * Report is saved locally immediately.
     * Sync happens in the background automatically.
     */
    fun submitReport() {
        val state = _uiState.value

        // Validation
        if (state.title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter a title") }
            return
        }
        if (state.description.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter a description") }
            return
        }

        screenModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

            try {
                repository.createReport(
                    title = state.title,
                    description = state.description,
                    category = state.selectedCategory,
                    latitude = state.latitude,
                    longitude = state.longitude,
                    imagePath = state.imagePath
                )

                // Success – report saved locally, sync queued
                _uiState.update { it.copy(isSubmitting = false, isSubmitted = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = "Failed to save report: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun resetSubmittedState() {
        _uiState.update { it.copy(isSubmitted = false) }
    }
}
