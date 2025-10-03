package com.blake7.weartube.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.blake7.weartube.data.model.YouTubeVideo
import com.blake7.weartube.data.model.YouTubeVideoDetails
import com.blake7.weartube.data.model.CommentThread
import com.blake7.weartube.data.model.YouTubeChannel
import com.blake7.weartube.data.repository.YouTubeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VideoPlayerUiState(
    val isLoading: Boolean = false,
    val videoDetails: YouTubeVideoDetails? = null,
    val comments: List<CommentThread> = emptyList(),
    val isLoadingComments: Boolean = false,
    val channelDetails: YouTubeChannel? = null,
    val embedUrl: String? = null,
    val videoDuration: String? = null,
    val error: String? = null
)

class VideoPlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = YouTubeRepository()

    private val _uiState = MutableStateFlow(VideoPlayerUiState())
    val uiState: StateFlow<VideoPlayerUiState> = _uiState.asStateFlow()

    fun loadVideo(video: YouTubeVideo) {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            try {
                // Generate the embed URL immediately for faster loading
                val embedUrl = repository.getEmbedUrl(video.videoId, autoplay = false)
                _uiState.value = _uiState.value.copy(embedUrl = embedUrl)

                // Load additional video data concurrently
                launch { loadVideoDetails(video.videoId) }
                launch { loadVideoComments(video.videoId) }

                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load video: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private suspend fun loadVideoDetails(videoId: String) {
        try {
            val details = repository.getVideoDetails(videoId)
            _uiState.value = _uiState.value.copy(
                videoDetails = details,
                videoDuration = repository.parseVideoDuration(details?.contentDetails?.duration)
            )

            // Load channel details if we have the channel ID
            details?.snippet?.channelId?.let { channelId ->
                loadChannelDetails(channelId)
            }
        } catch (_: Exception) {
            // Video details loading failed, but don't show error for this
        }
    }

    private suspend fun loadChannelDetails(channelId: String) {
        try {
            val channel = repository.getChannelDetails(channelId)
            _uiState.value = _uiState.value.copy(
                channelDetails = channel
            )
        } catch (_: Exception) {
            // Channel details loading failed, but don't show error for this
        }
    }

    private suspend fun loadVideoComments(videoId: String) {
        _uiState.value = _uiState.value.copy(isLoadingComments = true)
        try {
            val comments = repository.getVideoComments(videoId)
            _uiState.value = _uiState.value.copy(
                comments = comments,
                isLoadingComments = false
            )
        } catch (_: Exception) {
            // Comments loading failed, but don't show error for this
            _uiState.value = _uiState.value.copy(isLoadingComments = false)
        }
    }
}
