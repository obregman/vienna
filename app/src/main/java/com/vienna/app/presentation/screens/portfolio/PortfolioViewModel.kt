package com.vienna.app.presentation.screens.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vienna.app.domain.model.PortfolioHolding
import com.vienna.app.domain.usecase.GetPortfolioUseCase
import com.vienna.app.domain.usecase.ManagePortfolioUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PortfolioUiState(
    val holdings: List<PortfolioHolding> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val totalValue: Double = 0.0,
    val totalGainLoss: Double = 0.0,
    val totalGainLossPercent: Double = 0.0
)

@HiltViewModel
class PortfolioViewModel @Inject constructor(
    private val getPortfolioUseCase: GetPortfolioUseCase,
    private val managePortfolioUseCase: ManagePortfolioUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PortfolioUiState())
    val uiState: StateFlow<PortfolioUiState> = _uiState.asStateFlow()

    init {
        loadPortfolio()
    }

    private fun loadPortfolio() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getPortfolioUseCase().collect { holdings ->
                // Fetch current prices for holdings
                val holdingsWithPrices = getPortfolioUseCase.getWithCurrentPrices(holdings)
                updateState(holdingsWithPrices)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }

            val holdings = _uiState.value.holdings
            val holdingsWithPrices = getPortfolioUseCase.getWithCurrentPrices(holdings)
            updateState(holdingsWithPrices)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    private fun updateState(holdings: List<PortfolioHolding>) {
        val totalCost = holdings.sumOf { it.totalCost }
        val totalValue = holdings.sumOf { it.currentValue ?: it.totalCost }
        val totalGainLoss = totalValue - totalCost
        val totalGainLossPercent = if (totalCost > 0) (totalGainLoss / totalCost) * 100 else 0.0

        _uiState.update {
            it.copy(
                holdings = holdings,
                isLoading = false,
                totalValue = totalValue,
                totalGainLoss = totalGainLoss,
                totalGainLossPercent = totalGainLossPercent
            )
        }
    }

    fun removeHolding(holding: PortfolioHolding) {
        viewModelScope.launch {
            try {
                managePortfolioUseCase.removeFromPortfolio(holding.id)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
