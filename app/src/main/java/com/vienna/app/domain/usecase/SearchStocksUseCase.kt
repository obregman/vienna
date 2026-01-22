package com.vienna.app.domain.usecase

import com.vienna.app.domain.model.SearchResult
import com.vienna.app.domain.repository.StockRepository
import javax.inject.Inject

class SearchStocksUseCase @Inject constructor(
    private val stockRepository: StockRepository
) {
    suspend operator fun invoke(query: String): Result<List<SearchResult>> {
        if (query.isBlank()) {
            return Result.success(emptyList())
        }
        val result = stockRepository.searchStocks(query)
        result.onSuccess {
            stockRepository.saveSearchQuery(query)
        }
        return result
    }
}
