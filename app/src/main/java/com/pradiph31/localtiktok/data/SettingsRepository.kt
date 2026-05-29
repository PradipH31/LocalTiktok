package com.pradiph31.localtiktok.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private val IGNORED_FOLDERS_KEY = stringSetPreferencesKey("ignored_folders")
    private val IGNORED_FILES_KEY = stringSetPreferencesKey("ignored_files")
    private val LIKED_ITEMS_KEY = stringSetPreferencesKey("liked_items")
    private val CONTENT_MODE_KEY = stringPreferencesKey("content_mode")

    val ignoredFolders: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[IGNORED_FOLDERS_KEY] ?: emptySet()
    }

    val ignoredFiles: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[IGNORED_FILES_KEY] ?: emptySet()
    }

    val likedItems: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[LIKED_ITEMS_KEY] ?: emptySet()
    }

    val contentMode: Flow<ContentMode> = context.dataStore.data.map { preferences ->
        val modeStr = preferences[CONTENT_MODE_KEY] ?: ContentMode.MIXED.name
        try {
            ContentMode.valueOf(modeStr)
        } catch (e: IllegalArgumentException) {
            ContentMode.MIXED
        }
    }

    suspend fun setIgnoredFolders(folders: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[IGNORED_FOLDERS_KEY] = folders
        }
    }

    suspend fun setIgnoredFiles(files: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[IGNORED_FILES_KEY] = files
        }
    }

    suspend fun setLikedItems(items: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[LIKED_ITEMS_KEY] = items
        }
    }

    suspend fun setContentMode(mode: ContentMode) {
        context.dataStore.edit { preferences ->
            preferences[CONTENT_MODE_KEY] = mode.name
        }
    }
}
