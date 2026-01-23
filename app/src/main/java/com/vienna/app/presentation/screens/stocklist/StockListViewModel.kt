package com.vienna.app.presentation.screens.stocklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vienna.app.data.local.ErrorLogManager
import com.vienna.app.domain.model.SortOption
import com.vienna.app.domain.model.Stock
import com.vienna.app.domain.usecase.GetMarketDataUseCase
import com.vienna.app.domain.usecase.ManagePortfolioUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StockListUiState(
    val stocks: List<Stock> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val sortOption: SortOption = SortOption.VOLUME,
    val isRefreshing: Boolean = false
)

@HiltViewModel
class StockListViewModel @Inject constructor(
    private val getMarketDataUseCase: GetMarketDataUseCase,
    private val managePortfolioUseCase: ManagePortfolioUseCase,
    private val errorLogManager: ErrorLogManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockListUiState())
    val uiState: StateFlow<StockListUiState> = _uiState.asStateFlow()

    init {
        loadStocks()
    }

    fun loadStocks(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = !forceRefresh, isRefreshing = forceRefresh, error = null) }

            getMarketDataUseCase(_uiState.value.sortOption, forceRefresh)
                .onSuccess { stocks ->
                    _uiState.update {
                        it.copy(
                            stocks = stocks,
                            isLoading = false,
                            isRefreshing = false,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    errorLogManager.logError("StockListViewModel", "Failed to load stocks", exception)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = exception.message ?: "Failed to load stocks"
                        )
                    }
                }
        }
    }

    fun setSortOption(option: SortOption) {
        if (option != _uiState.value.sortOption) {
            _uiState.update { it.copy(sortOption = option) }
            loadStocks()
        }
    }

    fun refresh() {
        loadStocks(forceRefresh = true)
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
