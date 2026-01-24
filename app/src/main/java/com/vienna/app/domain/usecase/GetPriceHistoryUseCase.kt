package com.vienna.app.domain.usecase

import com.vienna.app.domain.model.PriceHistory
import com.vienna.app.domain.model.TimeRange
import com.vienna.app.domain.repository.StockRepository
import javax.inject.Inject

class GetPriceHistoryUseCase @Inject constructor(
    private val stockRepository: StockRepository
) {
    suspend operator fun invoke(symbol: String, timeRange: TimeRange): Result<PriceHistory> {
        return stockRepository.getPriceHistory(symbol, timeRange)
    }
}
