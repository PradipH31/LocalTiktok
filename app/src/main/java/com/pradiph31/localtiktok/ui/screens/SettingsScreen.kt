package com.pradiph31.localtiktok.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pradiph31.localtiktok.data.ContentMode
import com.pradiph31.localtiktok.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateToHiddenFiles: () -> Unit,
    onNavigateToIgnoredFolders: () -> Unit
) {
    val contentMode by viewModel.contentMode.collectAsState()
    val ignoredFolders by viewModel.ignoredFolders.collectAsState()
    val ignoredFiles by viewModel.ignoredFiles.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Content Mode Section
            Text(
                text = "Content Mode",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = contentMode == ContentMode.MIXED,
                    onClick = { viewModel.setContentMode(ContentMode.MIXED) },
                    label = { Text("Mixed") },
                    modifier = Modifier.padding(end = 8.dp)
                )
                FilterChip(
                    selected = contentMode == ContentMode.VIDEOS_ONLY,
                    onClick = { viewModel.setContentMode(ContentMode.VIDEOS_ONLY) },
                    label = { Text("Videos") },
                    modifier = Modifier.padding(end = 8.dp)
                )
                FilterChip(
                    selected = contentMode == ContentMode.PHOTOS_ONLY,
                    onClick = { viewModel.setContentMode(ContentMode.PHOTOS_ONLY) },
                    label = { Text("Photos") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()

            // Hidden Files button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToHiddenFiles)
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hidden Files",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${ignoredFiles.size} file(s) hidden",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Open",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider()

            // Ignored Folders button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToIgnoredFolders)
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ignored Folders",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${ignoredFolders.size} folder(s) ignored",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Open",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider()
        }
    }
}
