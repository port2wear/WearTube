package com.blake7.weartube.data.model

// Channel response models
data class YouTubeChannelsResponse(
    val kind: String?,
    val etag: String?,
    val pageInfo: PageInfo?,
    val items: List<YouTubeChannel>?
)

data class YouTubeChannel(
    val kind: String?,
    val etag: String?,
    val id: String?,
    val snippet: ChannelSnippet?,
    val statistics: ChannelStatistics?
)

data class ChannelSnippet(
    val title: String?,
    val description: String?,
    val customUrl: String?,
    val publishedAt: String?,
    val thumbnails: Thumbnails?,
    val defaultLanguage: String?,
    val localized: LocalizedText?,
    val country: String?
)

data class ChannelStatistics(
    val viewCount: String?,
    val subscriberCount: String?,
    val hiddenSubscriberCount: Boolean?,
    val videoCount: String?
)

data class LocalizedText(
    val title: String?,
    val description: String?
)