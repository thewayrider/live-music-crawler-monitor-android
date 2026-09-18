package com.wayrider.musiccrawlermonitor.data

import android.content.Context
import com.wayrider.musiccrawlermonitor.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class GistRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("crawler_monitor_prefs", Context.MODE_PRIVATE)

    fun getGistId(): String = prefs.getString("gist_id", "9d9f324ab82907243f576f71ca001523") ?: "9d9f324ab82907243f576f71ca001523"
    fun getGithubToken(): String = prefs.getString("github_token", "") ?: ""
    fun isSampleMode(): Boolean = prefs.getBoolean("sample_mode", false)

    fun saveConfig(gistId: String, token: String, sampleMode: Boolean) {
        prefs.edit()
            .putString("gist_id", gistId.trim())
            .putString("github_token", token.trim())
            .putBoolean("sample_mode", sampleMode)
            .apply()
    }

    suspend fun fetchMetrics(): Result<CrawlerAnalytics> = withContext(Dispatchers.IO) {
        val gistId = getGistId()
        val token = getGithubToken()
        val sampleMode = isSampleMode()

        if (sampleMode || gistId.isEmpty()) {
            return@withContext Result.success(getSampleMetrics())
        }

        try {
            val url = URL("https://api.github.com/gists/$gistId")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "LiveMusicCrawlerMonitor-Android")
                if (token.isNotEmpty()) {
                    setRequestProperty("Authorization", "Bearer $token")
                }
                connectTimeout = 10000
                readTimeout = 10000
            }

            val responseCode = conn.responseCode
            if (responseCode !in 200..299) {
                val errorStream = conn.errorStream?.let { BufferedReader(InputStreamReader(it)).readText() } ?: ""
                return@withContext Result.failure(Exception("GitHub API error ($responseCode): $errorStream"))
            }

            val rawJson = BufferedReader(InputStreamReader(conn.inputStream)).readText()
            val gistObj = JSONObject(rawJson)
            val filesObj = gistObj.getJSONObject("files")

            val metricsFile = if (filesObj.has("crawler_metrics.json")) {
                filesObj.getJSONObject("crawler_metrics.json")
            } else {
                // Pick first file if name differs
                val firstKey = filesObj.keys().next()
                filesObj.getJSONObject(firstKey)
            }

            val contentStr = metricsFile.getString("content")
            val parsed = parseJsonToAnalytics(contentStr)
            Result.success(parsed)
        } catch (e: java.net.UnknownHostException) {
            Result.failure(Exception("Cannot reach the internet. Please verify your Wi-Fi or mobile data connection."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseJsonToAnalytics(jsonString: String): CrawlerAnalytics {
        val root = JSONObject(jsonString)
        val genAt = root.optString("generatedAt", "")

        val sumObj = root.getJSONObject("summary")
        val summary = CrawlerSummary(
            totalCrawlers = sumObj.optInt("totalCrawlers", 10),
            systemNewToday = sumObj.optInt("systemNewToday", 0),
            systemNewWeek = sumObj.optInt("systemNewWeek", 0),
            systemNewAllTime = sumObj.optInt("systemNewAllTime", 0),
            systemTotalRuns = sumObj.optInt("systemTotalRuns", 0),
            healthyCount = sumObj.optInt("healthyCount", 0),
            reviewCount = sumObj.optInt("reviewCount", 0),
            inactiveCount = sumObj.optInt("inactiveCount", 0)
        )

        val crawlersList = mutableListOf<CrawlerInfo>()
        val arr = root.getJSONArray("crawlers")
        for (i in 0 until arr.length()) {
            val c = arr.getJSONObject(i)
            val statsObj = c.getJSONObject("stats")
            val todayObj = statsObj.getJSONObject("today")
            val weekObj = statsObj.getJSONObject("past7Days")
            val monthObj = statsObj.getJSONObject("past30Days")
            val allTimeObj = statsObj.getJSONObject("allTime")

            val healthObj = c.getJSONObject("health")

            val historyList = mutableListOf<RecentRun>()
            if (c.has("recentRunHistory")) {
                val histArr = c.getJSONArray("recentRunHistory")
                for (j in 0 until histArr.length()) {
                    val h = histArr.getJSONObject(j)
                    historyList.add(RecentRun(h.optString("date", ""), h.optInt("newSongs", 0)))
                }
            }

            crawlersList.add(
                CrawlerInfo(
                    id = c.optString("id", ""),
                    name = c.optString("name", ""),
                    schedule = c.optString("schedule", ""),
                    status = c.optString("status", "Success"),
                    lastRun = if (c.isNull("lastRun")) null else c.optString("lastRun"),
                    lastRunFoundTotal = if (c.isNull("lastRunFoundTotal")) null else c.optInt("lastRunFoundTotal"),
                    lastRunNewAdded = if (c.isNull("lastRunNewAdded")) null else c.optInt("lastRunNewAdded"),
                    stats = CrawlerStats(
                        today = TimeRangeStat(todayObj.optInt("runs", 0), todayObj.optInt("newSongs", 0)),
                        past7Days = TimeRangeStat(weekObj.optInt("runs", 0), weekObj.optInt("newSongs", 0)),
                        past30Days = TimeRangeStat(monthObj.optInt("runs", 0), monthObj.optInt("newSongs", 0)),
                        allTime = AllTimeStat(
                            runs = allTimeObj.optInt("runs", 0),
                            newSongs = allTimeObj.optInt("newSongs", 0),
                            baselinePool = allTimeObj.optInt("baselinePool", 0),
                            totalUniqueProcessed = allTimeObj.optInt("totalUniqueProcessed", 0)
                        )
                    ),
                    health = CrawlerHealth(
                        healthStatus = healthObj.optString("healthStatus", "Healthy"),
                        badge = healthObj.optString("badge", "success"),
                        recommendation = healthObj.optString("recommendation", ""),
                        consecutiveZeros = healthObj.optInt("consecutiveZeros", 0),
                        daysSinceLastRun = healthObj.optInt("daysSinceLastRun", 0)
                    ),
                    recentRunHistory = historyList
                )
            )
        }

        return CrawlerAnalytics(genAt, summary, crawlersList)
    }

    private fun getSampleMetrics(): CrawlerAnalytics {
        return CrawlerAnalytics(
            generatedAt = "2026-09-17T14:03:26.862Z",
            summary = CrawlerSummary(
                totalCrawlers = 10,
                systemNewToday = 0,
                systemNewWeek = 161,
                systemNewAllTime = 343,
                systemTotalRuns = 51,
                healthyCount = 6,
                reviewCount = 2,
                inactiveCount = 0
            ),
            crawlers = listOf(
                createSampleCrawler("air_charts_discovery", "Air Charts", "Mon 16:00", "High Yield", "success", "Added 21 new tracks in past 7 days.", 21, 42),
                createSampleCrawler("acid_stag_discovery", "Acid Stag", "Fri 16:00", "High Yield", "success", "Added 15 new tracks in past 7 days.", 15, 36),
                createSampleCrawler("amrap_indie_discovery", "Amrap", "Thu 16:00", "Moderate", "info", "Operating normally with intermittent releases.", 0, 7),
                createSampleCrawler("bandcamp_indie_discovery", "Bandcamp", "Mon, Wed, Fri, Sat 09:00", "High Yield", "success", "Added 22 new tracks in past 7 days.", 22, 63),
                createSampleCrawler("listenbrainz_indie_discovery", "ListenBrainz", "Mon, Fri 09:00", "High Yield", "success", "Top discovery source! Added 30 new tracks.", 30, 78),
                createSampleCrawler("musicbrainz_indie_discovery", "MusicBrainz", "Manual", "Moderate", "info", "Manual runs operating normally.", 1, 3),
                createSampleCrawler("futuremag_indie_discovery", "Futuremag", "Fri 09:00", "Moderate", "info", "Operating normally on weekly cycle.", 0, 46),
                createSampleCrawler("roots_mag", "Roots Mag", "Fri 09:00", "Moderate", "info", "Operating normally on weekly cycle.", 0, 18),
                createSampleCrawler("triple_j_hitlist_discovery", "Triple J", "Mon, Wed, Fri 12:00", "Needs Review", "warning", "Yielded 0 new songs across last 5 runs. Consider adjusting filters or eliminating.", 0, 0, 5),
                createSampleCrawler("triple_j_unearthed_discovery", "Triple J Unearthed", "Mon, Wed, Fri 14:00", "Needs Review", "warning", "Yielded 0 new songs across last 3 runs. Consider adjusting filters or eliminating.", 0, 0, 3)
            )
        )
    }

    private fun createSampleCrawler(
        id: String,
        name: String,
        schedule: String,
        healthStatus: String,
        badge: String,
        recommendation: String,
        weekNew: Int,
        allTimeNew: Int,
        consecutiveZeros: Int = 0
    ): CrawlerInfo {
        return CrawlerInfo(
            id = id,
            name = name,
            schedule = schedule,
            status = "Success",
            lastRun = "2026-09-16T12:00:18Z",
            lastRunFoundTotal = 25,
            lastRunNewAdded = if (consecutiveZeros > 0) 0 else weekNew / 2,
            stats = CrawlerStats(
                today = TimeRangeStat(0, 0),
                past7Days = TimeRangeStat(3, weekNew),
                past30Days = TimeRangeStat(10, allTimeNew),
                allTime = AllTimeStat(12, allTimeNew, 30, 30 + allTimeNew)
            ),
            health = CrawlerHealth(
                healthStatus = healthStatus,
                badge = badge,
                recommendation = recommendation,
                consecutiveZeros = consecutiveZeros,
                daysSinceLastRun = 1
            ),
            recentRunHistory = emptyList()
        )
    }
}
