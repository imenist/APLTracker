package com.example.apltracker.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val ApexRed = Color(0xFFDA292A)
private val ApexRedDark = Color(0xFF8E1F20)
private val BgBlack = Color(0xFF0E0E10)
private val Surface1 = Color(0xFF17171A)
private val Surface2 = Color(0xFF1F1F23)
private val OnSurfaceHigh = Color(0xFFEDEDED)
private val OnSurfaceMid = Color(0xFFB5B5B5)

private val ApexDarkScheme = darkColorScheme(
    primary = ApexRed,
    onPrimary = Color.White,
    primaryContainer = ApexRedDark,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFFFB703),
    onSecondary = Color.Black,
    background = BgBlack,
    onBackground = OnSurfaceHigh,
    surface = Surface1,
    onSurface = OnSurfaceHigh,
    surfaceVariant = Surface2,
    onSurfaceVariant = OnSurfaceMid,
    error = ApexRed,
    onError = Color.White,
    outline = Color(0xFF3A3A3F),
)

@Composable
fun ApexTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ApexDarkScheme,
        content = content,
    )
}
