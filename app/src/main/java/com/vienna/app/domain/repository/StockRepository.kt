package com.vienna.app.domain.repository

import com.vienna.app.domain.model.MarketData
import com.vienna.app.domain.model.PriceHistory
import com.vienna.app.domain.model.SearchResult
import com.vienna.app.domain.model.Stock
import com.vienna.app.domain.model.TimeRange
import kotlinx.coroutines.flow.Flow

interface StockRepository {
    suspend fun getMarketData(forceRefresh: Boolean = false): Result<MarketData>
    suspend fun getStockQuote(symbol: String): Result<Stock>
    suspend fun searchStocks(query: String): Result<List<SearchResult>>
    suspend fun getPriceHistory(symbol: String, timeRange: TimeRange): Result<PriceHistory>
    fun getRecentSearches(): Flow<List<String>>
    suspend fun saveSearchQuery(query: String)
    suspend fun clearSearchHistory()
}
