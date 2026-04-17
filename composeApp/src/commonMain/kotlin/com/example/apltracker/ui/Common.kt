package com.example.apltracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.apltracker.data.ApexApiException
import kotlinx.coroutines.delay

/**
 * 通用的红色错误文字（居中）；带「复制」便于粘贴到电脑检索。
 * LocalClipboardManager 在 CMP 中仍由各平台实现；新 LocalClipboard 需 ClipEntry 构造，应用层暂不迁移。
 */
@Suppress("DEPRECATION")
@Composable
fun ErrorBanner(message: String, modifier: Modifier = Modifier) {
    val clipboard = LocalClipboardManager.current
    var copiedHint by remember { mutableStateOf(false) }
    LaunchedEffect(copiedHint) {
        if (copiedHint) {
            delay(2200)
            copiedHint = false
        }
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
            )
            FilledTonalButton(
                onClick = {
                    clipboard.setText(AnnotatedString(message))
                    copiedHint = true
                },
            ) {
                Text("复制错误信息")
            }
            if (copiedHint) {
                Text(
                    text = "已复制到剪贴板",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            Text("加载中…", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** 统一表示一次异步请求的 UI 状态。 */
sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

/** 把任何异常转成给用户看的字符串（保留完整错误信息）。 */
fun Throwable.toErrorText(): String {
    // ALS 服务端返回的业务错误（例如端点下线），直接展示原文更友好
    if (this is ApexApiException) return message ?: "接口返回错误"
    val cause = this::class.simpleName ?: "Error"
    val msg = message ?: toString()
    return "$cause: $msg"
}
