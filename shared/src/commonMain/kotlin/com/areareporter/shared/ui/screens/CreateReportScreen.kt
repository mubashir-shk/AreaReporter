package com.areareporter.shared.ui.screens

import androidx.compose.foundation.BorderStroke
import kotlin.math.pow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.areareporter.shared.model.ReportCategory
import com.areareporter.shared.viewmodel.CreateReportViewModel
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent

/**
 * Screen for creating a new area problem report.
 */
class CreateReportScreen : Screen, KoinComponent {

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberScreenModel { get<CreateReportViewModel>() }
        val uiState by viewModel.uiState.collectAsState()

        LaunchedEffect(uiState.isSubmitted) {
            if (uiState.isSubmitted) {
                navigator.pop()
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Report a Problem", fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .imePadding()
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SectionLabel("Problem Title")
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = viewModel::onTitleChange,
                    label = { Text("e.g. Deep pothole on Main Street") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    isError = uiState.errorMessage != null && uiState.title.isBlank()
                )

                SectionLabel("Description")
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = viewModel::onDescriptionChange,
                    label = { Text("Describe the problem in detail") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    isError = uiState.errorMessage != null && uiState.description.isBlank()
                )

                SectionLabel("Category")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReportCategory.entries.forEach { category ->
                        val isSelected = uiState.selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onCategorySelected(category) },
                            label = { Text("${category.emoji} ${category.displayName}") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                SectionLabel("Location")
                LocationCard(
                    locationAcquired = uiState.locationAcquired,
                    latitude = uiState.latitude,
                    longitude = uiState.longitude,
                    onCapture = {
                        viewModel.onLocationUpdated(
                            latitude = 19.0760 + (kotlin.random.Random.nextDouble() * 0.01 - 0.005),
                            longitude = 72.8777 + (kotlin.random.Random.nextDouble() * 0.01 - 0.005)
                        )
                    }
                )

                SectionLabel("Photo (Optional)")
                ImagePickerCard(
                    imagePath = uiState.imagePath,
                    onPickImage = {
                        viewModel.onImageSelected(
                            "/mock/image_${Clock.System.now().toEpochMilliseconds()}.jpg"
                        )
                    }
                )

                uiState.errorMessage?.let { error ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = viewModel::submitReport,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    enabled = !uiState.isSubmitting,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Submit Report", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }

    private inline fun <reified T : Any> get(): T = getKoin().get()
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun LocationCard(
    locationAcquired: Boolean,
    latitude: Double,
    longitude: Double,
    onCapture: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (locationAcquired)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        else
            MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            1.dp,
            if (locationAcquired) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = if (locationAcquired) Icons.Default.LocationOn
                    else Icons.Default.LocationSearching,
                    contentDescription = null,
                    tint = if (locationAcquired) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = if (locationAcquired) "Location Captured" else "No Location",
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (locationAcquired) {
                        Text(
                            text = "${latitude.roundTo(5)}, ${longitude.roundTo(5)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            OutlinedButton(
                onClick = onCapture,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(if (locationAcquired) "Update" else "Capture", fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun ImagePickerCard(imagePath: String?, onPickImage: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (imagePath != null)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        else
            MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            1.dp,
            if (imagePath != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = if (imagePath != null) Icons.Default.CheckCircle
                    else Icons.Default.Camera,
                    contentDescription = null,
                    tint = if (imagePath != null) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = if (imagePath != null) "Photo Attached" else "Add Photo",
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (imagePath != null) "Tap to change" else "Camera or gallery",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            OutlinedButton(
                onClick = onPickImage,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(if (imagePath != null) "Change" else "Select", fontSize = 13.sp)
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
