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

    data class Video(
        override val id: Long,
        val uri: Uri,
        val title: String,
        val duration: Long,
        override val folderPath: String,
        override val folderName: String
    ) : MediaItem()

    data class PhotoAlbum(
        override val id: Long,
        val photos: List<Photo>,
        override val folderPath: String,
        override val folderName: String
    ) : MediaItem()
}

data class Photo(
    val id: Long,
    val uri: Uri,
    val title: String,
    val dateTaken: Long
)

