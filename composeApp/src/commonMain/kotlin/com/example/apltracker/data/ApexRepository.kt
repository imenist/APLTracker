package com.example.apltracker.data

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

object ApexApi {
    const val BASE_URL = "https://api.mozambiquehe.re"
    const val AUTH_KEY = "5eb953a2c3e54507a69adf0064d4a2de"
}

/** ALS 接口虽然返回 200，但 body 是 `{"Error":"..."}` 这种错误对象时抛出。 */
class ApexApiException(message: String) : RuntimeException(message)

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
            // ALS 的 /servers 等端点返回 Content-Type: text/plain，
            // 这里额外注册一下，避免 NoTransformationFoundException
            json(json, contentType = ContentType.Text.Plain)
            json(json, contentType = ContentType.Any)
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
        expectSuccess = true
    }

    /** 强制带上 auth 参数的 GET 请求 */
    private suspend inline fun <reified T> apiGet(
        path: String,
        params: Map<String, String?> = emptyMap(),
    ): T {
        val resp = client.get("${ApexApi.BASE_URL}$path") {
            headers {
                // Apache MultiViews 会对 Accept: application/json 返回 406，
                // 这里强制覆盖为 */* 与 curl 保持一致
                set(HttpHeaders.Accept, "*/*")
            }
            parameter("auth", ApexApi.AUTH_KEY)
            params.forEach { (k, v) -> if (!v.isNullOrBlank()) parameter(k, v) }
        }
        val text = resp.bodyAsText()
        return try {
            json.decodeFromString(text)
        } catch (e: Exception) {
            // ALS 部分端点即便 HTTP 200 也会返回 {"Error":"..."} 形式的错误对象
            // （例如 /store 会返回 "This endpoint is no longer available at this time."），
            // 这时候强行按原目标类型反序列化会抛出难以理解的 JsonConvertException，
            // 这里统一转成 ApexApiException 好让上层展示可读的提示。
            val errMsg = runCatching { json.parseToJsonElement(text) }
                .getOrNull()
                ?.let { it as? JsonObject }
                ?.let { obj ->
                    obj["Error"]?.jsonPrimitive?.contentOrNull
                        ?: obj["error"]?.jsonPrimitive?.contentOrNull
                }
            if (!errMsg.isNullOrBlank()) throw ApexApiException(errMsg)
            throw e
        }
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

    // 6. /origin
    suspend fun getOriginUID(player: String): OriginResponse =
        apiGet(
            "/origin",
            mapOf("player" to player)
        )

    fun close() { client.close() }
}
