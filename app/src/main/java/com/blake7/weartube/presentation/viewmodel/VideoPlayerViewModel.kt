package com.blake7.weartube.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blake7.weartube.data.model.YouTubeVideo
import com.blake7.weartube.data.model.YouTubeVideoDetails
import com.blake7.weartube.data.model.CommentThread
import com.blake7.weartube.data.repository.YouTubeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VideoPlayerUiState(
    val video: YouTubeVideo? = null,
    val videoDetails: YouTubeVideoDetails? = null,
    val comments: List<CommentThread> = emptyList(),
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val isLoadingComments: Boolean = false,
    val error: String? = null,
    val streamUrl: String? = null
)

class VideoPlayerViewModel : ViewModel() {

    private val repository = YouTubeRepository()

    private val _uiState = MutableStateFlow(VideoPlayerUiState())
    val uiState: StateFlow<VideoPlayerUiState> = _uiState.asStateFlow()

    fun loadVideo(video: YouTubeVideo) {
        _uiState.value = _uiState.value.copy(
            video = video,
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            try {
                val streamUrl = repository.getVideoStreamUrl(video.videoId)
                _uiState.value = _uiState.value.copy(
                    streamUrl = streamUrl,
                    isLoading = false
                )

                // Load video details and comments
                loadVideoDetails(video.videoId)
                loadVideoComments(video.videoId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load video"
                )
            }
        }
    }

    private fun loadVideoDetails(videoId: String) {
        viewModelScope.launch {
            repository.getVideoDetails(videoId).fold(
                onSuccess = { videoDetails ->
                    _uiState.value = _uiState.value.copy(
                        videoDetails = videoDetails
                    )
                },
                onFailure = { error ->
                    // Don't update error state for details, just log it
                    println("Failed to load video details: ${error.message}")
                }
            )
        }
    }

    private fun loadVideoComments(videoId: String) {
        _uiState.value = _uiState.value.copy(isLoadingComments = true)

        viewModelScope.launch {
            repository.getVideoComments(videoId).fold(
                onSuccess = { (comments, _) ->
                    _uiState.value = _uiState.value.copy(
                        comments = comments,
                        isLoadingComments = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingComments = false
                    )
                    // Don't show error for comments, just log it
                    println("Failed to load comments: ${error.message}")
                }
            )
        }
    }

    fun playPause() {
        _uiState.value = _uiState.value.copy(
            isPlaying = !_uiState.value.isPlaying
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
