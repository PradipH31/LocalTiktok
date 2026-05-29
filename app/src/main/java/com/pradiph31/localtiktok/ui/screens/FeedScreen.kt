package com.pradiph31.localtiktok.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
    viewModel: MainViewModel
) {
    val mediaItems by viewModel.mediaItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val likedItems by viewModel.likedItems.collectAsState()

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

            // Right side action buttons (TikTok style)
            if (mediaItems.isNotEmpty() && pagerState.currentPage < mediaItems.size) {
                val currentItem = mediaItems[pagerState.currentPage]
                val isLiked = likedItems.contains(currentItem.uniqueKey)

                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Like button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = { viewModel.toggleLike(currentItem) }
                        ) {
                            Icon(
                                imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if (isLiked) Color.Red else Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    // Hide/Ignore button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = { viewModel.ignoreFile(currentItem) }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Hide this item",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // Shuffle button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = { viewModel.reshuffle() }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Shuffle",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                // Folder name overlay at bottom
                Text(
                    text = currentItem.folderName,
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 16.dp)
                )
            }
        }
    }
}
