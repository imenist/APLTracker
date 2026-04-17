package com.example.apltracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.example.apltracker.data.OriginResponse
import com.example.apltracker.ui.ErrorBanner
import com.example.apltracker.ui.LoadingBox
import com.example.apltracker.ui.LocalCurrentUser
import com.example.apltracker.ui.UiState
import com.example.apltracker.ui.toErrorText

@Composable
fun OriginScreen(repo: ApexRepository) {
    val user = LocalCurrentUser.current
    var state: UiState<OriginResponse> by remember { mutableStateOf(UiState.Loading) }

    LaunchedEffect(user.name) {
        if (user.name.isBlank()) {
            state = UiState.Error("未设置玩家：请在顶部设置当前玩家后再进入此页面。")
            return@LaunchedEffect
        }
        state = UiState.Loading
        state = runCatching { repo.getOriginUID(user.name) }
            .fold(
                onSuccess = { resp ->
                    if (resp.error != null) UiState.Error("API 错误: ${resp.error}") else UiState.Success(resp)
                },
                onFailure = { UiState.Error(it.toErrorText()) },
            )
    }

    when (val s = state) {
        UiState.Idle, UiState.Loading -> LoadingBox()
        is UiState.Error -> ErrorBanner(s.message, Modifier.fillMaxSize())
        is UiState.Success -> OriginContent(s.data)
    }
}

@Composable
private fun OriginContent(data: OriginResponse) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!data.avatar.isNullOrBlank()) {
                    AsyncImage(
                        model = data.avatar,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                    )
                    Spacer(Modifier.size(12.dp))
                }
                Column {
                    Text(data.name ?: "未知玩家", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("UID: ${data.uid ?: "-"}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("PID: ${data.pid ?: "-"}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
