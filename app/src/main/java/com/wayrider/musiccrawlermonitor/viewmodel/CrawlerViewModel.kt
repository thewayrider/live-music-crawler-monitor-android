package com.wayrider.musiccrawlermonitor.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wayrider.musiccrawlermonitor.data.GistRepository
import com.wayrider.musiccrawlermonitor.model.CrawlerAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CrawlerUiState {
    object Loading : CrawlerUiState
    data class Success(val data: CrawlerAnalytics, val isSampleMode: Boolean) : CrawlerUiState
    data class Error(val message: String) : CrawlerUiState
}

class CrawlerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GistRepository(application)

    private val _uiState = MutableStateFlow<CrawlerUiState>(CrawlerUiState.Loading)
    val uiState: StateFlow<CrawlerUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        _uiState.value = CrawlerUiState.Loading
        viewModelScope.launch {
            val result = repository.fetchMetrics()
            if (result.isSuccess) {
                _uiState.value = CrawlerUiState.Success(
                    data = result.getOrThrow(),
                    isSampleMode = repository.isSampleMode() || repository.getGistId().isEmpty()
                )
            } else {
                _uiState.value = CrawlerUiState.Error(
                    result.exceptionOrNull()?.message ?: "Unknown error fetching telemetry"
                )
            }
        }
    }

    fun getGistId(): String = repository.getGistId()
    fun getGithubToken(): String = repository.getGithubToken()
    fun isSampleMode(): Boolean = repository.isSampleMode()

    fun saveConfig(gistId: String, token: String, sampleMode: Boolean) {
        repository.saveConfig(gistId, token, sampleMode)
        loadData()
    }
}
