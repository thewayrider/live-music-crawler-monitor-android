package com.wayrider.musiccrawlermonitor.model

data class CrawlerAnalytics(
    val generatedAt: String,
    val summary: CrawlerSummary,
    val crawlers: List<CrawlerInfo>
)

data class CrawlerSummary(
    val totalCrawlers: Int,
    val systemNewToday: Int,
    val systemNewWeek: Int,
    val systemNewAllTime: Int,
    val systemTotalRuns: Int,
    val healthyCount: Int,
    val reviewCount: Int,
    val inactiveCount: Int
)

data class CrawlerInfo(
    val id: String,
    val name: String,
    val schedule: String,
    val status: String,
    val lastRun: String?,
    val lastRunFoundTotal: Int?,
    val lastRunNewAdded: Int?,
    val stats: CrawlerStats,
    val health: CrawlerHealth,
    val recentRunHistory: List<RecentRun>
)

data class CrawlerStats(
    val today: TimeRangeStat,
    val past7Days: TimeRangeStat,
    val past30Days: TimeRangeStat,
    val allTime: AllTimeStat
)

data class TimeRangeStat(
    val runs: Int,
    val newSongs: Int
)

data class AllTimeStat(
    val runs: Int,
    val newSongs: Int,
    val baselinePool: Int,
    val totalUniqueProcessed: Int
)

data class CrawlerHealth(
    val healthStatus: String,
    val badge: String, // "success", "warning", "danger", "info"
    val recommendation: String,
    val consecutiveZeros: Int,
    val daysSinceLastRun: Int
)

data class RecentRun(
    val date: String,
    val newSongs: Int
)
