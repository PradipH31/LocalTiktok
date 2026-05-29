package com.pradiph31.localtiktok.ui.screens

import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.video.VideoFrameDecoder
import com.pradiph31.localtiktok.viewmodel.MainViewModel
import java.io.File

data class BrowseItem(
    val file: File,
    val isDirectory: Boolean,
    val isVideo: Boolean,
    val isImage: Boolean
) {
    val name: String get() = file.name
    val path: String get() = file.absolutePath
}

enum class SortOption(val label: String) {
    NAME_ASC("Name (A-Z)"),
    NAME_DESC("Name (Z-A)"),
    SIZE_ASC("Size (Smallest)"),
    SIZE_DESC("Size (Largest)"),
    DATE_ASC("Date (Oldest)"),
    DATE_DESC("Date (Newest)"),
    TYPE("Type")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(viewModel: MainViewModel) {
    val rootPath = Environment.getExternalStorageDirectory().absolutePath
    var currentPath by remember { mutableStateOf(rootPath) }
    var items by remember { mutableStateOf<List<BrowseItem>>(emptyList()) }
    var sortOption by remember { mutableStateOf(SortOption.NAME_ASC) }
    var showSortMenu by remember { mutableStateOf(false) }
    val likedItems by viewModel.likedItems.collectAsState()
    val context = LocalContext.current

    // Load directory contents when path or sort changes
    LaunchedEffect(currentPath, sortOption) {
        val dir = File(currentPath)
        val files = dir.listFiles()?.toList() ?: emptyList()
        val browseItems = files
            .filter { !it.name.startsWith(".") }
            .map { file ->
                val ext = file.extension.lowercase()
                BrowseItem(
                    file = file,
                    isDirectory = file.isDirectory,
                    isVideo = ext in listOf("mp4", "mkv", "avi", "mov", "webm", "3gp", "flv", "wmv"),
                    isImage = ext in listOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "heic", "heif")
                )
            }

        items = sortBrowseItems(browseItems, sortOption)
    }

    val relativePath = currentPath.removePrefix(rootPath).ifEmpty { "/" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Browse Files", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = relativePath,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    if (currentPath != rootPath) {
                        IconButton(onClick = {
                            currentPath = File(currentPath).parent ?: rootPath
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Go back"
                            )
                        }
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "Sort"
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option.label,
                                            color = if (option == sortOption)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        sortOption = option
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Empty folder",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(items) { item ->
                    BrowseItemRow(
                        item = item,
                        isLiked = likedItems.contains(item.path),
                        onNavigate = { currentPath = item.path },
                        onToggleLike = { viewModel.toggleLikeByPath(item.path) }
                    )
                }
            }
        }
    }
}

private fun sortBrowseItems(items: List<BrowseItem>, sortOption: SortOption): List<BrowseItem> {
    val dirs = items.filter { it.isDirectory }
    val files = items.filter { !it.isDirectory }

    val sortedDirs = when (sortOption) {
        SortOption.NAME_ASC -> dirs.sortedBy { it.name.lowercase() }
        SortOption.NAME_DESC -> dirs.sortedByDescending { it.name.lowercase() }
        SortOption.SIZE_ASC -> dirs.sortedBy { it.name.lowercase() }
        SortOption.SIZE_DESC -> dirs.sortedBy { it.name.lowercase() }
        SortOption.DATE_ASC -> dirs.sortedBy { it.file.lastModified() }
        SortOption.DATE_DESC -> dirs.sortedByDescending { it.file.lastModified() }
        SortOption.TYPE -> dirs.sortedBy { it.name.lowercase() }
    }

    val sortedFiles = when (sortOption) {
        SortOption.NAME_ASC -> files.sortedBy { it.name.lowercase() }
        SortOption.NAME_DESC -> files.sortedByDescending { it.name.lowercase() }
        SortOption.SIZE_ASC -> files.sortedBy { it.file.length() }
        SortOption.SIZE_DESC -> files.sortedByDescending { it.file.length() }
        SortOption.DATE_ASC -> files.sortedBy { it.file.lastModified() }
        SortOption.DATE_DESC -> files.sortedByDescending { it.file.lastModified() }
        SortOption.TYPE -> files.sortedWith(compareBy({ it.file.extension.lowercase() }, { it.name.lowercase() }))
    }

    return sortedDirs + sortedFiles
}

@Composable
private fun BrowseItemRow(
    item: BrowseItem,
    isLiked: Boolean,
    onNavigate: () -> Unit,
    onToggleLike: () -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .clickable {
                if (item.isDirectory) {
                    onNavigate()
                }
            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail or icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            when {
                item.isDirectory -> {
                    Icon(
                        imageVector = Icons.Filled.Folder,
                        contentDescription = null,
                        tint = Color(0xFFFFCA28),
                        modifier = Modifier.size(32.dp)
                    )
                }
                item.isVideo -> {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(File(item.path))
                            .decoderFactory(VideoFrameDecoder.Factory())
                            .size(96)
                            .build(),
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                }
                item.isImage -> {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(File(item.path))
                            .size(96)
                            .build(),
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Filled.Image,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // File name
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (item.isDirectory) {
                val childCount = item.file.listFiles()?.size ?: 0
                Text(
                    text = "$childCount items",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val sizeStr = formatFileSize(item.file.length())
                Text(
                    text = sizeStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Like button for media files and folders
        if (item.isVideo || item.isImage || item.isDirectory) {
            IconButton(onClick = onToggleLike) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (isLiked) "Remove from liked" else "Add to liked",
                    tint = if (isLiked) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1_073_741_824 -> "%.1f GB".format(bytes / 1_073_741_824.0)
        bytes >= 1_048_576 -> "%.1f MB".format(bytes / 1_048_576.0)
        bytes >= 1_024 -> "%.1f KB".format(bytes / 1_024.0)
        else -> "$bytes B"
    }
}

