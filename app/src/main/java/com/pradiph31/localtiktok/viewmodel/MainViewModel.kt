package com.pradiph31.localtiktok.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pradiph31.localtiktok.data.ContentMode
import com.pradiph31.localtiktok.data.FolderInfo
import com.pradiph31.localtiktok.data.MediaItem
import com.pradiph31.localtiktok.data.MediaRepository
import com.pradiph31.localtiktok.data.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val mediaRepository = MediaRepository(application)
    private val settingsRepository = SettingsRepository(application)

    private val _mediaItems = MutableStateFlow<List<MediaItem>>(emptyList())
    val mediaItems: StateFlow<List<MediaItem>> = _mediaItems.asStateFlow()

    private val _contentMode = MutableStateFlow(ContentMode.MIXED)
    val contentMode: StateFlow<ContentMode> = _contentMode.asStateFlow()

    private val _ignoredFolders = MutableStateFlow<Set<String>>(emptySet())
    val ignoredFolders: StateFlow<Set<String>> = _ignoredFolders.asStateFlow()

    private val _allFolders = MutableStateFlow<List<FolderInfo>>(emptyList())
    val allFolders: StateFlow<List<FolderInfo>> = _allFolders.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.contentMode.collect { mode ->
                _contentMode.value = mode
            }
        }
        viewModelScope.launch {
            settingsRepository.ignoredFolders.collect { folders ->
                _ignoredFolders.value = folders
            }
        }
    }

    private var hasLoaded = false

    fun loadMedia(forceReload: Boolean = false) {
        if (hasLoaded && !forceReload) return
        hasLoaded = true
        viewModelScope.launch {
            _isLoading.value = true
            val ignored = settingsRepository.ignoredFolders.first()
            val mode = settingsRepository.contentMode.first()
            _ignoredFolders.value = ignored
            _contentMode.value = mode

            val items = withContext(Dispatchers.IO) {
                when (mode) {
                    ContentMode.VIDEOS_ONLY -> mediaRepository.getVideos(ignored)
                    ContentMode.PHOTOS_ONLY -> mediaRepository.getPhotoAlbums(ignored)
                    ContentMode.MIXED -> {
                        val videos = mediaRepository.getVideos(ignored)
                        val albums = mediaRepository.getPhotoAlbums(ignored)
                        videos + albums
                    }
                }
            }
            _mediaItems.value = items.shuffled()
            _isLoading.value = false
        }
    }

    fun loadFolders() {
        viewModelScope.launch {
            val folders = withContext(Dispatchers.IO) {
                mediaRepository.getAllFolders()
            }
            _allFolders.value = folders
        }
    }

    fun setContentMode(mode: ContentMode) {
        viewModelScope.launch {
            settingsRepository.setContentMode(mode)
            _contentMode.value = mode
            loadMedia(forceReload = true)
        }
    }

    fun toggleFolderIgnored(folderPath: String) {
        viewModelScope.launch {
            val current = _ignoredFolders.value.toMutableSet()
            if (current.contains(folderPath)) {
                current.remove(folderPath)
            } else {
                current.add(folderPath)
            }
            settingsRepository.setIgnoredFolders(current)
            _ignoredFolders.value = current
        }
    }

    fun reshuffle() {
        _mediaItems.value = _mediaItems.value.shuffled()
    }
}

