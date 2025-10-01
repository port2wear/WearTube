package com.blake7.weartube.data.api

import com.blake7.weartube.data.model.YouTubeSearchResponse
import com.blake7.weartube.data.model.YouTubeVideoDetailsResponse
import com.blake7.weartube.data.model.YouTubeCommentsResponse
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
        @Query("pageToken") pageToken: String? = null
    ): Response<YouTubeSearchResponse>

    // Use search with popular query terms as fallback for trending
    @GET("search")
    suspend fun getTrendingVideos(
        @Query("part") part: String = "snippet",
        @Query("type") type: String = "video",
        @Query("q") query: String = "trending",
        @Query("order") order: String = "relevance",
        @Query("publishedAfter") publishedAfter: String? = null,
        @Query("maxResults") maxResults: Int = 20,
        @Query("key") apiKey: String,
        @Query("pageToken") pageToken: String? = null
    ): Response<YouTubeSearchResponse>

    @GET("videos")
    suspend fun getVideoDetails(
        @Query("part") part: String = "snippet,statistics",
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
}
