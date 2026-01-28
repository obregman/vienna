package com.vienna.app.presentation.screens.holdingdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vienna.app.data.local.ErrorLogManager
import com.vienna.app.domain.model.PortfolioHolding
import com.vienna.app.domain.model.PricePoint
import com.vienna.app.domain.model.TimeRange
import com.vienna.app.domain.repository.PortfolioRepository
import com.vienna.app.domain.usecase.GetPriceHistoryUseCase
import com.vienna.app.domain.usecase.GetStockQuoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HoldingDetailUiState(
    val holding: PortfolioHolding? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val priceHistory: List<PricePoint> = emptyList(),
    val priceHistorySincePurchase: List<PricePoint> = emptyList(),
    val isChartLoading: Boolean = false
)

@HiltViewModel
class HoldingDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val portfolioRepository: PortfolioRepository,
    private val getStockQuoteUseCase: GetStockQuoteUseCase,
    private val getPriceHistoryUseCase: GetPriceHistoryUseCase,
    private val errorLogManager: ErrorLogManager
) : ViewModel() {

    private val holdingId: Long = savedStateHandle.get<Long>("holdingId") ?: 0L

    private val _uiState = MutableStateFlow(HoldingDetailUiState())
    val uiState: StateFlow<HoldingDetailUiState> = _uiState.asStateFlow()

    init {
        loadHoldingDetails()
    }

    private fun loadHoldingDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val holding = portfolioRepository.getHoldingById(holdingId)
                if (holding == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Holding not found"
                        )
                    }
                    return@launch
                }

                // Get current price
                val currentPrice = getStockQuoteUseCase(holding.symbol)
                    .getOrNull()?.currentPrice

                val holdingWithPrice = holding.copy(currentPrice = currentPrice)

                _uiState.update {
                    it.copy(
                        holding = holdingWithPrice,
                        isLoading = false
                    )
                }

                // Load price history
                loadPriceHistory(holding)
            } catch (e: Exception) {
                errorLogManager.logError("HoldingDetailViewModel", "Failed to load holding details", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load holding details"
                    )
                }
            }
        }
    }

    private fun loadPriceHistory(holding: PortfolioHolding) {
        viewModelScope.launch {
            _uiState.update { it.copy(isChartLoading = true) }

            // Determine time range based on days held
            val timeRange = when {
                holding.daysHeld <= 7 -> TimeRange.WEEK_1
                holding.daysHeld <= 30 -> TimeRange.MONTH_1
                holding.daysHeld <= 180 -> TimeRange.MONTH_6
                else -> TimeRange.ALL
            }

            getPriceHistoryUseCase(holding.symbol, timeRange)
                .onSuccess { history ->
                    // Filter prices to only show since purchase date
                    val pricesSincePurchase = history.prices.filter {
                        it.timestamp >= holding.purchaseDate
                    }

                    _uiState.update {
                        it.copy(
                            priceHistory = history.prices,
                            priceHistorySincePurchase = pricesSincePurchase,
                            isChartLoading = false
                        )
                    }
                }
                .onFailure { exception ->
                    errorLogManager.logError("HoldingDetailViewModel", "Failed to load price history", exception)
                    _uiState.update {
                        it.copy(isChartLoading = false)
                    }
                }
        }
    }

    fun refresh() {
        loadHoldingDetails()
    }
}
