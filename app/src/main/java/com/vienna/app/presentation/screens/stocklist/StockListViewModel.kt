package com.vienna.app.presentation.screens.stocklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vienna.app.data.local.ErrorLogManager
import com.vienna.app.domain.model.Algorithm
import com.vienna.app.domain.model.AlgorithmPrediction
import com.vienna.app.domain.model.Stock
import com.vienna.app.domain.usecase.GetAlgorithmsUseCase
import com.vienna.app.domain.usecase.GetAlgorithmPredictionsUseCase
import com.vienna.app.domain.usecase.ManagePortfolioUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StockListUiState(
    val algorithms: List<Algorithm> = emptyList(),
    val selectedAlgorithm: Algorithm? = null,
    val predictions: List<AlgorithmPrediction> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingAlgorithms: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false
)

@HiltViewModel
class StockListViewModel @Inject constructor(
    private val getAlgorithmsUseCase: GetAlgorithmsUseCase,
    private val getAlgorithmPredictionsUseCase: GetAlgorithmPredictionsUseCase,
    private val managePortfolioUseCase: ManagePortfolioUseCase,
    private val errorLogManager: ErrorLogManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockListUiState())
    val uiState: StateFlow<StockListUiState> = _uiState.asStateFlow()

    init {
        loadAlgorithms()
    }

    private fun loadAlgorithms() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingAlgorithms = true) }

            getAlgorithmsUseCase()
                .onSuccess { algorithms ->
                    _uiState.update {
                        it.copy(
                            algorithms = algorithms,
                            isLoadingAlgorithms = false
                        )
                    }
                    // Auto-select first algorithm
                    if (algorithms.isNotEmpty() && _uiState.value.selectedAlgorithm == null) {
                        selectAlgorithm(algorithms.first())
                    }
                }
                .onFailure { exception ->
                    errorLogManager.logError("StockListViewModel", "Failed to load algorithms", exception)
                    _uiState.update {
                        it.copy(
                            isLoadingAlgorithms = false,
                            error = exception.message ?: "Failed to load algorithms"
                        )
                    }
                }
        }
    }

    fun selectAlgorithm(algorithm: Algorithm) {
        if (algorithm != _uiState.value.selectedAlgorithm) {
            _uiState.update { it.copy(selectedAlgorithm = algorithm, predictions = emptyList()) }
            loadPredictions(algorithm.id)
        }
    }

    private fun loadPredictions(algorithmId: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = !forceRefresh, isRefreshing = forceRefresh, error = null) }

            getAlgorithmPredictionsUseCase(algorithmId, forceRefresh)
                .onSuccess { predictions ->
                    _uiState.update {
                        it.copy(
                            predictions = predictions,
                            isLoading = false,
                            isRefreshing = false,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    errorLogManager.logError("StockListViewModel", "Failed to load predictions", exception)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = exception.message ?: "Failed to load predictions"
                        )
                    }
                }
        }
    }

    fun refresh() {
        _uiState.value.selectedAlgorithm?.let { algorithm ->
            loadPredictions(algorithm.id, forceRefresh = true)
        }
    }

    fun addToPortfolio(stock: Stock, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                managePortfolioUseCase.addToPortfolio(
                    symbol = stock.symbol,
                    companyName = stock.companyName,
                    price = stock.currentPrice
                )
                onSuccess()
            } catch (e: Exception) {
                errorLogManager.logError("StockListViewModel", "Failed to add stock to portfolio", e)
                onError(e.message ?: "Failed to add stock")
            }
        }
    }
}
