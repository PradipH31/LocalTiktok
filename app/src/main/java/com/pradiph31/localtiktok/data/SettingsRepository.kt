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
    private val CONTENT_MODE_KEY = stringPreferencesKey("content_mode")

    val ignoredFolders: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[IGNORED_FOLDERS_KEY] ?: emptySet()
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

    suspend fun setContentMode(mode: ContentMode) {
        context.dataStore.edit { preferences ->
            preferences[CONTENT_MODE_KEY] = mode.name
        }
    }
}

