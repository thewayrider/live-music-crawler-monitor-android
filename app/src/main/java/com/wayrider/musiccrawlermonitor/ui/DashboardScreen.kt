package com.wayrider.musiccrawlermonitor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wayrider.musiccrawlermonitor.model.*
import com.wayrider.musiccrawlermonitor.ui.theme.*
import com.wayrider.musiccrawlermonitor.viewmodel.CrawlerUiState
import com.wayrider.musiccrawlermonitor.viewmodel.CrawlerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: CrawlerViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showSettings by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Music Crawler Monitor",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = AccentCyan
                        )
                        Text(
                            "Live Telemetry & Diagnostics",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TextMain)
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextMain)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgPrimary
                )
            )
        },
        containerColor = BgPrimary
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val state = uiState) {
                is CrawlerUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentCyan)
                    }
                }
                is CrawlerUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Failed to load metrics",
                            style = MaterialTheme.typography.titleMedium,
                            color = DangerRed
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            state.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(onClick = { viewModel.loadData() }) {
                                Text("Retry")
                            }
                            OutlinedButton(onClick = {
                                viewModel.saveConfig(
                                    viewModel.getGistId(),
                                    viewModel.getGithubToken(),
                                    sampleMode = true
                                )
                            }) {
                                Text("Use Sample Data")
                            }
                        }
                    }
                }
                is CrawlerUiState.Success -> {
                    DashboardContent(
                        analytics = state.data,
                        isSample = state.isSampleMode
                    )
                }
            }
        }
    }

    if (showSettings) {
        SettingsDialog(
            initialGistId = viewModel.getGistId(),
            initialToken = viewModel.getGithubToken(),
            initialSampleMode = viewModel.isSampleMode(),
            onDismiss = { showSettings = false },
            onSave = { gistId, token, sampleMode ->
                viewModel.saveConfig(gistId, token, sampleMode)
                showSettings = false
            }
        )
    }
}

@Composable
fun DashboardContent(analytics: CrawlerAnalytics, isSample: Boolean) {
    val summary = analytics.summary
    val reviewList = analytics.crawlers.filter { it.health.badge == "warning" || it.health.badge == "danger" }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isSample) {
            item {
                Surface(
                    color = AccentCyan.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Preview Mode: Showing local/sample metrics. Tap settings ⚙ to configure your GitHub Gist ID.",
                        color = AccentCyan,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // Summary Metric Cards Grid
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard(
                    label = "Past 7 Days",
                    value = "${summary.systemNewWeek}",
                    sub = "new tracks",
                    color = AccentCyan,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "All Time",
                    value = "${summary.systemNewAllTime}",
                    sub = "${summary.systemTotalRuns} runs",
                    color = TextMain,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "Review",
                    value = "${summary.reviewCount}",
                    sub = "0-yield crawlers",
                    color = if (summary.reviewCount > 0) WarningAmber else SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Action Required Banner
        if (reviewList.isNotEmpty()) {
            item {
                Surface(
                    color = WarningAmber.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Action Recommended (${reviewList.size} non-performing)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = WarningAmber
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        reviewList.forEach { c ->
                            Text(
                                "• ${c.name}: ${c.health.recommendation}",
                                fontSize = 12.sp,
                                color = TextMain
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                "Active Crawlers (${analytics.crawlers.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = TextMuted,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Crawlers List
        items(analytics.crawlers) { crawler ->
            CrawlerCard(crawler)
        }
    }
}

@Composable
fun MetricCard(label: String, value: String, sub: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = BgCard,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
            Text(sub, fontSize = 10.sp, color = TextMuted)
        }
    }
}

@Composable
fun CrawlerCard(crawler: CrawlerInfo) {
    val isWarning = crawler.health.badge == "warning" || crawler.health.badge == "danger"
    val badgeColor = when (crawler.health.badge) {
        "success" -> SuccessGreen
        "warning" -> WarningAmber
        "danger" -> DangerRed
        else -> TextMuted
    }

    Surface(
        color = BgCard,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isWarning) WarningAmber.copy(alpha = 0.4f) else BorderColor
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(crawler.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextMain)
                    Text("Schedule: ${crawler.schedule}", fontSize = 12.sp, color = TextMuted)
                }

                Surface(
                    color = badgeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        crawler.health.healthStatus,
                        color = badgeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stats row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgPrimary.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatColumn(label = "Today", count = crawler.stats.today.newSongs, runs = crawler.stats.today.runs)
                StatColumn(label = "7 Days", count = crawler.stats.past7Days.newSongs, runs = crawler.stats.past7Days.runs)
                StatColumn(label = "All Time", count = crawler.stats.allTime.newSongs, runs = crawler.stats.allTime.runs)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                crawler.health.recommendation,
                fontSize = 12.sp,
                color = if (isWarning) WarningAmber else TextMuted,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun StatColumn(label: String, count: Int, runs: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = TextMuted)
        Text("+$count", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (count > 0) SuccessGreen else TextMain)
        Text("($runs runs)", fontSize = 9.sp, color = TextMuted)
    }
}
