package com.example.apltracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.apltracker.data.ApexRepository
import com.example.apltracker.data.BridgeResponse
import com.example.apltracker.data.LegendAllEntry
import com.example.apltracker.data.LegendStat
import com.example.apltracker.data.RankInfo
import com.example.apltracker.ui.ErrorBanner
import com.example.apltracker.ui.LoadingBox
import com.example.apltracker.ui.LocalCurrentUser
import com.example.apltracker.ui.UiState
import com.example.apltracker.ui.toErrorText
import com.example.apltracker.data.TotalStat
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull

@Composable
fun PlayerStatsScreen(repo: ApexRepository) {
    val user = LocalCurrentUser.current
    var state: UiState<BridgeResponse> by remember { mutableStateOf(UiState.Loading) }

    LaunchedEffect(user.name, user.platform) {
        if (user.name.isBlank()) {
            state = UiState.Error("未设置玩家：请在顶部设置当前玩家后再进入此页面。")
            return@LaunchedEffect
        }
        state = UiState.Loading
        state = runCatching { repo.getPlayerBridge(user.name, user.platform) }
            .fold(
                onSuccess = { resp ->
                    val err = resp.error ?: resp.errorCode
                    if (err != null) UiState.Error("API 错误: $err") else UiState.Success(resp)
                },
                onFailure = { UiState.Error(it.toErrorText()) },
            )
    }

    when (val s = state) {
        UiState.Idle, UiState.Loading -> LoadingBox()
        is UiState.Error -> ErrorBanner(s.message, Modifier.fillMaxSize())
        is UiState.Success -> PlayerStatsContent(s.data)
    }
}

@Composable
private fun PlayerStatsContent(data: BridgeResponse) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        data.global?.let { g ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (!g.avatar.isNullOrBlank()) {
                        AsyncImage(
                            model = g.avatar,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                        )
                        Spacer(Modifier.size(12.dp))
                    }
                    Column {
                        Text(g.name ?: "未知玩家", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("平台：${g.platform ?: "-"}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("等级：${g.level ?: "-"}（距下一级 ${g.toNextLevelPercent ?: 0}%）")
                    }
                }
            }
        }

        data.global?.rank?.let { r ->
            RankCard(title = "大逃杀段位（当前赛季）", rank = r)
        }

        data.total?.let { CareerSummaryCard(it) }

        data.realtime?.let { rt ->
            Card {
                Column(Modifier.padding(16.dp)) {
                    Text("在线状态", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text("大厅：${rt.lobbyState ?: "-"}")
                    Text("当前状态：${rt.currentStateAsText ?: rt.currentState ?: "-"}")
                    Text("所选英雄：${rt.selectedLegend ?: "-"}")
                    Text("是否在线：${if (rt.isOnline == 1) "是" else "否"} / 是否在游戏：${if (rt.isInGame == 1) "是" else "否"}")
                }
            }
        }

        data.legends?.selected?.let { sel ->
            Card {
                Column(Modifier.padding(16.dp)) {
                    Text("当前英雄：${sel.legendName ?: "-"}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(6.dp))
                    if (!sel.ImgAssets?.icon.isNullOrBlank()) {
                        AsyncImage(model = sel.ImgAssets?.icon, contentDescription = null, modifier = Modifier.size(72.dp))
                        Spacer(Modifier.height(6.dp))
                    }
                    sel.data?.forEach { stat ->
                        Text("• ${stat.name ?: stat.key ?: "stat"}：${stat.displayValue()}")
                    }
                }
            }
        }

        data.legends?.all?.let { all ->
            AllLegendsCard(all)
        }
    }
}

private fun LegendStat.displayValue(): String {
    return (value as? JsonPrimitive)?.content ?: value?.toString() ?: "-"
}

@Composable
private fun AllLegendsCard(all: Map<String, LegendAllEntry>) {
    // 不显示 Global 聚合（已在生涯汇总卡中体现）和完全没有任何信息的英雄
    val entries = all.entries
        .filter { it.key != "Global" }
        .filter { (_, v) -> !v.data.isNullOrEmpty() || v.gameInfo?.badges != null }
        .sortedBy { it.key }
    if (entries.isEmpty()) return

    Card {
        Column(Modifier.padding(16.dp)) {
            Text(
                "英雄数据（按徽章 / 追踪器）",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                "注：ALS 接口不提供历史赛季段位，这里改为展示各英雄徽章与击杀 / 技能统计。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            entries.forEach { (name, entry) ->
                LegendRow(name, entry)
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun LegendRow(name: String, entry: LegendAllEntry) {
    Row(verticalAlignment = Alignment.Top) {
        if (!entry.ImgAssets?.icon.isNullOrBlank()) {
            AsyncImage(
                model = entry.ImgAssets?.icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
            )
            Spacer(Modifier.width(10.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.Bold)
            val badges = (entry.gameInfo?.badges as? JsonArray)
            if (badges != null && badges.isNotEmpty()) {
                badges.forEach { b ->
                    val bn = (b as? JsonObject)?.get("name")?.let { (it as? JsonPrimitive)?.content }
                    val bv = (b as? JsonObject)?.get("value")?.let { (it as? JsonPrimitive)?.content }
                    if (bn != null) {
                        Text(
                            "徽章：$bn${if (bv != null) " × $bv" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
            entry.data?.forEach { stat ->
                Text(
                    "• ${stat.name ?: stat.key ?: "stat"}：${stat.displayValue()}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun CareerSummaryCard(total: Map<String, TotalStat>) {
    fun intOf(key: String): Int? =
        (total[key]?.value as? JsonPrimitive)?.intOrNull

    fun strOf(key: String): String? =
        (total[key]?.value as? JsonPrimitive)?.content

    // BR 相关关键数据：specialEvent_* 为大逃杀汇总
    val brKills = intOf("specialEvent_kills") ?: intOf("kills")
    val brDamage = intOf("specialEvent_damage") ?: intOf("damage")
    val brWins = intOf("specialEvent_wins")
    val careerKills = intOf("career_kills")
    val careerWins = intOf("career_wins")
    val careerRevives = intOf("career_revives")
    val kd = strOf("kd")
    val headshots = intOf("headshots")

    val rows = listOfNotNull(
        brKills?.let { "BR 击杀（specialEvent_kills）" to it.toString() },
        brDamage?.let { "BR 伤害" to it.toString() },
        brWins?.let { "BR 胜场" to it.toString() },
        careerKills?.let { "生涯击杀" to it.toString() },
        careerWins?.let { "生涯胜场" to it.toString() },
        careerRevives?.let { "生涯救援" to it.toString() },
        headshots?.let { "爆头数" to it.toString() },
        kd?.takeIf { it != "-1" }?.let { "KD" to it },
    )
    if (rows.isEmpty()) return

    Card {
        Column(Modifier.padding(16.dp)) {
            Text(
                "个人战绩汇总",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(6.dp))
            rows.forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                ) {
                    Text(label, modifier = Modifier.weight(1f))
                    Text(value, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun RankCard(title: String, rank: RankInfo) {
    Card {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!rank.rankImg.isNullOrBlank()) {
                    AsyncImage(model = rank.rankImg, contentDescription = null, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.size(8.dp))
                }
                Column {
                    Text("${rank.rankName ?: "-"} ${rank.rankDiv ?: ""}")
                    Text("分数：${rank.rankScore ?: 0}")
                    Text("赛季：${rank.rankedSeason ?: "-"}")
                    rank.ladderPosPlatform?.takeIf { it > 0 }?.let {
                        Text("平台排名：#$it")
                    }
                }
            }
        }
    }
}
