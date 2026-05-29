package com.pradiph31.localtiktok.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    val mediaItems by viewModel.mediaItems.collectAsState()
    val item = mediaItems.find { it.uniqueKey == itemKey }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (item != null) {
            when (item) {
                is MediaItem.Video -> {
                    VideoPlayer(
                        videoUri = item.uri,
                        isVisible = true,
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

        // Back button
        IconButton(
            onClick = onBack,
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

