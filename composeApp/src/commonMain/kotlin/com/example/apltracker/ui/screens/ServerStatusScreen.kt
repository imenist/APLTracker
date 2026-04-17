package com.example.apltracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.apltracker.data.ApexRepository
import com.example.apltracker.data.ServerRegionStatus
import com.example.apltracker.data.ServersResponse
import com.example.apltracker.ui.ApexRed
import com.example.apltracker.ui.ErrorBanner
import com.example.apltracker.ui.LoadingBox
import com.example.apltracker.ui.UiState
import com.example.apltracker.ui.toErrorText
import kotlinx.serialization.json.JsonPrimitive

@Composable
fun ServerStatusScreen(repo: ApexRepository) {
    var state: UiState<ServersResponse> by remember { mutableStateOf(UiState.Loading) }
    LaunchedEffect(Unit) {
        state = runCatching { repo.getServerStatus() }.fold(
            onSuccess = { UiState.Success(it) },
            onFailure = { UiState.Error(it.toErrorText()) },
        )
    }
    when (val s = state) {
        UiState.Idle, UiState.Loading -> LoadingBox()
        is UiState.Error -> ErrorBanner(s.message, Modifier.fillMaxSize())
        is UiState.Success -> {
            val groups = buildList {
                s.data.originLogin?.let { add("Origin Login" to it) }
                s.data.eaNovafusion?.let { add("EA Novafusion" to it) }
                s.data.eaAccounts?.let { add("EA Accounts" to it) }
                s.data.apexOauthCrossplay?.let { add("Apex OAuth Crossplay" to it) }
                s.data.selfCoreTest?.let { add("Self Core Test" to it) }
            }
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                groups.forEach { (name, regions) -> ServerGroupCard(name, regions) }
                s.data.otherPlatforms?.forEach { (platform, regions) ->
                    ServerGroupCard("其它平台：$platform", regions)
                }
            }
        }
    }
}

@Composable
private fun ServerGroupCard(title: String, regions: Map<String, ServerRegionStatus>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            regions.forEach { (region, status) ->
                val rt = (status.responseTime as? JsonPrimitive)?.content ?: "-"
                val isUp = status.status.equals("UP", ignoreCase = true)
                Text(
                    text = "• $region：${status.status ?: "-"}   延迟：${rt}ms",
                    color = if (isUp) Color(0xFF7AD67A) else ApexRed,
                )
            }
        }
    }
}
