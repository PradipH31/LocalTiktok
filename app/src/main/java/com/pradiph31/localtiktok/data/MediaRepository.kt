package com.pradiph31.localtiktok.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore

class MediaRepository(private val context: Context) {

    fun getVideos(ignoredFolders: Set<String>): List<MediaItem.Video> {
        val videos = mutableListOf<MediaItem.Video>()
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME
        )

        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            val bucketColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn) ?: "Unknown"
                val duration = cursor.getLong(durationColumn)
                val data = cursor.getString(dataColumn) ?: ""
                val bucketName = cursor.getString(bucketColumn) ?: "Unknown"
                val folderPath = data.substringBeforeLast("/")

                if (ignoredFolders.any { folderPath.startsWith(it) }) continue

                val uri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id
                )
                videos.add(
                    MediaItem.Video(
                        id = id,
                        uri = uri,
                        title = title,
                        duration = duration,
                        folderPath = folderPath,
                        folderName = bucketName
                    )
                )
            }
        }
        return videos
    }

    fun getPhotoAlbums(ignoredFolders: Set<String>): List<MediaItem.PhotoAlbum> {
        val photosMap = mutableMapOf<String, MutableList<Photo>>()
        val folderNames = mutableMapOf<String, String>()

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.TITLE,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )

        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE)
            val dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val bucketColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn) ?: "Unknown"
                val dateTaken = cursor.getLong(dateTakenColumn)
                val data = cursor.getString(dataColumn) ?: ""
                val bucketName = cursor.getString(bucketColumn) ?: "Unknown"
                val folderPath = data.substringBeforeLast("/")

                if (ignoredFolders.any { folderPath.startsWith(it) }) continue

                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                )

                folderNames[folderPath] = bucketName
                photosMap.getOrPut(folderPath) { mutableListOf() }.add(
                    Photo(id = id, uri = uri, title = title, dateTaken = dateTaken)
                )
            }
        }

        return photosMap.map { (folderPath, photos) ->
            MediaItem.PhotoAlbum(
                id = photos.first().id,
                photos = photos,
                folderPath = folderPath,
                folderName = folderNames[folderPath] ?: "Unknown"
            )
        }
    }

    fun getAllFolders(): List<FolderInfo> {
        val folders = mutableSetOf<FolderInfo>()

        // Get video folders
        val videoProjection = arrayOf(
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME
        )
        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            videoProjection, null, null, null
        )?.use { cursor ->
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            val bucketColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
            while (cursor.moveToNext()) {
                val data = cursor.getString(dataColumn) ?: continue
                val bucket = cursor.getString(bucketColumn) ?: "Unknown"
                folders.add(FolderInfo(data.substringBeforeLast("/"), bucket))
            }
        }

        // Get image folders
        val imageProjection = arrayOf(
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            imageProjection, null, null, null
        )?.use { cursor ->
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val bucketColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            while (cursor.moveToNext()) {
                val data = cursor.getString(dataColumn) ?: continue
                val bucket = cursor.getString(bucketColumn) ?: "Unknown"
                folders.add(FolderInfo(data.substringBeforeLast("/"), bucket))
            }
        }

        return folders.sortedBy { it.name }
    }
}

data class FolderInfo(val path: String, val name: String)

