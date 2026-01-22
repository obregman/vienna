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
        val ALPHA_VANTAGE_API_KEY = stringPreferencesKey("alpha_vantage_api_key")
        val CLAUDE_API_KEY = stringPreferencesKey("claude_api_key")
    }

    val alphaVantageApiKey: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ALPHA_VANTAGE_API_KEY] ?: ""
        }

    val claudeApiKey: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.CLAUDE_API_KEY] ?: ""
        }

    suspend fun getAlphaVantageApiKey(): String {
        return context.dataStore.data.first()[PreferencesKeys.ALPHA_VANTAGE_API_KEY] ?: ""
    }

    suspend fun getClaudeApiKey(): String {
        return context.dataStore.data.first()[PreferencesKeys.CLAUDE_API_KEY] ?: ""
    }

    suspend fun setAlphaVantageApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ALPHA_VANTAGE_API_KEY] = apiKey
        }
    }

    suspend fun setClaudeApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CLAUDE_API_KEY] = apiKey
        }
    }

    suspend fun hasApiKeys(): Boolean {
        val data = context.dataStore.data.first()
        val alphaVantageKey = data[PreferencesKeys.ALPHA_VANTAGE_API_KEY] ?: ""
        val claudeKey = data[PreferencesKeys.CLAUDE_API_KEY] ?: ""
        return alphaVantageKey.isNotBlank() && claudeKey.isNotBlank()
    }

    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
