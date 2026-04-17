package com.example.apltracker.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// ---- /bridge ----
// ALS 官方返回字段非常多，这里只抽取 UI 展示需要的核心部分，其它用 JsonElement 兜底
// 以避免因接口新增字段导致反序列化失败。
@Serializable
data class BridgeResponse(
    val global: GlobalInfo? = null,
    val realtime: RealtimeInfo? = null,
    val legends: LegendsInfo? = null,
    @SerialName("Error") val error: String? = null,
    val errorCode: String? = null,
)

@Serializable
data class GlobalInfo(
    val name: String? = null,
    val uid: JsonElement? = null,
    val avatar: String? = null,
    val platform: String? = null,
    val level: Int? = null,
    val toNextLevelPercent: Int? = null,
    val internalUpdateCount: Int? = null,
    val bans: BansInfo? = null,
    val rank: RankInfo? = null,
    val arena: RankInfo? = null,
    val battlepass: BattlepassInfo? = null,
)

@Serializable
data class BansInfo(
    val isActive: Boolean? = null,
    val remainingSeconds: Long? = null,
    val last_banReason: String? = null,
)

@Serializable
data class RankInfo(
    val rankScore: Int? = null,
    val rankName: String? = null,
    val rankDiv: Int? = null,
    val ladderPosPlatform: Int? = null,
    val rankImg: String? = null,
    val rankedSeason: String? = null,
)

@Serializable
data class BattlepassInfo(
    val level: JsonElement? = null,
    val history: JsonElement? = null,
)

@Serializable
data class RealtimeInfo(
    val lobbyState: String? = null,
    val isOnline: Int? = null,
    val isInGame: Int? = null,
    val canJoin: Int? = null,
    val partyFull: Int? = null,
    val selectedLegend: String? = null,
    val currentState: String? = null,
    val currentStateSinceTimestamp: Long? = null,
    val currentStateAsText: String? = null,
)

@Serializable
data class LegendsInfo(
    val selected: SelectedLegend? = null,
    val all: JsonElement? = null,
)

@Serializable
data class SelectedLegend(
    @SerialName("LegendName") val legendName: String? = null,
    val data: List<LegendStat>? = null,
    val gameInfo: LegendGameInfo? = null,
    val ImgAssets: LegendImgAssets? = null,
)

@Serializable
data class LegendStat(
    val name: String? = null,
    val value: JsonElement? = null,
    val key: String? = null,
    val global: Boolean? = null,
    val rank: JsonElement? = null,
    val rankPlatformSpecific: JsonElement? = null,
)

@Serializable
data class LegendGameInfo(
    val skin: String? = null,
    val skinRarity: String? = null,
    val frame: String? = null,
    val frameRarity: String? = null,
    val pose: String? = null,
    val poseRarity: String? = null,
    val intro: String? = null,
    val introRarity: String? = null,
    val badges: JsonElement? = null,
)

@Serializable
data class LegendImgAssets(
    val icon: String? = null,
    val banner: String? = null,
)

// ---- /games (Match History) ----
@Serializable
data class GameEntry(
    val uid: JsonElement? = null,
    val player: String? = null,
    val playerStats: JsonElement? = null,
    val gameStartTimestamp: Long? = null,
    val gameLengthSecs: Long? = null,
    val legendPlayed: String? = null,
    val rankedBattlePass: JsonElement? = null,
    val BRScoreChange: JsonElement? = null,
    val ArenasScoreChange: JsonElement? = null,
)

// ---- /servers ----
@Serializable
data class ServersResponse(
    @SerialName("Origin_login") val originLogin: Map<String, ServerRegionStatus>? = null,
    @SerialName("EA_novafusion") val eaNovafusion: Map<String, ServerRegionStatus>? = null,
    @SerialName("EA_accounts") val eaAccounts: Map<String, ServerRegionStatus>? = null,
    @SerialName("ApexOauth_Crossplay") val apexOauthCrossplay: Map<String, ServerRegionStatus>? = null,
    @SerialName("selfCoreTest") val selfCoreTest: Map<String, ServerRegionStatus>? = null,
    @SerialName("otherPlatforms") val otherPlatforms: Map<String, Map<String, ServerRegionStatus>>? = null,
)

@Serializable
data class ServerRegionStatus(
    @SerialName("Status") val status: String? = null,
    @SerialName("HostName") val hostName: String? = null,
    @SerialName("ResponseTime") val responseTime: JsonElement? = null,
    @SerialName("QueryTimestamp") val queryTimestamp: Long? = null,
)

// ---- /maprotation ----
@Serializable
data class MapRotationResponse(
    val battle_royale: RotationPair? = null,
    val arenas: RotationPair? = null,
    val ranked: RotationPair? = null,
    val arenasRanked: RotationPair? = null,
    val ltm: RotationPair? = null,
    val control: RotationPair? = null,
    val gunrun: RotationPair? = null,
)

@Serializable
data class RotationPair(
    val current: MapSlot? = null,
    val next: MapSlot? = null,
)

@Serializable
data class MapSlot(
    val start: Long? = null,
    val end: Long? = null,
    val readableDate_start: String? = null,
    val readableDate_end: String? = null,
    val map: String? = null,
    val code: String? = null,
    val DurationInSecs: Long? = null,
    val DurationInMinutes: Long? = null,
    val asset: String? = null,
    val remainingSecs: Long? = null,
    val remainingMins: Long? = null,
    val remainingTimer: String? = null,
    val eventName: String? = null,
)

// ---- /store ----
@Serializable
data class StoreItem(
    val name: String? = null,
    val bundle: String? = null,
    val bundleType: String? = null,
    val bundleContent: JsonElement? = null,
    val start: JsonElement? = null,
    val end: JsonElement? = null,
    val expiration: Expiration? = null,
    val pricing: List<Pricing>? = null,
    val assets: Assets? = null,
    val title: String? = null,
)

@Serializable
data class Expiration(
    val startDate: String? = null,
    val endDate: String? = null,
    val remaining: Remaining? = null,
)

@Serializable
data class Remaining(
    val seconds: Long? = null,
    val minutes: Long? = null,
    val hours: Long? = null,
    val days: Long? = null,
    @SerialName("ShortFormat") val shortFormat: String? = null,
)

@Serializable
data class Pricing(
    val currency: String? = null,
    val cost: Int? = null,
)

@Serializable
data class Assets(
    val large: String? = null,
    val small: String? = null,
    val video: String? = null,
)

// ---- 平台枚举 ----
enum class ApexPlatform(val value: String, val label: String) {
    PC("PC", "PC (Steam/Origin)"),
    X1("X1", "Xbox"),
    PS4("PS4", "PlayStation"),
    SWITCH("SWITCH", "Switch"),
}
