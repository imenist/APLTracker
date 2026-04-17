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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.apltracker.data.ApexRepository
import com.example.apltracker.data.MapRotationResponse
import com.example.apltracker.data.MapSlot
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
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp),
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MapSlotItem(
                    label = "当前",
                    labelColor = Color(0xFF4CAF50),
                    slot = pair.current,
                    modifier = Modifier.weight(1f),
                )
                MapSlotItem(
                    label = "下一张",
                    labelColor = Color(0xFFFFB703),
                    slot = pair.next,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun MapSlotItem(
    label: String,
    labelColor: Color,
    slot: MapSlot?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = labelColor,
        )

        val imageModifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(8.dp))

        if (slot == null) {
            Surface(
                modifier = imageModifier,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text("暂无", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else if (!slot.asset.isNullOrBlank()) {
            AsyncImage(
                model = slot.asset,
                contentDescription = slot.map,
                contentScale = ContentScale.Crop,
                modifier = imageModifier,
            )
        } else {
            Surface(
                modifier = imageModifier,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = slot.map ?: "-",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        if (slot != null) {
            Text(
                text = slot.map ?: "-",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            slot.eventName?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            }
            val detail = slot.remainingTimer
                ?: slot.remainingMins?.let { "$it 分钟" }
                ?: slot.DurationInMinutes?.let { "时长 $it 分钟" }
            if (!detail.isNullOrBlank()) {
                Text(detail, style = MaterialTheme.typography.bodySmall, color = Color(0xFFFFB703))
            }
        } else {
            Spacer(Modifier.height(2.dp))
        }
    }
}
