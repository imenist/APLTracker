package com.example.apltracker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.apltracker.data.ApexRepository
import com.example.apltracker.ui.ApexTheme
import com.example.apltracker.ui.LocalCurrentUser
import com.example.apltracker.ui.PlayerInputDialog
import com.example.apltracker.ui.Screen
import com.example.apltracker.ui.rememberCurrentUser
import com.example.apltracker.ui.screens.HomeScreen
import com.example.apltracker.ui.screens.MapRotationScreen
import com.example.apltracker.ui.screens.MatchHistoryScreen
import com.example.apltracker.ui.screens.OriginScreen
import com.example.apltracker.ui.screens.PlayerStatsScreen
import com.example.apltracker.ui.screens.ServerStatusScreen
import com.example.apltracker.ui.screens.StoreScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    ApexTheme {
        val repo = remember { ApexRepository() }
        DisposableEffect(Unit) { onDispose { repo.close() } }

        val currentUser = rememberCurrentUser()
        var currentScreen by remember { mutableStateOf(Screen.Home) }
        var pendingScreen by remember { mutableStateOf<Screen?>(null) }
        var showUserDialog by remember { mutableStateOf(false) }

        CompositionLocalProvider(LocalCurrentUser provides currentUser) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Scaffold(
                    modifier = Modifier.safeContentPadding(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Text(
                                        text = currentScreen.title,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    val subtitle = if (currentUser.name.isBlank()) "未设置玩家 · 点击右上角"
                                    else "${currentUser.name} · ${currentUser.platform.label}"
                                    Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            },
                            navigationIcon = {
                                if (currentScreen != Screen.Home) {
                                    IconButton(onClick = { currentScreen = Screen.Home }) {
                                        Text("‹", style = MaterialTheme.typography.headlineMedium,
                                            color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            },
                            actions = {
                                IconButton(onClick = { showUserDialog = true }) {
                                    Text("👤")
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                            ),
                        )
                    },
                ) { inner ->
                    Box(modifier = Modifier.fillMaxSize().padding(inner)) {
                        when (currentScreen) {
                            Screen.Home -> HomeScreen(onNavigate = { target ->
                                if (target.needsPlayer && currentUser.name.isBlank()) {
                                    pendingScreen = target
                                    showUserDialog = true
                                } else {
                                    currentScreen = target
                                }
                            })
                            Screen.PlayerStats -> PlayerStatsScreen(repo)
                            Screen.MatchHistory -> MatchHistoryScreen(repo)
                            Screen.ServerStatus -> ServerStatusScreen(repo)
                            Screen.MapRotation -> MapRotationScreen(repo)
                            Screen.Store -> StoreScreen(repo)
                            Screen.OriginUID -> OriginScreen(repo)
                        }
                    }
                }

                if (showUserDialog) {
                    PlayerInputDialog(
                        initialName = currentUser.name,
                        initialPlatform = currentUser.platform,
                        onConfirm = { name, platform ->
                            currentUser.save(name, platform)
                            showUserDialog = false
                            pendingScreen?.let {
                                currentScreen = it
                                pendingScreen = null
                            }
                        },
                        onDismiss = {
                            showUserDialog = false
                            pendingScreen = null
                        },
                    )
                }
            }
        }
    }
}
