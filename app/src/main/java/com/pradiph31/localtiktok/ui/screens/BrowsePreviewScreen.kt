package com.pradiph31.localtiktok.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.pradiph31.localtiktok.ui.components.VideoPlayer
import com.pradiph31.localtiktok.viewmodel.MainViewModel
import java.io.File

@Composable
fun BrowsePreviewScreen(
    viewModel: MainViewModel,
    filePath: String,
    onBack: () -> Unit
) {
    val likedItems by viewModel.likedItems.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val isLiked = likedItems.contains(filePath)
    val file = File(filePath)
    val ext = file.extension.lowercase()
    val isVideo = ext in listOf("mp4", "mkv", "avi", "mov", "webm", "3gp", "flv", "wmv")
    val isImage = ext in listOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "heic", "heif")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when {
            isVideo -> {
                VideoPlayer(
                    videoUri = Uri.fromFile(file),
                    isVisible = true,
                    isMuted = isMuted,
                    onToggleMute = { viewModel.toggleMute() },
                    modifier = Modifier.fillMaxSize()
                )
            }
            isImage -> {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(file)
                        .build(),
                    contentDescription = file.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }

        // Action buttons on the right
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(
                onClick = { viewModel.toggleLikeByPath(filePath) }
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (isLiked) "Unlike" else "Like",
                    tint = if (isLiked) Color.Red else Color.White,
                    modifier = Modifier.size(32.dp)
                )
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

