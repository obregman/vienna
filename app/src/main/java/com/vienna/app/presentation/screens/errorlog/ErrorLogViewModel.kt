package com.vienna.app.presentation.screens.errorlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vienna.app.data.local.ErrorLogManager
import com.vienna.app.data.local.database.entity.ErrorLogEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ErrorLogUiState(
    val errors: List<ErrorLogEntity> = emptyList(),
    val isLoading: Boolean = true,
    val expandedErrorId: Long? = null
)

@HiltViewModel
class ErrorLogViewModel @Inject constructor(
    private val errorLogManager: ErrorLogManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ErrorLogUiState())
    val uiState: StateFlow<ErrorLogUiState> = _uiState.asStateFlow()

    init {
        loadErrors()
    }

    private fun loadErrors() {
        viewModelScope.launch {
            errorLogManager.getErrors().collect { errors ->
                _uiState.update {
                    it.copy(
                        errors = errors,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun toggleExpanded(errorId: Long) {
        _uiState.update {
            it.copy(
                expandedErrorId = if (it.expandedErrorId == errorId) null else errorId
            )
        }
    }

    fun deleteError(errorId: Long) {
        viewModelScope.launch {
            errorLogManager.deleteError(errorId)
        }
    }

    fun clearAllErrors() {
        viewModelScope.launch {
            errorLogManager.clearErrors()
        }
    }
}
