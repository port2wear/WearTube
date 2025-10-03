package com.blake7.weartube.presentation.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.*
import coil.compose.AsyncImage
import com.blake7.weartube.data.model.YouTubeVideo
import com.blake7.weartube.data.model.CommentThread
import com.blake7.weartube.data.model.YouTubeVideoDetails
import com.blake7.weartube.presentation.viewmodel.VideoPlayerViewModel
import com.blake7.weartube.presentation.components.YouTubeEmbeddedPlayer
import com.blake7.weartube.presentation.components.YouTubeIFramePlayer
import com.blake7.weartube.presentation.components.PlayerState
import com.blake7.weartube.presentation.components.YouTubeThumbnailPlayer
import androidx.core.text.HtmlCompat

@Composable
fun VideoPlayerScreen(
    video: YouTubeVideo,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VideoPlayerViewModel = viewModel()
) {
    // If videoId is blank, show error and back button
    if (video.videoId.isBlank()) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Invalid video ID", color = Color.Red)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onBackClick) {
                Text("Go Back")
            }
        }
        return
    }

    val scrollState = rememberScrollState()
    val uiState by viewModel.uiState.collectAsState()

    // Load video when screen opens
    LaunchedEffect(video) {
        viewModel.loadVideo(video)
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Fixed header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBackClick,
                modifier = Modifier.size(28.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.surface
                )
            ) {
                Text("◀", fontSize = 12.sp, color = MaterialTheme.colors.onSurface)
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "🎬 ${video.title.take(25)}...",
                style = MaterialTheme.typography.caption1.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colors.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 8.dp)
        ) {
            // YouTube Embedded Player
            YouTubeEmbeddedPlayerSection(
                videoId = video.videoId,
                videoTitle = video.title,
                embedUrl = uiState.embedUrl,
                videoDuration = uiState.videoDuration,
                thumbnailUrl = video.thumbnailUrl
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Real Video Description Section
            RealVideoDescriptionSection(
                video = video,
                videoDetails = uiState.videoDetails,
                channelIconUrl = uiState.videoDetails?.snippet?.thumbnails?.default?.url,
                videoDuration = uiState.videoDuration
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Comments Section (real only)
            CommentsSection(
                comments = uiState.comments,
                isLoading = uiState.isLoadingComments
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun YouTubeEmbeddedPlayerSection(
    videoId: String,
    videoTitle: String,
    embedUrl: String?,
    videoDuration: String?,
    thumbnailUrl: String?,
    modifier: Modifier = Modifier
) {
    var useWebView by remember { mutableStateOf(true) }
    var webViewFailed by remember { mutableStateOf(false) }
    var useIFrameAPI by remember { mutableStateOf(true) } // Use IFrame API by default
    var playerState by remember { mutableStateOf<PlayerState?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        onClick = {}
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            when {
                useWebView && !webViewFailed && useIFrameAPI -> {
                    // Use advanced YouTube IFrame API Player
                    YouTubeIFramePlayer(
                        videoId = videoId,
                        modifier = Modifier.fillMaxSize(),
                        autoplay = false,
                        showControls = true,
                        onPlayerReady = {
                            Log.d("VideoPlayer", "YouTube IFrame Player ready")
                        },
                        onStateChange = { state ->
                            playerState = state
                            Log.d("VideoPlayer", "Player state: $state")
                        },
                        onError = { error ->
                            Log.w("VideoPlayer", "IFrame API failed: $error, switching to basic embed")
                            useIFrameAPI = false
                        }
                    )
                }
                
                embedUrl != null && useWebView && !webViewFailed -> {
                    // Fallback to basic embedded player
                    YouTubeEmbeddedPlayer(
                        videoId = videoId,
                        modifier = Modifier.fillMaxSize(),
                        autoplay = false,
                        showControls = true,
                        customEmbedUrl = embedUrl,
                        onError = { error ->
                            Log.w("VideoPlayer", "WebView failed, switching to thumbnail: $error")
                            webViewFailed = true
                            useWebView = false
                        }
                    )
                }
                
                else -> {
                    // Fallback to thumbnail player
                    YouTubeThumbnailPlayer(
                        videoId = videoId,
                        videoTitle = videoTitle,
                        thumbnailUrl = thumbnailUrl,
                        videoDuration = videoDuration,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Duration badge overlay (if available and using WebView)
            if (useWebView && !webViewFailed && videoDuration != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.8f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = videoDuration,
                        style = MaterialTheme.typography.caption2.copy(fontSize = 7.sp),
                        color = Color.White
                    )
                }
            }

            // Player state indicator (top-left corner)
            if (playerState != null && useIFrameAPI) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = when (playerState) {
                            PlayerState.PLAYING -> "▶️"
                            PlayerState.PAUSED -> "⏸️"
                            PlayerState.BUFFERING -> "⏳"
                            PlayerState.ENDED -> "⏹️"
                            else -> ""
                        },
                        style = MaterialTheme.typography.caption2.copy(fontSize = 8.sp),
                        color = Color.White
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Video title
    Text(
        text = videoTitle,
        style = MaterialTheme.typography.caption1.copy(
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium
        ),
        color = MaterialTheme.colors.onSurface,
        textAlign = TextAlign.Center,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(4.dp))

    // Status indicator
    val statusText = when {
        useIFrameAPI && useWebView && !webViewFailed -> "🎬 YouTube IFrame API Player"
        !useIFrameAPI && useWebView && !webViewFailed -> "🎬 YouTube Embedded Player"
        webViewFailed -> "🎬 Tap thumbnail to open in YouTube"
        else -> "🎬 Loading..."
    }
    
    val statusColor = when {
        useWebView && !webViewFailed -> Color.Green.copy(alpha = 0.8f)
        webViewFailed -> Color.Yellow.copy(alpha = 0.8f)
        else -> Color.Red.copy(alpha = 0.8f)
    }
    
    Text(
        text = statusText,
        fontSize = 6.sp,
        color = statusColor,
        textAlign = TextAlign.Center
    )
}

@Composable
fun RealVideoDescriptionSection(
    video: YouTubeVideo,
    videoDetails: YouTubeVideoDetails?,
    channelIconUrl: String?,
    videoDuration: String?,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val fullDesc = videoDetails?.snippet?.description ?: ""
    val firstSentence = remember(fullDesc) {
        fullDesc.indexOf('.')
            .takeIf { it >= 0 }
            ?.let { fullDesc.substring(0, it + 1) }
            ?: fullDesc.take(100) // Take first 100 chars if no sentence break
    }

    // Clickable Card with empty onClick so inner toggle works
    Card(
        onClick = {},
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                text = "📖 Description",
                style = MaterialTheme.typography.title3.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colors.primary
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Video metadata row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                videoDuration?.let { duration ->
                    Text(
                        text = "⏱️ $duration",
                        style = MaterialTheme.typography.caption2.copy(fontSize = 8.sp),
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                videoDetails?.snippet?.categoryId?.let { categoryId ->
                    Text(
                        text = "📁 Category $categoryId",
                        style = MaterialTheme.typography.caption2.copy(fontSize = 8.sp),
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Description text
            if (fullDesc.isNotBlank()) {
                Text(
                    text = if (expanded) fullDesc else firstSentence,
                    style = MaterialTheme.typography.caption1.copy(
                        fontSize = 9.sp
                    ),
                    color = MaterialTheme.colors.onSurface,
                    lineHeight = 12.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (expanded) "Show less" else "Show more",
                    style = MaterialTheme.typography.caption2.copy(
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier
                        .clickable { expanded = !expanded }
                        .padding(4.dp)
                        .align(Alignment.End)
                )
            } else {
                Text(
                    text = "No description available",
                    style = MaterialTheme.typography.caption1.copy(fontSize = 9.sp),
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Enhanced stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Channel info with icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!channelIconUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = channelIconUrl,
                            contentDescription = "Channel icon",
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colors.onSurface.copy(alpha = 0.2f))
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = video.channelTitle,
                        style = MaterialTheme.typography.caption2.copy(
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
                    )
                }

                // Video statistics
                Column(horizontalAlignment = Alignment.End) {
                    videoDetails?.statistics?.viewCount?.let { viewCount ->
                        Text(
                            text = "👁️ ${formatViewCount(viewCount)}",
                            style = MaterialTheme.typography.caption2.copy(fontSize = 8.sp),
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    
                    videoDetails?.statistics?.likeCount?.let { likeCount ->
                        Text(
                            text = "👍 ${formatViewCount(likeCount)}",
                            style = MaterialTheme.typography.caption2.copy(fontSize = 7.sp),
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun CommentsSection(
    comments: List<CommentThread>,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { },
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                text = "💬 Comments",
                style = MaterialTheme.typography.title3.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colors.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    indicatorColor = MaterialTheme.colors.primary
                )
            } else if (comments.isEmpty()) {
                Text(
                    text = "💭 No comments available or disabled.",
                    style = MaterialTheme.typography.caption1.copy(fontSize = 9.sp),
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                comments.take(10).forEach { thread ->
                    thread.snippet?.topLevelComment?.snippet?.let { comment ->
                        RealCommentItem(
                            author = comment.authorDisplayName ?: "Unknown",
                            text = comment.textDisplay ?: "",
                            likeCount = comment.likeCount ?: 0,
                            profileImageUrl = comment.authorProfileImageUrl
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun RealCommentItem(
    author: String,
    text: String,
    likeCount: Int,
    profileImageUrl: String?,
    modifier: Modifier = Modifier
) {
    // Sanitize HTML, control chars, and non-ASCII symbols from YouTube comments
    val sanitizedText = remember(text) {
        HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY)
            .toString()
            .replace(Regex("\\p{C}"), "")            // remove control characters
            .replace(Regex("[^\u0000-\u007F]"), "") // remove non-ASCII chars
            .replace(Regex("\\s+"), " ")            // collapse whitespace
            .trim()
    }
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Show actual profile image if available
                if (!profileImageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = profileImageUrl,
                        contentDescription = "$author profile image",
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Placeholder circle
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colors.onSurface.copy(alpha = 0.2f))
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = author,
                    style = MaterialTheme.typography.caption2.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colors.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (likeCount > 0) {
                Text(
                    text = "👍 $likeCount",
                    style = MaterialTheme.typography.caption2.copy(
                        fontSize = 7.sp
                    ),
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = sanitizedText,
            style = MaterialTheme.typography.caption2.copy(
                fontSize = 8.sp
            ),
            color = MaterialTheme.colors.onSurface,
            lineHeight = 10.sp
        )
    }
}

// Helper function to format view count
private fun formatViewCount(viewCount: String): String {
    return try {
        val count = viewCount.toLong()
        when {
            count >= 1_000_000 -> "${count / 1_000_000}M views"
            count >= 1_000 -> "${count / 1_000}K views"
            else -> "$count views"
        }
    } catch (_: NumberFormatException) {
        viewCount
    }
}
