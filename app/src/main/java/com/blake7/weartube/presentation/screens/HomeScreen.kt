package com.blake7.weartube.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.*
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import com.blake7.weartube.data.model.YouTubeVideo
import com.blake7.weartube.presentation.components.*
import com.blake7.weartube.presentation.viewmodel.HomeViewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun HomeScreen(
    onVideoClick: (YouTubeVideo) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchText by remember { mutableStateOf("") }
    var isShortsMode by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header with search
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (uiState.isSearchMode) "🔍 Search" else if (isShortsMode) "🎬 Shorts" else "📺 WearTube",
                style = MaterialTheme.typography.title3.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colors.primary
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Mode toggle
                Button(
                    onClick = { isShortsMode = !isShortsMode },
                    modifier = Modifier.size(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (isShortsMode) MaterialTheme.colors.primary else MaterialTheme.colors.surface
                    )
                ) {
                    Text(if (isShortsMode) "▦" else "▤", fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.width(6.dp))
                if (uiState.isSearchMode) {
                    Button(
                        onClick = {
                            searchText = ""
                            viewModel.clearSearch()
                        },
                        modifier = Modifier.size(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.surface
                        )
                    ) {
                        Text("✕", fontSize = 10.sp)
                    }
                }
            }
        }

        // Search bar
        SearchBar(
            query = searchText,
            onQueryChange = {
                searchText = it
                viewModel.searchVideos(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )

        // Content
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    LoadingIndicator()
                }

                uiState.error != null -> {
                    ErrorMessage(
                        message = uiState.error!!,
                        onRetry = {
                            viewModel.clearError()
                            if (uiState.isSearchMode) {
                                viewModel.searchVideos(searchText)
                            } else {
                                viewModel.loadTrendingVideos()
                            }
                        }
                    )
                }

                else -> {
                    val videos = if (uiState.isSearchMode) {
                        uiState.searchResults
                    } else {
                        uiState.trendingVideos
                    }

                    if (videos.isEmpty()) {
                        EmptyState(
                            message = if (uiState.isSearchMode) "🔍 No videos found" else "📺 No trending videos"
                        )
                    } else {
                        if (isShortsMode) {
                            ShortsGrid(
                                videos = videos,
                                onClick = onVideoClick
                            )
                        } else {
                            VideoList(
                                videos = videos,
                                onVideoClick = onVideoClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { },
        modifier = modifier.height(32.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🔍",
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = TextStyle(
                    color = MaterialTheme.colors.onSurface,
                    fontSize = 11.sp
                ),
                singleLine = true,
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = "Search videos...",
                            style = TextStyle(
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

@Composable
fun VideoList(
    videos: List<YouTubeVideo>,
    onVideoClick: (YouTubeVideo) -> Unit,
    modifier: Modifier = Modifier
) {
    ScalingLazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(videos) { video ->
            VideoItem(
                video = video,
                onClick = { onVideoClick(video) }
            )
        }
    }
}

@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.caption1,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        onVideoClick = {},
        viewModel = HomeViewModel()
    )
}

@Preview(showBackground = true)
@Composable
fun SearchBarPreview() {
    SearchBar(
        query = "Sample",
        onQueryChange = {},
    )
}

@Preview(showBackground = true)
@Composable
fun VideoListPreview() {
    VideoList(
        videos = listOf(
            YouTubeVideo(id = null, snippet = null),
            YouTubeVideo(id = null, snippet = null)
        ),
        onVideoClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun EmptyStatePreview() {
    EmptyState(
        message = "No videos found"
    )
}
