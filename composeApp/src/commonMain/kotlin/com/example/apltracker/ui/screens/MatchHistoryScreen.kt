package com.example.apltracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.apltracker.data.ApexRepository
import com.example.apltracker.data.GameEntry
import com.example.apltracker.ui.ErrorBanner
import com.example.apltracker.ui.LoadingBox
import com.example.apltracker.ui.LocalCurrentUser
import com.example.apltracker.ui.UiState
import com.example.apltracker.ui.toErrorText
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull

@Composable
fun MatchHistoryScreen(repo: ApexRepository) {
    val user = LocalCurrentUser.current
    var state: UiState<List<GameEntry>> by remember { mutableStateOf(UiState.Loading) }

    LaunchedEffect(user.name, user.platform) {
        if (user.name.isBlank()) {
            state = UiState.Error("未设置玩家：请在顶部设置当前玩家。")
            return@LaunchedEffect
        }
        state = UiState.Loading
        state = runCatching { repo.getMatchHistory(user.name, user.platform) }
            .fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.toErrorText()) },
            )
    }

    when (val s = state) {
        UiState.Idle, UiState.Loading -> LoadingBox()
        is UiState.Error -> ErrorBanner(s.message, Modifier.fillMaxSize())
        is UiState.Success -> {
            if (s.data.isEmpty()) {
                ErrorBanner("暂无对战记录。对战记录跟踪需要该玩家在 ALS 官网启用 tracker。", Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(s.data) { entry -> MatchCard(entry) }
                }
            }
        }
    }
}

@Composable
private fun MatchCard(entry: GameEntry) {
    val stats = entry.playerStats as? JsonObject
    fun int(key: String): Int? = (stats?.get(key) as? JsonPrimitive)?.intOrNull
    val kills = int("kills_kills") ?: int("kills") ?: 0
    val damage = int("damage_damage") ?: int("damage") ?: 0
    val rank = int("gameDuration_placement") ?: int("placement")
    val time = entry.gameStartTimestamp?.let {
        runCatching {
            Instant.fromEpochSeconds(it).toLocalDateTime(TimeZone.currentSystemDefault()).toString()
        }.getOrNull()
    } ?: "-"
    val length = entry.gameLengthSecs?.let { "${it / 60} 分钟" } ?: "-"

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = entry.legendPlayed ?: "未知英雄",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Text("时间：$time")
            Text("时长：$length")
            Text("击杀：$kills   伤害：$damage   排名：${rank ?: "-"}")
        }
    }
}
