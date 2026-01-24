package com.vienna.app.presentation.screens.stockdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vienna.app.data.local.ErrorLogManager
import com.vienna.app.domain.model.PricePoint
import com.vienna.app.domain.model.Stock
import com.vienna.app.domain.model.TimeRange
import com.vienna.app.domain.usecase.GetPriceHistoryUseCase
import com.vienna.app.domain.usecase.GetStockQuoteUseCase
import com.vienna.app.domain.usecase.ManagePortfolioUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StockDetailUiState(
    val symbol: String = "",
    val companyName: String = "",
    val stock: Stock? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isInPortfolio: Boolean = false,
    val addedToPortfolio: Boolean = false,
    val priceHistory: List<PricePoint> = emptyList(),
    val selectedTimeRange: TimeRange = TimeRange.MONTH_1,
    val isChartLoading: Boolean = false
)

@HiltViewModel
class StockDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getStockQuoteUseCase: GetStockQuoteUseCase,
    private val getPriceHistoryUseCase: GetPriceHistoryUseCase,
    private val managePortfolioUseCase: ManagePortfolioUseCase,
    private val errorLogManager: ErrorLogManager
) : ViewModel() {

    private val symbol: String = savedStateHandle.get<String>("symbol") ?: ""
    private val companyName: String = savedStateHandle.get<String>("companyName") ?: symbol

    private val _uiState = MutableStateFlow(
        StockDetailUiState(
            symbol = symbol,
            companyName = companyName
        )
    )
    val uiState: StateFlow<StockDetailUiState> = _uiState.asStateFlow()

    init {
        loadStockDetails()
        checkPortfolioStatus()
        loadPriceHistory()
    }

    private fun loadStockDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getStockQuoteUseCase(symbol)
                .onSuccess { stock ->
                    _uiState.update {
                        it.copy(
                            stock = stock.copy(companyName = companyName),
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    errorLogManager.logError("StockDetailViewModel", "Failed to load stock details for $symbol", exception)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load stock details"
                        )
                    }
                }
        }
    }

    private fun checkPortfolioStatus() {
        viewModelScope.launch {
            val isInPortfolio = managePortfolioUseCase.isInPortfolio(symbol)
            _uiState.update { it.copy(isInPortfolio = isInPortfolio) }
        }
    }

    fun addToPortfolio() {
        viewModelScope.launch {
            val stock = _uiState.value.stock ?: return@launch

            try {
                managePortfolioUseCase.addToPortfolio(
                    symbol = stock.symbol,
                    companyName = _uiState.value.companyName,
                    price = stock.currentPrice
                )
                _uiState.update {
                    it.copy(
                        isInPortfolio = true,
                        addedToPortfolio = true
                    )
                }
            } catch (e: Exception) {
                errorLogManager.logError("StockDetailViewModel", "Failed to add stock to portfolio", e)
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun refresh() {
        loadStockDetails()
        checkPortfolioStatus()
        loadPriceHistory()
    }

    fun clearAddedFlag() {
        _uiState.update { it.copy(addedToPortfolio = false) }
    }

    private fun loadPriceHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isChartLoading = true) }

            getPriceHistoryUseCase(symbol, _uiState.value.selectedTimeRange)
                .onSuccess { history ->
                    _uiState.update {
                        it.copy(
                            priceHistory = history.prices,
                            isChartLoading = false
                        )
                    }
                }
                .onFailure { exception ->
                    errorLogManager.logError("StockDetailViewModel", "Failed to load price history", exception)
                    _uiState.update {
                        it.copy(isChartLoading = false)
                    }
                }
        }
    }

    fun setTimeRange(timeRange: TimeRange) {
        _uiState.update { it.copy(selectedTimeRange = timeRange) }
        loadPriceHistory()
    }
}
