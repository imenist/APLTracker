package com.example.apltracker.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object ApexApi {
    const val BASE_URL = "https://api.mozambiquehe.al"
    const val AUTH_KEY = "5eb953a2c3e54507a69adf0064d4a2de"
}

class ApexRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        explicitNulls = false
    }

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 20_000
            connectTimeoutMillis = 15_000
            socketTimeoutMillis = 20_000
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
        install(DefaultRequest) {
            header(HttpHeaders.Accept, "application/json")
        }
        expectSuccess = true
    }

    /** 强制带上 auth 参数的 GET 请求 */
    private suspend inline fun <reified T> apiGet(
        path: String,
        params: Map<String, String?> = emptyMap(),
    ): T {
        val resp = client.get("${ApexApi.BASE_URL}$path") {
            parameter("auth", ApexApi.AUTH_KEY)
            params.forEach { (k, v) -> if (!v.isNullOrBlank()) parameter(k, v) }
        }
        return resp.body()
    }

    // 1. /bridge
    suspend fun getPlayerBridge(player: String, platform: ApexPlatform): BridgeResponse =
        apiGet(
            "/bridge",
            mapOf("player" to player, "platform" to platform.value)
        )

    // 2. /games (官方 Match History 端点)
    suspend fun getMatchHistory(player: String, platform: ApexPlatform): List<GameEntry> =
        apiGet(
            "/games",
            mapOf("player" to player, "platform" to platform.value, "action" to "get")
        )

    // 3. /servers
    suspend fun getServerStatus(): ServersResponse = apiGet("/servers")

    // 4. /maprotation (version=2 提供所有模式)
    suspend fun getMapRotation(): MapRotationResponse =
        apiGet("/maprotation", mapOf("version" to "2"))

    // 5. /store
    suspend fun getStore(): List<StoreItem> = apiGet("/store")

    fun close() { client.close() }
}
