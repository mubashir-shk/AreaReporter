package com.areareporter.android

import android.app.Application
import com.areareporter.shared.di.androidModule
import com.areareporter.shared.di.commonModule
import com.areareporter.shared.sync.SyncEngine
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Android Application class.
 * Initializes Koin DI and starts the SyncEngine.
 */
class AreaReporterApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Koin dependency injection
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@AreaReporterApplication)
            modules(androidModule, commonModule)
        }

        // Start the sync engine – begins observing connectivity
        get<SyncEngine>().start()
    }
}
