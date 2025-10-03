package com.blake7.weartube.data.api

import com.blake7.weartube.data.model.YouTubeSearchResponse
import com.blake7.weartube.data.model.YouTubeVideoDetailsResponse
import com.blake7.weartube.data.model.YouTubeCommentsResponse
import com.blake7.weartube.data.model.YouTubeChannelsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApiService {

    @GET("search")
    suspend fun searchVideos(
        @Query("part") part: String = "snippet",
        @Query("type") type: String = "video",
        @Query("q") query: String,
        @Query("maxResults") maxResults: Int = 10,
        @Query("key") apiKey: String,
        @Query("pageToken") pageToken: String? = null,
        @Query("order") order: String = "relevance",
        @Query("regionCode") regionCode: String = "US",
        @Query("safeSearch") safeSearch: String = "moderate"
    ): Response<YouTubeSearchResponse>

    // Get popular/trending videos using search with better parameters
    @GET("search")
    suspend fun getTrendingVideos(
        @Query("part") part: String = "snippet",
        @Query("type") type: String = "video",
        @Query("q") query: String = "trending music gaming technology",
        @Query("order") order: String = "viewCount",
        @Query("publishedAfter") publishedAfter: String? = null,
        @Query("maxResults") maxResults: Int = 20,
        @Query("key") apiKey: String,
        @Query("pageToken") pageToken: String? = null,
        @Query("regionCode") regionCode: String = "US",
        @Query("safeSearch") safeSearch: String = "moderate"
    ): Response<YouTubeSearchResponse>

    // Get videos by category - kept for potential future use
    @Suppress("unused")
    @GET("search")
    suspend fun getVideosByCategory(
        @Query("part") part: String = "snippet",
        @Query("type") type: String = "video",
        @Query("q") query: String,
        @Query("order") order: String = "relevance",
        @Query("maxResults") maxResults: Int = 10,
        @Query("key") apiKey: String,
        @Query("pageToken") pageToken: String? = null,
        @Query("regionCode") regionCode: String = "US"
    ): Response<YouTubeSearchResponse>

    @GET("videos")
    suspend fun getVideoDetails(
        @Query("part") part: String = "snippet,statistics,contentDetails",
        @Query("id") videoId: String,
        @Query("key") apiKey: String
    ): Response<YouTubeVideoDetailsResponse>

    @GET("commentThreads")
    suspend fun getVideoComments(
        @Query("part") part: String = "snippet",
        @Query("videoId") videoId: String,
        @Query("maxResults") maxResults: Int = 10,
        @Query("order") order: String = "relevance",
        @Query("key") apiKey: String,
        @Query("pageToken") pageToken: String? = null
    ): Response<YouTubeCommentsResponse>

    // Get channel details
    @GET("channels")
    suspend fun getChannelDetails(
        @Query("part") part: String = "snippet,statistics",
        @Query("id") channelId: String,
        @Query("key") apiKey: String
    ): Response<YouTubeChannelsResponse>

    // Get related videos (using search with channel or similar keywords)
    // Note: Kept for potential future use
    @Suppress("unused")
    @GET("search")
    suspend fun getRelatedVideos(
        @Query("part") part: String = "snippet",
        @Query("type") type: String = "video",
        @Query("relatedToVideoId") relatedToVideoId: String,
        @Query("maxResults") maxResults: Int = 5,
        @Query("key") apiKey: String
    ): Response<YouTubeSearchResponse>
}
