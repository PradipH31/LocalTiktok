package com.pradiph31.localtiktok.ui.screens

import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.video.VideoFrameDecoder
import com.pradiph31.localtiktok.data.MediaItem
import com.pradiph31.localtiktok.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LikedScreen(
    viewModel: MainViewModel,
    onItemClick: (MediaItem) -> Unit,
    onShufflePlay: () -> Unit
) {
    val allMediaItems by viewModel.allMediaItems.collectAsState()
    val likedItems by viewModel.likedItems.collectAsState()

    val likedMedia = allMediaItems.filter { likedItems.contains(it.uniqueKey) }
    val videoCount = likedMedia.count { it is MediaItem.Video }
    val photoCount = likedMedia.count { it is MediaItem.PhotoAlbum }
    val totalCount = likedMedia.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Liked")
                        Text(
                            text = "$totalCount total · $videoCount videos · $photoCount albums",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    if (likedMedia.isNotEmpty()) {
                        IconButton(onClick = onShufflePlay) {
                            Icon(
                                imageVector = Icons.Filled.Shuffle,
                                contentDescription = "Shuffle play"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (likedMedia.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "No liked items yet.\nTap the heart on videos you enjoy!",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(likedMedia) { item ->
                    LikedItemThumbnail(
                        item = item,
                        onClick = { onItemClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LikedItemThumbnail(item: MediaItem, onClick: () -> Unit) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .aspectRatio(9f / 16f)
            .background(Color.DarkGray)
            .clickable(onClick = onClick)
    ) {
        when (item) {
            is MediaItem.Video -> {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(item.uri)
                        .decoderFactory(VideoFrameDecoder.Factory())
                        .build(),
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Play icon overlay
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(36.dp)
                )
            }

            is MediaItem.PhotoAlbum -> {
                if (item.photos.isNotEmpty()) {
                    AsyncImage(
                        model = item.photos.first().uri,
                        contentDescription = item.folderName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                // Photo count
                Text(
                    text = "${item.photos.size} photos",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                )
            }
        }

        // Folder name at bottom
        Text(
            text = item.folderName,
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(4.dp)
        )
    }
}
