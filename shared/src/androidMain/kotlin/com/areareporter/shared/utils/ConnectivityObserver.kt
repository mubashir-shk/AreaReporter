package com.areareporter.shared.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Android implementation of ConnectivityObserver.
 * Uses ConnectivityManager.NetworkCallback for reactive updates.
 */
actual class ConnectivityObserver(private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _connectivityStatus = MutableStateFlow(getCurrentStatus())
    actual val connectivityStatus: StateFlow<ConnectivityStatus> =
        _connectivityStatus.asStateFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _connectivityStatus.value = ConnectivityStatus.AVAILABLE
        }

        override fun onLosing(network: Network, maxMsToLive: Int) {
            _connectivityStatus.value = ConnectivityStatus.LOSING
        }

        override fun onLost(network: Network) {
            _connectivityStatus.value = ConnectivityStatus.LOST
        }

        override fun onUnavailable() {
            _connectivityStatus.value = ConnectivityStatus.UNAVAILABLE
        }
    }

    init {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    actual fun isConnected(): Boolean {
        return getCurrentStatus() == ConnectivityStatus.AVAILABLE
    }

    private fun getCurrentStatus(): ConnectivityStatus {
        val network = connectivityManager.activeNetwork ?: return ConnectivityStatus.UNAVAILABLE
        val capabilities = connectivityManager.getNetworkCapabilities(network)
            ?: return ConnectivityStatus.UNAVAILABLE
        return if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            ConnectivityStatus.AVAILABLE
        } else {
            ConnectivityStatus.UNAVAILABLE
        }
    }
}
