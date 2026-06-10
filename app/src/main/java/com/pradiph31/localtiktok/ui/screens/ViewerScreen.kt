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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pradiph31.localtiktok.data.MediaItem
import com.pradiph31.localtiktok.ui.components.PhotoAlbumViewer
import com.pradiph31.localtiktok.ui.components.VideoPlayer
import com.pradiph31.localtiktok.viewmodel.MainViewModel

@Composable
fun ViewerScreen(
    viewModel: MainViewModel,
    itemKey: String,
    onBack: () -> Unit
) {
    val allMediaItems by viewModel.allMediaItems.collectAsState()
    val likedItems by viewModel.likedItems.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val shuffledOrder by viewModel.shuffledLikedOrder.collectAsState()

    val isShuffleMode = itemKey == "__shuffle__"

    val likedMedia = remember(allMediaItems, likedItems, shuffledOrder, isShuffleMode) {
        val liked = allMediaItems.filter { likedItems.contains(it.uniqueKey) }
        if (isShuffleMode && shuffledOrder.isNotEmpty()) {
            val itemMap = liked.associateBy { it.uniqueKey }
            shuffledOrder.mapNotNull { key -> itemMap[key] }
        } else {
            liked
        }
    }

    val startIndex = remember(likedMedia, itemKey) {
        if (isShuffleMode) 0
        else likedMedia.indexOfFirst { it.uniqueKey == itemKey }.coerceAtLeast(0)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (likedMedia.isNotEmpty()) {
            val pagerState = rememberPagerState(
                initialPage = startIndex,
                pageCount = { likedMedia.size }
            )

            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 0
            ) { page ->
                val item = likedMedia[page]
                val isVisible = pagerState.currentPage == page

                when (item) {
                    is MediaItem.Video -> {
                        VideoPlayer(
                            videoUri = item.uri,
                            isVisible = isVisible,
                            isMuted = isMuted,
                            onToggleMute = { viewModel.toggleMute() },
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

            // Unlike button on the right side
            if (pagerState.currentPage < likedMedia.size) {
                val currentItem = likedMedia[pagerState.currentPage]
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = { viewModel.toggleLike(currentItem) }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Unlike",
                            tint = Color.Red,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }

        // Back button
        IconButton(
            onClick = {
                if (isShuffleMode) viewModel.clearShuffledOrder()
                onBack()
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 40.dp, start = 8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
