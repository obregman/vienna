package com.vienna.app.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "vienna_settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val FINNHUB_API_KEY = stringPreferencesKey("finnhub_api_key")
        val CLAUDE_API_KEY = stringPreferencesKey("claude_api_key")
    }

    val finnhubApiKey: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.FINNHUB_API_KEY] ?: ""
        }

    val claudeApiKey: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.CLAUDE_API_KEY] ?: ""
        }

    suspend fun getFinnhubApiKey(): String {
        return context.dataStore.data.first()[PreferencesKeys.FINNHUB_API_KEY] ?: ""
    }

    suspend fun getClaudeApiKey(): String {
        return context.dataStore.data.first()[PreferencesKeys.CLAUDE_API_KEY] ?: ""
    }

    suspend fun setFinnhubApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FINNHUB_API_KEY] = apiKey
        }
    }

    suspend fun setClaudeApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CLAUDE_API_KEY] = apiKey
        }
    }

    suspend fun hasApiKeys(): Boolean {
        val data = context.dataStore.data.first()
        val finnhubKey = data[PreferencesKeys.FINNHUB_API_KEY] ?: ""
        val claudeKey = data[PreferencesKeys.CLAUDE_API_KEY] ?: ""
        return finnhubKey.isNotBlank() && claudeKey.isNotBlank()
    }

    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
