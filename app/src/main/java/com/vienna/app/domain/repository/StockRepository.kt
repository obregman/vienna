package com.vienna.app.domain.repository

import com.vienna.app.domain.model.MarketData
import com.vienna.app.domain.model.SearchResult
import com.vienna.app.domain.model.Stock
import kotlinx.coroutines.flow.Flow

interface StockRepository {
    suspend fun getMarketData(forceRefresh: Boolean = false): Result<MarketData>
    suspend fun getStockQuote(symbol: String): Result<Stock>
    suspend fun searchStocks(query: String): Result<List<SearchResult>>
    fun getRecentSearches(): Flow<List<String>>
    suspend fun saveSearchQuery(query: String)
    suspend fun clearSearchHistory()
}
