package com.pradiph31.localtiktok.viewmodel

import android.app.Application
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pradiph31.localtiktok.data.ContentMode
import com.pradiph31.localtiktok.data.FolderInfo
import com.pradiph31.localtiktok.data.MediaItem
import com.pradiph31.localtiktok.data.MediaRepository
import com.pradiph31.localtiktok.data.SettingsRepository
import com.pradiph31.localtiktok.ui.screens.SortOption
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

    // All media items (both videos and photos) for liked list - independent of mode
    private val _allMediaItems = MutableStateFlow<List<MediaItem>>(emptyList())
    val allMediaItems: StateFlow<List<MediaItem>> = _allMediaItems.asStateFlow()

    private val _contentMode = MutableStateFlow(ContentMode.MIXED)
    val contentMode: StateFlow<ContentMode> = _contentMode.asStateFlow()

    private val _ignoredFolders = MutableStateFlow<Set<String>>(emptySet())
    val ignoredFolders: StateFlow<Set<String>> = _ignoredFolders.asStateFlow()

    private val _ignoredFiles = MutableStateFlow<Set<String>>(emptySet())
    val ignoredFiles: StateFlow<Set<String>> = _ignoredFiles.asStateFlow()

    private val _likedItems = MutableStateFlow<Set<String>>(emptySet())
    val likedItems: StateFlow<Set<String>> = _likedItems.asStateFlow()

    private val _allFolders = MutableStateFlow<List<FolderInfo>>(emptyList())
    val allFolders: StateFlow<List<FolderInfo>> = _allFolders.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Filter: show only unliked items in feed
    private val _showUnlikedOnly = MutableStateFlow(false)
    val showUnlikedOnly: StateFlow<Boolean> = _showUnlikedOnly.asStateFlow()

    fun toggleUnlikedFilter() {
        _showUnlikedOnly.value = !_showUnlikedOnly.value
    }

    // Browse screen state - persisted across navigation
    private val _browsePath = MutableStateFlow(Environment.getExternalStorageDirectory().absolutePath)
    val browsePath: StateFlow<String> = _browsePath.asStateFlow()

    private val _browseSortOption = MutableStateFlow(SortOption.NAME_ASC)
    val browseSortOption: StateFlow<SortOption> = _browseSortOption.asStateFlow()

    fun setBrowsePath(path: String) {
        _browsePath.value = path
    }

    fun setBrowseSortOption(option: SortOption) {
        _browseSortOption.value = option
    }

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
        viewModelScope.launch {
            settingsRepository.ignoredFiles.collect { files ->
                _ignoredFiles.value = files
            }
        }
        viewModelScope.launch {
            settingsRepository.likedItems.collect { items ->
                _likedItems.value = items
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
            val ignoredFiles = settingsRepository.ignoredFiles.first()
            val mode = settingsRepository.contentMode.first()
            _ignoredFolders.value = ignored
            _ignoredFiles.value = ignoredFiles
            _contentMode.value = mode

            val allItems = withContext(Dispatchers.IO) {
                val videos = mediaRepository.getVideos(ignored, ignoredFiles)
                val albums = mediaRepository.getPhotoAlbums(ignored, ignoredFiles)
                videos + albums
            }
            _allMediaItems.value = allItems

            val items = when (mode) {
                ContentMode.VIDEOS_ONLY -> allItems.filterIsInstance<MediaItem.Video>()
                ContentMode.PHOTOS_ONLY -> allItems.filterIsInstance<MediaItem.PhotoAlbum>()
                ContentMode.MIXED -> allItems
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

    fun toggleLike(item: MediaItem) {
        viewModelScope.launch {
            val current = _likedItems.value.toMutableSet()
            if (current.contains(item.uniqueKey)) {
                current.remove(item.uniqueKey)
            } else {
                current.add(item.uniqueKey)
            }
            settingsRepository.setLikedItems(current)
            _likedItems.value = current
        }
    }

    fun isLiked(item: MediaItem): Boolean {
        return _likedItems.value.contains(item.uniqueKey)
    }

    fun toggleLikeByPath(path: String) {
        viewModelScope.launch {
            val current = _likedItems.value.toMutableSet()
            if (current.contains(path)) {
                current.remove(path)
            } else {
                current.add(path)
            }
            settingsRepository.setLikedItems(current)
            _likedItems.value = current
        }
    }

    fun ignoreFile(item: MediaItem) {
        viewModelScope.launch {
            val current = _ignoredFiles.value.toMutableSet()
            current.add(item.uniqueKey)
            settingsRepository.setIgnoredFiles(current)
            _ignoredFiles.value = current
            // Remove from current feed and all items
            _mediaItems.value = _mediaItems.value.filter { it.uniqueKey != item.uniqueKey }
            _allMediaItems.value = _allMediaItems.value.filter { it.uniqueKey != item.uniqueKey }
        }
    }

    fun unignoreFile(filePath: String) {
        viewModelScope.launch {
            val current = _ignoredFiles.value.toMutableSet()
            current.remove(filePath)
            settingsRepository.setIgnoredFiles(current)
            _ignoredFiles.value = current
        }
    }

    fun reshuffle() {
        _mediaItems.value = _mediaItems.value.shuffled()
    }
}
