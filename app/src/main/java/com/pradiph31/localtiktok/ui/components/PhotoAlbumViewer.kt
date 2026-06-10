package com.pradiph31.localtiktok.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.SwipeRight
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.pradiph31.localtiktok.data.Photo

@Composable
fun PhotoAlbumViewer(
    photos: List<Photo>,
    modifier: Modifier = Modifier
) {
    var isGalleryMode by remember { mutableStateOf(false) }
    var gridColumns by remember { mutableIntStateOf(1) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (isGalleryMode) {
            // Scrollable grid gallery view
            LazyVerticalGrid(
                columns = GridCells.Fixed(gridColumns),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(photos) { photo ->
                    AsyncImage(
                        model = photo.uri,
                        contentDescription = photo.title,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(2.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Zoom controls - bottom center
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { if (gridColumns < 5) gridColumns++ },
                    enabled = gridColumns < 5,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ZoomOut,
                        contentDescription = "Zoom out",
                        tint = if (gridColumns < 5) Color.White else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "${gridColumns}x",
                    color = Color.White,
                    fontSize = 14.sp
                )
                IconButton(
                    onClick = { if (gridColumns > 1) gridColumns-- },
                    enabled = gridColumns > 1,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ZoomIn,
                        contentDescription = "Zoom in",
                        tint = if (gridColumns > 1) Color.White else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else {
            // Current swipe view
            val pagerState = rememberPagerState(pageCount = { photos.size })

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = photos[page].uri,
                        contentDescription = photos[page].title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Page indicator
            if (photos.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 48.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(photos.size.coerceAtMost(20)) { index ->
                        val color = if (pagerState.currentPage == index)
                            Color.White
                        else
                            Color.White.copy(alpha = 0.4f)
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }
            }
        }

        // Toggle button - top right
        IconButton(
            onClick = { isGalleryMode = !isGalleryMode },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 8.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Black.copy(alpha = 0.5f)
            )
        ) {
            Icon(
                imageVector = if (isGalleryMode) Icons.Filled.SwipeRight else Icons.Filled.GridView,
                contentDescription = if (isGalleryMode) "Switch to swipe view" else "Switch to gallery view",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
