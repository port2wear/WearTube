package com.blake7.weartube.data.model

data class YouTubeVideoDetailsResponse(
    val items: List<YouTubeVideoDetails>?
)

data class YouTubeVideoDetails(
    val id: String?,
    val snippet: VideoDetailsSnippet?,
    val statistics: VideoStatistics?,
    val contentDetails: ContentDetails?
)

data class VideoDetailsSnippet(
    val publishedAt: String?,
    val channelId: String?,
    val title: String?,
    val description: String?,
    val thumbnails: Thumbnails?,
    val channelTitle: String?,
    val tags: List<String>?,
    val categoryId: String?,
    val liveBroadcastContent: String?,
    val defaultLanguage: String?
)

data class VideoStatistics(
    val viewCount: String?,
    val likeCount: String?,
    val favoriteCount: String?,
    val commentCount: String?
)

data class YouTubeCommentsResponse(
    val items: List<CommentThread>?,
    val nextPageToken: String?,
    val pageInfo: PageInfo?
)

data class CommentThread(
    val id: String?,
    val snippet: CommentThreadSnippet?
)

data class CommentThreadSnippet(
    val videoId: String?,
    val topLevelComment: Comment?,
    val canReply: Boolean?,
    val totalReplyCount: Int?,
    val isPublic: Boolean?
)

data class Comment(
    val id: String?,
    val snippet: CommentSnippet?
)

data class CommentSnippet(
    val videoId: String?,
    val textDisplay: String?,
    val textOriginal: String?,
    val authorDisplayName: String?,
    val authorProfileImageUrl: String?,
    val authorChannelUrl: String?,
    val authorChannelId: AuthorChannelId?,
    val canRate: Boolean?,
    val likeCount: Int?,
    val publishedAt: String?,
    val updatedAt: String?
)

data class AuthorChannelId(
    val value: String?
)

// Content details for video duration, quality, etc.
data class ContentDetails(
    val duration: String?,
    val dimension: String?,
    val definition: String?,
    val caption: String?,
    val licensedContent: Boolean?,
    val projection: String?
)
