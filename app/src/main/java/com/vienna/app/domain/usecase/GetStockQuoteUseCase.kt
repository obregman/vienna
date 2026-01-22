package com.vienna.app.domain.usecase

import com.vienna.app.domain.model.Stock
import com.vienna.app.domain.repository.StockRepository
import javax.inject.Inject

class GetStockQuoteUseCase @Inject constructor(
    private val stockRepository: StockRepository
) {
    suspend operator fun invoke(symbol: String): Result<Stock> {
        return stockRepository.getStockQuote(symbol)
    }
}
