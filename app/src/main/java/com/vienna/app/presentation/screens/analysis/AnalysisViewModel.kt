package com.vienna.app.presentation.screens.analysis

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vienna.app.domain.model.Sentiment
import com.vienna.app.domain.model.StockAnalysis
import com.vienna.app.domain.usecase.GetStockAnalysisUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnalysisUiState(
    val symbol: String = "",
    val companyName: String = "",
    val analysis: StockAnalysis? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getStockAnalysisUseCase: GetStockAnalysisUseCase
) : ViewModel() {

    private val symbol: String = savedStateHandle.get<String>("symbol") ?: ""
    private val companyName: String = savedStateHandle.get<String>("companyName") ?: symbol

    private val _uiState = MutableStateFlow(
        AnalysisUiState(
            symbol = symbol,
            companyName = companyName
        )
    )
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    init {
        loadAnalysis()
    }

    fun loadAnalysis(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getStockAnalysisUseCase(symbol, companyName, forceRefresh)
                .onSuccess { analysis ->
                    _uiState.update {
                        it.copy(
                            analysis = analysis,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to generate analysis"
                        )
                    }
                }
        }
    }

    fun refresh() {
        loadAnalysis(forceRefresh = true)
    }
}
