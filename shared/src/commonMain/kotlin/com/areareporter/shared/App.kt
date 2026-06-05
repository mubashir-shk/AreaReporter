package com.areareporter.shared

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.areareporter.shared.ui.AreaReporterTheme
import com.areareporter.shared.ui.screens.ReportListScreen

/**
 * Main Compose app entry point.
 * Sets up theme and Voyager navigation.
 */
@Composable
fun AreaReporterApp() {
    AreaReporterTheme {
        Navigator(
            screen = ReportListScreen()
        ) { navigator ->
            SlideTransition(navigator)
        }
    }
}
