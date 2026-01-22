package com.vienna.app.domain.usecase

import com.vienna.app.domain.model.PortfolioHolding
import com.vienna.app.domain.repository.PortfolioRepository
import com.vienna.app.domain.repository.StockRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetPortfolioUseCase @Inject constructor(
    private val portfolioRepository: PortfolioRepository,
    private val stockRepository: StockRepository
) {
    operator fun invoke(): Flow<List<PortfolioHolding>> {
        return portfolioRepository.getAllHoldings()
    }

    suspend fun getWithCurrentPrices(holdings: List<PortfolioHolding>): List<PortfolioHolding> {
        return holdings.map { holding ->
            val quote = stockRepository.getStockQuote(holding.symbol)
            holding.copy(currentPrice = quote.getOrNull()?.currentPrice)
        }
    }
}
