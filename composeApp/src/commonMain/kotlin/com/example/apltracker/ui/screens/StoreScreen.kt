package com.example.apltracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.apltracker.data.ApexRepository
import com.example.apltracker.data.StoreItem
import com.example.apltracker.ui.ErrorBanner
import com.example.apltracker.ui.LoadingBox
import com.example.apltracker.ui.UiState
import com.example.apltracker.ui.toErrorText

@Composable
fun StoreScreen(repo: ApexRepository) {
    var state: UiState<List<StoreItem>> by remember { mutableStateOf(UiState.Loading) }
    LaunchedEffect(Unit) {
        state = runCatching { repo.getStore() }.fold(
            onSuccess = { UiState.Success(it) },
            onFailure = { UiState.Error(it.toErrorText()) },
        )
    }
    when (val s = state) {
        UiState.Idle, UiState.Loading -> LoadingBox()
        is UiState.Error -> ErrorBanner(s.message, Modifier.fillMaxSize())
        is UiState.Success -> {
            if (s.data.isEmpty()) {
                ErrorBanner("商店暂无数据。", Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(s.data) { item -> StoreItemCard(item) }
                }
            }
        }
    }
}

@Composable
private fun StoreItemCard(item: StoreItem) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            val image = item.assets?.large ?: item.assets?.small
            if (!image.isNullOrBlank()) {
                AsyncImage(
                    model = image,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                )
            }
            Text(
                text = item.title ?: item.bundle ?: item.name ?: "未命名",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            item.bundleType?.let { Text("类型：$it") }
            item.pricing?.forEach { p ->
                Text("价格：${p.cost ?: 0} ${p.currency ?: ""}")
            }
            item.expiration?.remaining?.shortFormat?.let {
                Text("剩余：$it", color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}
