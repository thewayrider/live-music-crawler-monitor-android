package com.wayrider.musiccrawlermonitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.wayrider.musiccrawlermonitor.ui.DashboardScreen
import com.wayrider.musiccrawlermonitor.ui.theme.BgPrimary
import com.wayrider.musiccrawlermonitor.ui.theme.CrawlerMonitorTheme
import com.wayrider.musiccrawlermonitor.viewmodel.CrawlerViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: CrawlerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CrawlerMonitorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BgPrimary
                ) {
                    DashboardScreen(viewModel = viewModel)
                }
            }
        }
    }
}
