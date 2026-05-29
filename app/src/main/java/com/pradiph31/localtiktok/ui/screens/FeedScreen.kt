package com.pradiph31.localtiktok.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pradiph31.localtiktok.data.MediaItem
import com.pradiph31.localtiktok.ui.components.PhotoAlbumViewer
import com.pradiph31.localtiktok.ui.components.VideoPlayer
import com.pradiph31.localtiktok.viewmodel.MainViewModel

@Composable
fun FeedScreen(
    viewModel: MainViewModel,
    onSettingsClick: () -> Unit
) {
    val mediaItems by viewModel.mediaItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        } else if (mediaItems.isEmpty()) {
            Text(
                text = "No media found.\nCheck permissions or adjust filters in settings.",
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp)
            )
        } else {
            val pagerState = rememberPagerState(pageCount = { mediaItems.size })

            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1
            ) { page ->
                val item = mediaItems[page]
                val isVisible = pagerState.currentPage == page

                when (item) {
                    is MediaItem.Video -> {
                        VideoPlayer(
                            videoUri = item.uri,
                            isVisible = isVisible,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    is MediaItem.PhotoAlbum -> {
                        PhotoAlbumViewer(
                            photos = item.photos,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Folder name overlay
            if (mediaItems.isNotEmpty() && pagerState.currentPage < mediaItems.size) {
                val currentItem = mediaItems[pagerState.currentPage]
                Text(
                    text = currentItem.folderName,
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                )
            }
        }

        // Settings button
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        // Shuffle button
        FloatingActionButton(
            onClick = { viewModel.reshuffle() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color.White.copy(alpha = 0.2f)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Shuffle",
                tint = Color.White
            )
        }
    }
}

