package com.blake7.weartube.data.repository

import com.blake7.weartube.BuildConfig
import com.blake7.weartube.data.api.YouTubeApiService
import com.blake7.weartube.data.model.YouTubeVideo
import com.blake7.weartube.data.model.YouTubeVideoDetails
import com.blake7.weartube.data.model.CommentThread
import com.blake7.weartube.data.model.YouTubeChannel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class YouTubeRepository {

    // API key loaded securely from BuildConfig (which reads from local.properties)
    private val apiKey = BuildConfig.YOUTUBE_API_KEY
    private val baseUrl = "https://www.googleapis.com/youtube/v3/"

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC // Reduced logging for production
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(YouTubeApiService::class.java)

    suspend fun searchVideos(query: String, pageToken: String? = null): Result<Pair<List<YouTubeVideo>, String?>> {
        return try {
            val response = apiService.searchVideos(
                query = query,
                apiKey = apiKey,
                pageToken = pageToken,
                maxResults = 15
            )

            if (response.isSuccessful) {
                val body = response.body()
                val videos = body?.items ?: emptyList()
                val nextPageToken = body?.nextPageToken
                Result.success(Pair(videos, nextPageToken))
            } else {
                Result.failure(Exception("Search failed: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTrendingVideos(pageToken: String? = null): Result<Pair<List<YouTubeVideo>, String?>> {
        return try {
            // Use multiple popular categories for better trending content
            val popularQueries = listOf(
                "music 2024", "gaming highlights", "tech review", 
                "tutorial", "entertainment", "sports highlights",
                "movie trailer", "funny moments"
            )
            val randomQuery = popularQueries.random()

            val response = apiService.getTrendingVideos(
                query = randomQuery,
                apiKey = apiKey,
                pageToken = pageToken,
                maxResults = 20,
                order = "viewCount"
            )

            if (response.isSuccessful) {
                val body = response.body()
                val videos = body?.items ?: emptyList()
                val nextPageToken = body?.nextPageToken

                if (videos.isEmpty()) {
                    // Fallback: try a different popular query
                    return searchVideos("latest popular videos", pageToken)
                }

                Result.success(Pair(videos, nextPageToken))
            } else {
                // Fallback to general search if trending fails
                Result.failure(Exception("Failed to get trending videos: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            // Fallback to search if trending fails
            try {
                return searchVideos("popular videos", pageToken)
            } catch (_: Exception) {
                Result.failure(Exception("Both trending and fallback failed: ${e.message}"))
            }
        }
    }

    suspend fun getVideoDetails(videoId: String): YouTubeVideoDetails? {
        return try {
            val response = apiService.getVideoDetails(
                videoId = videoId,
                apiKey = apiKey,
                part = "snippet,statistics,contentDetails"
            )

            if (response.isSuccessful) {
                response.body()?.items?.firstOrNull()
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getVideoComments(videoId: String, pageToken: String? = null): List<CommentThread> {
        return try {
            val response = apiService.getVideoComments(
                videoId = videoId,
                apiKey = apiKey,
                pageToken = pageToken,
                maxResults = 15
            )

            if (response.isSuccessful) {
                response.body()?.items ?: emptyList()
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun getChannelDetails(channelId: String): YouTubeChannel? {
        return try {
            val response = apiService.getChannelDetails(
                channelId = channelId,
                apiKey = apiKey
            )

            if (response.isSuccessful) {
                response.body()?.items?.firstOrNull()
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Gets the optimal YouTube embed URL for in-app playback
     * This uses YouTube's official embedded player which is the recommended approach
     */
    fun getEmbedUrl(videoId: String, autoplay: Boolean = false): String {
        val baseUrl = "https://www.youtube.com/embed/$videoId"
        val params = mutableListOf<String>()

        // Essential parameters for embedded player
        params.add("autoplay=${if (autoplay) 1 else 0}")
        params.add("controls=1") // Show player controls
        params.add("modestbranding=1") // Minimal YouTube branding
        params.add("rel=0") // Don't show related videos
        params.add("enablejsapi=1") // Enable JavaScript API
        params.add("playsinline=1") // Play inline on mobile
        params.add("html5=1") // Force HTML5 player
        params.add("fs=1") // Enable fullscreen
        params.add("cc_load_policy=1") // Show captions if available

        return "$baseUrl?${params.joinToString("&")}"
    }

    /**
     * Parse video duration from ISO 8601 format (e.g., "PT4M13S" -> "4:13")
     */
    fun parseVideoDuration(duration: String?): String {
        if (duration == null) return "0:00"
        
        return try {
            val regex = Regex("PT(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?")
            val matchResult = regex.find(duration)
            
            if (matchResult != null) {
                val hours = matchResult.groupValues[1].toIntOrNull() ?: 0
                val minutes = matchResult.groupValues[2].toIntOrNull() ?: 0
                val seconds = matchResult.groupValues[3].toIntOrNull() ?: 0
                
                when {
                    hours > 0 -> "%d:%02d:%02d".format(hours, minutes, seconds)
                    else -> "%d:%02d".format(minutes, seconds)
                }
            } else {
                "0:00"
            }
        } catch (_: Exception) {
            "0:00"
        }
    }
}
