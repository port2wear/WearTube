package com.blake7.weartube.presentation.screens

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
import androidx.core.text.HtmlCompat  // added for HTML sanitization
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun VideoPlayerScreen(
    video: YouTubeVideo,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VideoPlayerViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val uiState by viewModel.uiState.collectAsState()
    var isPlaying by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(0) }
    var totalTime by remember { mutableStateOf(180) }

    // Load video when screen opens
    LaunchedEffect(video) {
        viewModel.loadVideo(video)
    }

    // Simulate playback progress when playing
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying && currentTime < totalTime) {
                kotlinx.coroutines.delay(1000)
                currentTime++
            }
            if (currentTime >= totalTime) {
                isPlaying = false
                currentTime = 0
            }
        }
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
            StandaloneVideoPlayer(
                video = video,
                channelIconUrl = uiState.videoDetails?.snippet?.thumbnails?.default?.url,
                isPlaying = isPlaying,
                currentTime = currentTime,
                totalTime = totalTime,
                onPlayPause = { isPlaying = !isPlaying }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Real Video Description Section
            RealVideoDescriptionSection(
                video = video,
                videoDetails = uiState.videoDetails,
                channelIconUrl = uiState.videoDetails?.snippet?.thumbnails?.default?.url
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
fun VideoPlayerContent(
    video: YouTubeVideo,
    onOpenInYouTube: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Video display area
        Card(
            onClick = { onOpenInYouTube() },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                // Show real thumbnail as background
                if (video.thumbnailUrl.isNotEmpty()) {
                    AsyncImage(
                        model = video.thumbnailUrl,
                        contentDescription = video.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Dark overlay for better text visibility
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f))
                    )
                }

                // Play button overlay - opens YouTube directly
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Red.copy(alpha = 0.9f))
                        .clickable { onOpenInYouTube() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "▶️",
                        fontSize = 24.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Video info
        Text(
            text = video.title,
            style = MaterialTheme.typography.title3.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colors.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "👤 ${video.channelTitle}",
            style = MaterialTheme.typography.caption1.copy(
                fontSize = 9.sp
            ),
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun RealVideoDescriptionSection(
    video: YouTubeVideo,
    videoDetails: YouTubeVideoDetails?,
    channelIconUrl: String?,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val fullDesc = videoDetails?.snippet?.description
        ?: ""
    val firstSentence = remember(fullDesc) {
        fullDesc.indexOf('.')
            .takeIf { it >= 0 }
            ?.let { fullDesc.substring(0, it + 1) }
            ?: fullDesc
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

            Spacer(modifier = Modifier.height(6.dp))

            // stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Channel icon + name
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

                videoDetails?.statistics?.viewCount?.let { viewCount ->
                    Text(
                        text = "👁️ ${formatViewCount(viewCount)}",
                        style = MaterialTheme.typography.caption2.copy(fontSize = 8.sp),
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun StandaloneVideoPlayer(
    video: YouTubeVideo,
    channelIconUrl: String?,
    isPlaying: Boolean,
    currentTime: Int,
    totalTime: Int,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Title expansion state
    var titleExpanded by remember { mutableStateOf(false) }
    val fullTitle = video.title
    val firstSentenceTitle = remember(fullTitle) {
        fullTitle.indexOf('.')
            .takeIf { it >= 0 }
            ?.let { fullTitle.substring(0, it + 1) }
            ?: fullTitle
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Video player area with full controls
        Card(
            onClick = onPlayPause,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                // Show video thumbnail
                if (video.thumbnailUrl.isNotEmpty()) {
                    AsyncImage(
                        model = video.thumbnailUrl,
                        contentDescription = video.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Video overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (isPlaying) Color.Black.copy(alpha = 0.3f)
                                else Color.Black.copy(alpha = 0.5f)
                            )
                    )
                }

                // Video status and controls
                if (isPlaying) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🔴 LIVE PLAYING",
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${formatTime(currentTime)} / ${formatTime(totalTime)}",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "▶️",
                            fontSize = 32.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "TAP TO PLAY",
                            fontSize = 8.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Progress bar
        if (totalTime > 0) {
            val progress = currentTime.toFloat() / totalTime.toFloat()
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTime(currentTime),
                    fontSize = 8.sp,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colors.onSurface.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .background(Color.Red)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = formatTime(totalTime),
                    fontSize = 8.sp,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Expandable title
        Text(
            text = if (titleExpanded) fullTitle else firstSentenceTitle,
            style = MaterialTheme.typography.title3.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colors.onSurface,
            maxLines = if (titleExpanded) Int.MAX_VALUE else 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )
        Text(
            text = if (titleExpanded) "Show less" else "Show more",
            style = MaterialTheme.typography.caption2.copy(
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colors.primary,
            modifier = Modifier
                .clickable { titleExpanded = !titleExpanded }
                .padding(4.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Existing channel info
        // Channel icon + name
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = channelIconUrl,
                contentDescription = "Channel icon",
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = video.channelTitle,
                style = MaterialTheme.typography.caption1.copy(
                    fontSize = 9.sp
                ),
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Playback controls
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Previous button
            Button(
                onClick = { /* Previous video */ },
                modifier = Modifier.size(28.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.surface
                )
            ) {
                Text("⏮️", fontSize = 8.sp)
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main play/pause button
            Button(
                onClick = onPlayPause,
                modifier = Modifier.size(40.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (isPlaying) Color.Red else Color.Green
                )
            ) {
                Text(
                    text = if (isPlaying) "⏸️" else "▶️",
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Next button
            Button(
                onClick = { /* Next video */ },
                modifier = Modifier.size(28.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.surface
                )
            ) {
                Text("⏭️", fontSize = 8.sp)
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

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return "%d:%02d".format(minutes, secs)
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

// Previews
@Preview(showBackground = true)
@Composable
fun VideoPlayerScreenPreview() {
    VideoPlayerScreen(
        video = YouTubeVideo(id = null, snippet = null),
        onBackClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun VideoPlayerContentPreview() {
    VideoPlayerContent(
        video = YouTubeVideo(id = null, snippet = null),
        onOpenInYouTube = {}
    )
}

@Preview(showBackground = true)
@Composable
fun RealVideoDescriptionSectionPreview() {
    RealVideoDescriptionSection(
        video = YouTubeVideo(id = null, snippet = null),
        videoDetails = null,
        channelIconUrl = null
    )
}

@Preview(showBackground = true)
@Composable
fun StandaloneVideoPlayerPreview() {
    StandaloneVideoPlayer(
        video = YouTubeVideo(id = null, snippet = null),
        channelIconUrl = null,
        isPlaying = false,
        currentTime = 0,
        totalTime = 100,
        onPlayPause = {}
    )
}

@Preview(showBackground = true)
@Composable
fun CommentsSectionPreview() {
    CommentsSection(
        comments = emptyList(),
        isLoading = false
    )
}

@Preview(showBackground = true)
@Composable
fun RealCommentItemPreview() {
    RealCommentItem(
        author = "Author",
        text = "Sample comment",
        likeCount = 0,
        profileImageUrl = null
    )
}
