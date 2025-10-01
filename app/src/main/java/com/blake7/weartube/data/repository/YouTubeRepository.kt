package com.blake7.weartube.data.repository

import com.blake7.weartube.data.api.YouTubeApiService
import com.blake7.weartube.data.model.YouTubeVideo
import com.blake7.weartube.data.model.YouTubeVideoDetails
import com.blake7.weartube.data.model.CommentThread
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class YouTubeRepository {

    // Using the provided API key
    private val apiKey = "AIzaSyAbRfcxTPINBY50WSpWW29wgGlUqv7sj1c"
    private val baseUrl = "https://www.googleapis.com/youtube/v3/"

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
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
                pageToken = pageToken
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
            // Try multiple popular search terms to get interesting content
            val popularQueries = listOf("music", "gaming", "tech", "tutorial", "funny")
            val randomQuery = popularQueries.random()

            val response = apiService.getTrendingVideos(
                query = randomQuery,
                apiKey = apiKey,
                pageToken = pageToken
            )

            if (response.isSuccessful) {
                val body = response.body()
                val videos = body?.items ?: emptyList()
                val nextPageToken = body?.nextPageToken

                if (videos.isEmpty()) {
                    // Fallback: try a simple search for recent videos
                    return searchVideos("latest videos", pageToken)
                }

                Result.success(Pair(videos, nextPageToken))
            } else {
                // Fallback to search if trending fails
                Result.failure(Exception("Failed to get trending videos: ${response.code()} - ${response.message()}. Trying fallback..."))
            }
        } catch (e: Exception) {
            // Fallback to search if trending fails
            try {
                return searchVideos("popular", pageToken)
            } catch (fallbackException: Exception) {
                Result.failure(Exception("Both trending and fallback failed: ${e.message}"))
            }
        }
    }

    suspend fun getVideoDetails(videoId: String): Result<YouTubeVideoDetails?> {
        return try {
            val response = apiService.getVideoDetails(
                videoId = videoId,
                apiKey = apiKey
            )

            if (response.isSuccessful) {
                val videoDetails = response.body()?.items?.firstOrNull()
                Result.success(videoDetails)
            } else {
                Result.failure(Exception("Failed to get video details: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVideoComments(videoId: String, pageToken: String? = null): Result<Pair<List<CommentThread>, String?>> {
        return try {
            val response = apiService.getVideoComments(
                videoId = videoId,
                apiKey = apiKey,
                pageToken = pageToken
            )

            if (response.isSuccessful) {
                val body = response.body()
                val comments = body?.items ?: emptyList()
                val nextPageToken = body?.nextPageToken
                Result.success(Pair(comments, nextPageToken))
            } else {
                Result.failure(Exception("Failed to get comments: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getVideoStreamUrl(videoId: String): String {
        // For a real implementation, you'd need to use YouTube's player API
        // or a library like youtube-dl to get actual stream URLs
        return "https://www.youtube.com/watch?v=$videoId"
    }
}
