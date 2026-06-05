package com.areareporter.shared.utils

import kotlinx.coroutines.flow.StateFlow

/**
 * Connectivity status enum.
 */
enum class ConnectivityStatus {
    AVAILABLE,
    UNAVAILABLE,
    LOSING,
    LOST
}

/**
 * Platform-specific connectivity observer.
 * Emits connectivity status changes as a Flow.
 */
expect class ConnectivityObserver {
    val connectivityStatus: StateFlow<ConnectivityStatus>
    fun isConnected(): Boolean
}
