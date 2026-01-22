package com.vienna.app.domain.usecase

import com.vienna.app.domain.model.MarketData
import com.vienna.app.domain.model.SortOption
import com.vienna.app.domain.model.Stock
import com.vienna.app.domain.repository.StockRepository
import javax.inject.Inject

class GetMarketDataUseCase @Inject constructor(
    private val stockRepository: StockRepository
) {
    suspend operator fun invoke(
        sortOption: SortOption,
        forceRefresh: Boolean = false
    ): Result<List<Stock>> {
        return stockRepository.getMarketData(forceRefresh).map { marketData ->
            when (sortOption) {
                SortOption.VOLUME -> marketData.mostActive
                SortOption.GAINERS -> marketData.topGainers
                SortOption.LOSERS -> marketData.topLosers
            }
        }
    }
}
