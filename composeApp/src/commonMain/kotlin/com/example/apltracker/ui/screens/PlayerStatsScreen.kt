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
import com.example.apltracker.ui.ErrorBanner
import com.example.apltracker.ui.LoadingBox
import com.example.apltracker.ui.LocalCurrentUser
import com.example.apltracker.ui.UiState
import com.example.apltracker.ui.toErrorText
import kotlinx.serialization.json.JsonPrimitive

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
            Card {
                Column(Modifier.padding(16.dp)) {
                    Text("大逃杀段位", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!r.rankImg.isNullOrBlank()) {
                            AsyncImage(model = r.rankImg, contentDescription = null, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.size(8.dp))
                        }
                        Column {
                            Text("${r.rankName ?: "-"} ${r.rankDiv ?: ""}")
                            Text("分数：${r.rankScore ?: 0}")
                            Text("赛季：${r.rankedSeason ?: "-"}")
                        }
                    }
                }
            }
        }

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
                        val v = (stat.value as? JsonPrimitive)?.content ?: stat.value?.toString() ?: "-"
                        Text("• ${stat.name ?: stat.key ?: "stat"}：$v")
                    }
                }
            }
        }
    }
}
