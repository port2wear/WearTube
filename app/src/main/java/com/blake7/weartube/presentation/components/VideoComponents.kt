package com.blake7.weartube.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.Card
import coil.compose.AsyncImage
import com.blake7.weartube.data.model.YouTubeVideo
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun VideoItem(
    video: YouTubeVideo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 1.dp) // reduced padding
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp) // reduced inner padding
        ) {
            // Thumbnail area with real thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp) // reduced height
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                // Real thumbnail with fallback
                if (video.thumbnailUrl.isNotEmpty()) {
                    AsyncImage(
                        model = video.thumbnailUrl,
                        contentDescription = video.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Fallback if no thumbnail
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎬",
                            fontSize = 20.sp
                        )
                        Text(
                            text = "NO THUMBNAIL",
                            fontSize = 6.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Play icon overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(32.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Red.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "▶️",
                        fontSize = 12.sp
                    )
                }

                // Duration badge (bottom right)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "📺",
                        fontSize = 6.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp)) // reduced spacing

            // Title
            Text(
                text = video.title,
                style = MaterialTheme.typography.caption1.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colors.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Channel name with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "👤",
                    fontSize = 8.sp
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = video.channelTitle,
                    style = MaterialTheme.typography.caption2.copy(
                        fontSize = 8.sp
                    ),
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⏳",
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Loading videos...",
                style = MaterialTheme.typography.caption1.copy(
                    fontSize = 10.sp
                ),
                color = MaterialTheme.colors.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ErrorMessage(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onRetry,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .background(MaterialTheme.colors.error.copy(alpha = 0.1f)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⚠️",
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Error",
                style = MaterialTheme.typography.title3.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colors.error
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.caption1.copy(
                    fontSize = 9.sp
                ),
                color = MaterialTheme.colors.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔄",
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "Tap to retry",
                    style = MaterialTheme.typography.caption2.copy(
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colors.primary
                )
            }
        }
    }
}

@Composable
fun ShortItem(
    video: YouTubeVideo,
    onClick: (YouTubeVideo) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(4.dp)
            .clickable { onClick(video) }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp))
        ) {
            if (video.thumbnailUrl.isNotEmpty()) {
                AsyncImage(
                    model = video.thumbnailUrl,
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎬", fontSize = 24.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = video.title,
            style = MaterialTheme.typography.caption1.copy(fontSize = 10.sp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "1M views",
            style = MaterialTheme.typography.caption2.copy(fontSize = 8.sp),
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )
    }
}

/**
 * Grid layout that displays Shorts in 2 columns, scrolling vertically.
 */
@Composable
fun ShortsGrid(
    videos: List<YouTubeVideo>,
    onClick: (YouTubeVideo) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
    ) {
        videos.chunked(2).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { video ->
                    ShortItem(
                        video = video,
                        onClick = onClick,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VideoItemPreview() {
    VideoItem(
        video = YouTubeVideo(id = null, snippet = null),
        onClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun LoadingIndicatorPreview() {
    LoadingIndicator()
}

@Preview(showBackground = true)
@Composable
fun ErrorMessagePreview() {
    ErrorMessage(
        message = "Sample error",
        onRetry = {}
    )
}

@Preview(showBackground = true)
@Composable
fun ShortItemPreview() {
    ShortItem(
        video = YouTubeVideo(id = null, snippet = null),
        onClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun ShortsGridPreview() {
    val sample = YouTubeVideo(id = null, snippet = null)
    ShortsGrid(
        videos = listOf(sample, sample),
        onClick = {}
    )
}
