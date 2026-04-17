package com.example.apltracker.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.apltracker.data.ApexPlatform
import com.russhwolf.settings.Settings

/** 当前选中的玩家（跨页面共享 + 持久化）。 */
class CurrentUser(private val settings: Settings) {
    private val keyName = "player_name"
    private val keyPlatform = "player_platform"

    var name: String by mutableStateOf(settings.getStringOrNull(keyName).orEmpty())
    var platform: ApexPlatform by mutableStateOf(
        runCatching {
            ApexPlatform.valueOf(
                settings.getStringOrNull(keyPlatform) ?: ApexPlatform.PC.name
            )
        }.getOrDefault(ApexPlatform.PC)
    )

    fun save(newName: String, newPlatform: ApexPlatform) {
        name = newName.trim()
        platform = newPlatform
        settings.putString(keyName, name)
        settings.putString(keyPlatform, newPlatform.name)
    }

    private fun Settings.getStringOrNull(key: String): String? =
        if (hasKey(key)) getString(key, "") else null
}

val LocalCurrentUser = compositionLocalOf<CurrentUser> {
    error("CurrentUser 未初始化")
}

@Composable
fun rememberCurrentUser(): CurrentUser {
    return remember { CurrentUser(Settings()) }
}
