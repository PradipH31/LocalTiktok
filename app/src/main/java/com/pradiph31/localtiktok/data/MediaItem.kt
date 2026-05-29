package com.pradiph31.localtiktok.data

import android.net.Uri

enum class ContentMode {
    VIDEOS_ONLY,
    PHOTOS_ONLY,
    MIXED
}

sealed class MediaItem {
    abstract val id: Long
    abstract val folderPath: String
    abstract val folderName: String
    abstract val filePath: String

    /**
     * Unique key for identifying this item in likes/ignores.
     * For videos: the file path. For albums: the folder path.
     */
    val uniqueKey: String get() = filePath

    data class Video(
        override val id: Long,
        val uri: Uri,
        val title: String,
        val duration: Long,
        override val folderPath: String,
        override val folderName: String,
        override val filePath: String
    ) : MediaItem()

    data class PhotoAlbum(
        override val id: Long,
        val photos: List<Photo>,
        override val folderPath: String,
        override val folderName: String,
        override val filePath: String = folderPath
    ) : MediaItem()
}

data class Photo(
    val id: Long,
    val uri: Uri,
    val title: String,
    val dateTaken: Long
)
