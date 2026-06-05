package com.areareporter.shared.di

import com.areareporter.shared.database.DatabaseDriverFactory
import com.areareporter.shared.utils.ConnectivityObserver
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Android-specific Koin module.
 * Provides Android Context-dependent dependencies.
 */
val androidModule = module {
    // Android database driver factory
    single { DatabaseDriverFactory(androidContext()) }

    // Android connectivity observer
    single { ConnectivityObserver(androidContext()) }
}
