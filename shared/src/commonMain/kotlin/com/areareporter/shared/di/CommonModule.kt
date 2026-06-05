package com.areareporter.shared.di

import com.areareporter.shared.database.ReportDao
import com.areareporter.shared.network.MockApiService
import com.areareporter.shared.repository.ReportRepository
import com.areareporter.shared.sync.SyncEngine
import com.areareporter.shared.viewmodel.CreateReportViewModel
import com.areareporter.shared.viewmodel.ReportDetailViewModel
import com.areareporter.shared.viewmodel.ReportListViewModel
import org.koin.dsl.module

/**
 * Common Koin module shared across all platforms.
 * Platform modules provide the platform-specific dependencies (Context, etc.)
 */
val commonModule = module {
    // Database
    single { ReportDao(get()) }

    // Network
    single { MockApiService() }

    // Sync
    single { SyncEngine(get(), get(), get()) }

    // Repository
    single { ReportRepository(get(), get()) }

    // ViewModels (factory – new instance each time)
    factory { ReportListViewModel(get(), get()) }
    factory { CreateReportViewModel(get()) }
    factory { (reportId: String) -> ReportDetailViewModel(reportId, get()) }
}
