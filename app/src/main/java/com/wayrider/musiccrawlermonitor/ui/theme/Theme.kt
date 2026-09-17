package com.wayrider.musiccrawlermonitor.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AccentCyan,
    secondary = SuccessGreen,
    tertiary = WarningAmber,
    background = BgPrimary,
    surface = BgCard,
    onPrimary = BgPrimary,
    onSecondary = BgPrimary,
    onBackground = TextMain,
    onSurface = TextMain
)

@Composable
fun CrawlerMonitorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
