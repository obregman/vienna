package com.vienna.app.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vienna.app.data.local.datastore.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val alphaVantageApiKey: String = "",
    val claudeApiKey: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val alphaVantageKey = settingsDataStore.getAlphaVantageApiKey()
                val claudeKey = settingsDataStore.getClaudeApiKey()
                _uiState.update {
                    it.copy(
                        alphaVantageApiKey = alphaVantageKey,
                        claudeApiKey = claudeKey,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load settings"
                    )
                }
            }
        }
    }

    fun onAlphaVantageApiKeyChanged(key: String) {
        _uiState.update { it.copy(alphaVantageApiKey = key, saveSuccess = false) }
    }

    fun onClaudeApiKeyChanged(key: String) {
        _uiState.update { it.copy(claudeApiKey = key, saveSuccess = false) }
    }

    fun saveSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                settingsDataStore.setAlphaVantageApiKey(_uiState.value.alphaVantageApiKey.trim())
                settingsDataStore.setClaudeApiKey(_uiState.value.claudeApiKey.trim())
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveSuccess = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = "Failed to save settings"
                    )
                }
            }
        }
    }

    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
