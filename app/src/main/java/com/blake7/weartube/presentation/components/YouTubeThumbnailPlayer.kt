package com.blake7.weartube.presentation.components

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.core.net.toUri
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*
import coil.compose.AsyncImage

@Composable
fun YouTubeThumbnailPlayer(
    videoId: String,
    videoTitle: String,
    thumbnailUrl: String?,
    videoDuration: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        onClick = {
            openYouTubeVideo(context, videoId)
        },
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            // Background thumbnail
            if (!thumbnailUrl.isNullOrBlank()) {
                AsyncImage(
                    model = thumbnailUrl,
                    contentDescription = videoTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Dark overlay for better visibility
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                )
            }

            // Play button overlay
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.Red.copy(alpha = 0.9f))
                    .clickable {
                        openYouTubeVideo(context, videoId)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "▶",
                    fontSize = 32.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            // Duration badge (if available)
            videoDuration?.let { duration ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.8f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = duration,
                        style = MaterialTheme.typography.caption2.copy(fontSize = 8.sp),
                        color = Color.White
                    )
                }
            }

            // "Tap to open" text
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Tap to open in YouTube",
                    style = MaterialTheme.typography.caption2.copy(fontSize = 7.sp),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun openYouTubeVideo(context: Context, videoId: String) {
    try {
        Log.d("YouTubeThumbnailPlayer", "Opening YouTube video: $videoId")
        
        // Try YouTube app first
        val appIntent = Intent(Intent.ACTION_VIEW, "vnd.youtube:$videoId".toUri()).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            setPackage("com.google.android.youtube")
        }
        context.startActivity(appIntent)
        
    } catch (_: Exception) {
        try {
            // Fallback to browser
            val webIntent = Intent(Intent.ACTION_VIEW, "https://www.youtube.com/watch?v=$videoId".toUri()).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
            
        } catch (e2: Exception) {
            Log.e("YouTubeThumbnailPlayer", "Failed to open YouTube video", e2)
            
            // Last resort - try any app that can handle the YouTube URL
            try {
                val genericIntent = Intent(Intent.ACTION_VIEW, "https://m.youtube.com/watch?v=$videoId".toUri()).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(genericIntent)
            } catch (e3: Exception) {
                Log.e("YouTubeThumbnailPlayer", "All methods failed to open YouTube", e3)
            }
        }
    }
}