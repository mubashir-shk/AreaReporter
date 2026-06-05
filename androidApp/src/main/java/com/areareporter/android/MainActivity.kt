package com.areareporter.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.areareporter.shared.AreaReporterApp
import com.areareporter.shared.sync.SyncWorker

/**
 * Main Android activity.
 * Sets up the Compose UI and schedules WorkManager sync.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Schedule periodic background sync via WorkManager
        SyncWorker.schedule(this)

        setContent {
            AreaReporterApp()
        }
    }
}
