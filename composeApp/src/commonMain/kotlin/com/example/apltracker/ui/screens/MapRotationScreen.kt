package com.example.apltracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.apltracker.data.ApexRepository
import com.example.apltracker.data.MapRotationResponse
import com.example.apltracker.data.RotationPair
import com.example.apltracker.ui.ErrorBanner
import com.example.apltracker.ui.LoadingBox
import com.example.apltracker.ui.UiState
import com.example.apltracker.ui.toErrorText

@Composable
fun MapRotationScreen(repo: ApexRepository) {
    var state: UiState<MapRotationResponse> by remember { mutableStateOf(UiState.Loading) }
    LaunchedEffect(Unit) {
        state = runCatching { repo.getMapRotation() }.fold(
            onSuccess = { UiState.Success(it) },
            onFailure = { UiState.Error(it.toErrorText()) },
        )
    }
    when (val s = state) {
        UiState.Idle, UiState.Loading -> LoadingBox()
        is UiState.Error -> ErrorBanner(s.message, Modifier.fillMaxSize())
        is UiState.Success -> {
            val entries = buildList {
                s.data.battle_royale?.let { add("大逃杀 (Battle Royale)" to it) }
                s.data.ranked?.let { add("排位 (Ranked)" to it) }
                s.data.ltm?.let { add("限时模式 (LTM)" to it) }
                s.data.control?.let { add("控制 (Control)" to it) }
                s.data.gunrun?.let { add("抢先体验 (Gun Run)" to it) }
                s.data.arenas?.let { add("竞技场 (Arenas)" to it) }
                s.data.arenasRanked?.let { add("竞技场排位" to it) }
            }
            if (entries.isEmpty()) {
                ErrorBanner("没有可用的地图轮换数据。", Modifier.fillMaxSize())
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    entries.forEach { (title, pair) -> RotationCard(title, pair) }
                }
            }
        }
    }
}

@Composable
private fun RotationCard(title: String, pair: RotationPair) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            pair.current?.let { m ->
                if (!m.asset.isNullOrBlank()) {
                    AsyncImage(
                        model = m.asset,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                    )
                }
                Text("当前地图：${m.map ?: "-"}${m.eventName?.let { " · $it" } ?: ""}")
                Text("剩余时间：${m.remainingTimer ?: "${m.remainingMins ?: 0} 分钟"}", color = Color(0xFFFFB703))
            }
            pair.next?.let { n ->
                Text("下一张：${n.map ?: "-"}   时长：${n.DurationInMinutes ?: "-"} 分钟")
            }
        }
    }
}
