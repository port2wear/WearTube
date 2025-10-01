package com.blake7.weartube.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blake7.weartube.data.model.YouTubeVideo
import com.blake7.weartube.data.repository.YouTubeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val trendingVideos: List<YouTubeVideo> = emptyList(),
    val searchResults: List<YouTubeVideo> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null,
    val nextPageToken: String? = null,
    val isSearchMode: Boolean = false
)

class HomeViewModel : ViewModel() {

    private val repository = YouTubeRepository()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadTrendingVideos()
    }

    fun loadTrendingVideos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.getTrendingVideos().fold(
                onSuccess = { (videos, nextPageToken) ->
                    if (videos.isEmpty()) {
                        // If no trending videos, try a fallback search
                        searchVideos("popular videos")
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            trendingVideos = videos,
                            nextPageToken = nextPageToken,
                            isSearchMode = false
                        )
                    }
                },
                onFailure = { error ->
                    // Try fallback search on error
                    searchVideos("latest videos")
                }
            )
        }
    }

    fun searchVideos(query: String) {
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                searchQuery = "",
                searchResults = emptyList(),
                isSearchMode = false
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                searchQuery = query,
                isSearchMode = true
            )

            repository.searchVideos(query).fold(
                onSuccess = { (videos, nextPageToken) ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        searchResults = videos,
                        nextPageToken = nextPageToken
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Search failed"
                    )
                }
            )
        }
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            searchResults = emptyList(),
            isSearchMode = false,
            error = null
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
