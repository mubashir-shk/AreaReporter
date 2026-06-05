package com.areareporter.shared.network

import com.areareporter.shared.model.ReportUploadRequest
import com.areareporter.shared.model.ReportUploadResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json

/**
 * Mock API service that simulates a real backend.
 *
 * In production, replace the simulateNetworkCall() with real Ktor HTTP calls.
 * The mock randomly succeeds or fails to demonstrate retry behavior.
 */
class MockApiService {

    // Simulated success rate: 70% of the time, sync succeeds
    private val simulatedSuccessRate = 0.70

    /**
     * Upload a report to the (mock) backend.
     *
     * Simulates:
     * - 500-1500ms network latency
     * - 70% success rate
     * - Realistic error conditions
     */
    suspend fun uploadReport(request: ReportUploadRequest): ReportUploadResponse {
        // Simulate network latency
        delay((500..1500).random().toLong())

        // Simulate network success/failure
        return if (kotlin.random.Random.nextDouble() < simulatedSuccessRate) {
            ReportUploadResponse(
                success = true,
                serverId = "server_${request.id}_${kotlinx.datetime.Clock.System.now().toEpochMilliseconds()}",
                message = "Report uploaded successfully"
            )
        } else {
            // Simulate various error types
            val errorMessages = listOf(
                "Connection timeout",
                "Server error: 503 Service Unavailable",
                "Network unreachable",
                "Request failed: Bad Gateway"
            )
            ReportUploadResponse(
                success = false,
                serverId = null,
                message = errorMessages.random()
            )
        }
    }
}

/**
 * Creates a configured Ktor HttpClient for real API calls.
 * Currently unused in mock mode – ready for production integration.
 */
fun createHttpClient(): HttpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
        })
    }
    install(Logging) {
        level = LogLevel.BODY
        logger = object : Logger {
            override fun log(message: String) {
                println("[Ktor] $message")
            }
        }
    }
}
