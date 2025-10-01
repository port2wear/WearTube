package com.blake7.weartube.data.model

import com.google.gson.annotations.SerializedName

data class YouTubeSearchResponse(
    val items: List<YouTubeVideo>?,
    val nextPageToken: String?,
    val pageInfo: PageInfo?
)

data class YouTubeVideo(
    val id: VideoId?,
    val snippet: VideoSnippet?
) {
    val videoId: String
        get() = id?.videoId ?: ""

    val title: String
        get() = snippet?.title ?: ""

    val channelTitle: String
        get() = snippet?.channelTitle ?: ""

    val thumbnailUrl: String
        get() = snippet?.thumbnails?.medium?.url ?: snippet?.thumbnails?.default?.url ?: ""

    val description: String
        get() = snippet?.description ?: ""

    val publishedAt: String
        get() = snippet?.publishedAt ?: ""
}

data class VideoId(
    val kind: String?,
    val videoId: String?
)

data class VideoSnippet(
    val publishedAt: String?,
    val channelId: String?,
    val title: String?,
    val description: String?,
    val thumbnails: Thumbnails?,
    val channelTitle: String?,
    val liveBroadcastContent: String?
)

data class Thumbnails(
    val default: Thumbnail?,
    val medium: Thumbnail?,
    val high: Thumbnail?
)

data class Thumbnail(
    val url: String?,
    val width: Int?,
    val height: Int?
)

data class PageInfo(
    val totalResults: Int?,
    val resultsPerPage: Int?
)
