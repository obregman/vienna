package com.vienna.app.data.repository

import com.vienna.app.BuildConfig
import com.vienna.app.data.local.database.dao.CachedStockDao
import com.vienna.app.data.local.database.dao.SearchHistoryDao
import com.vienna.app.data.local.database.entity.CachedStockEntity
import com.vienna.app.data.local.database.entity.SearchHistoryEntity
import com.vienna.app.data.remote.api.StockApi
import com.vienna.app.data.remote.dto.GlobalQuoteDto
import com.vienna.app.data.remote.dto.MarketMoverDto
import com.vienna.app.domain.model.MarketData
import com.vienna.app.domain.model.SearchResult
import com.vienna.app.domain.model.Stock
import com.vienna.app.domain.repository.StockRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class StockRepositoryImpl @Inject constructor(
    private val stockApi: StockApi,
    private val cachedStockDao: CachedStockDao,
    private val searchHistoryDao: SearchHistoryDao,
    private val json: Json
) : StockRepository {

    private var cachedMarketData: MarketData? = null
    private var marketDataTimestamp: Long = 0
    private val cacheValidityMs = 5 * 60 * 1000L // 5 minutes

    override suspend fun getMarketData(forceRefresh: Boolean): Result<MarketData> {
        return try {
            val now = System.currentTimeMillis()
            if (!forceRefresh && cachedMarketData != null && (now - marketDataTimestamp) < cacheValidityMs) {
                return Result.success(cachedMarketData!!)
            }

            val response = stockApi.getTopGainersLosers(apiKey = BuildConfig.ALPHA_VANTAGE_API_KEY)
            val marketData = MarketData(
                topGainers = response.topGainers.map { it.toStock() },
                topLosers = response.topLosers.map { it.toStock() },
                mostActive = response.mostActivelyTraded.map { it.toStock() },
                lastUpdated = now
            )
            cachedMarketData = marketData
            marketDataTimestamp = now
            Result.success(marketData)
        } catch (e: Exception) {
            cachedMarketData?.let { Result.success(it) } ?: Result.failure(e)
        }
    }

    override suspend fun getStockQuote(symbol: String): Result<Stock> {
        return try {
            // Check cache first
            val cached = cachedStockDao.getCachedStock(symbol)
            val now = System.currentTimeMillis()
            if (cached != null && (now - cached.cachedAt) < 60_000) { // 1 minute cache
                val quote = json.decodeFromString<GlobalQuoteDto>(cached.dataJson)
                return Result.success(quote.toStock())
            }

            val response = stockApi.getGlobalQuote(symbol = symbol, apiKey = BuildConfig.ALPHA_VANTAGE_API_KEY)
            val quote = response.globalQuote ?: return Result.failure(Exception("Stock not found"))

            // Cache the result
            cachedStockDao.insertCachedStock(
                CachedStockEntity(
                    symbol = symbol,
                    dataJson = json.encodeToString(quote),
                    cachedAt = now
                )
            )

            Result.success(quote.toStock())
        } catch (e: Exception) {
            // Try to return cached data on error
            val cached = cachedStockDao.getCachedStock(symbol)
            if (cached != null) {
                val quote = json.decodeFromString<GlobalQuoteDto>(cached.dataJson)
                Result.success(quote.toStock())
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun searchStocks(query: String): Result<List<SearchResult>> {
        return try {
            val response = stockApi.searchSymbol(keywords = query, apiKey = BuildConfig.ALPHA_VANTAGE_API_KEY)
            val results = response.bestMatches.map { match ->
                SearchResult(
                    symbol = match.symbol,
                    name = match.name,
                    type = match.type,
                    region = match.region,
                    matchScore = match.matchScore.toDoubleOrNull() ?: 0.0
                )
            }
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getRecentSearches(): Flow<List<String>> {
        return searchHistoryDao.getRecentSearches().map { entities ->
            entities.map { it.query }
        }
    }

    override suspend fun saveSearchQuery(query: String) {
        searchHistoryDao.insertSearch(
            SearchHistoryEntity(
                query = query,
                searchedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun clearSearchHistory() {
        searchHistoryDao.clearAll()
    }

    private fun MarketMoverDto.toStock(): Stock {
        return Stock(
            symbol = ticker,
            companyName = ticker, // API doesn't provide company name
            currentPrice = price.toDoubleOrNull() ?: 0.0,
            priceChange = changeAmount.toDoubleOrNull() ?: 0.0,
            percentChange = changePercentage.replace("%", "").toDoubleOrNull() ?: 0.0,
            volume = volume.toLongOrNull() ?: 0L
        )
    }

    private fun GlobalQuoteDto.toStock(): Stock {
        return Stock(
            symbol = symbol,
            companyName = symbol, // Will be enriched later
            currentPrice = price.toDoubleOrNull() ?: 0.0,
            priceChange = change.toDoubleOrNull() ?: 0.0,
            percentChange = changePercent.replace("%", "").toDoubleOrNull() ?: 0.0,
            volume = volume.toLongOrNull() ?: 0L,
            dayHigh = high.toDoubleOrNull() ?: 0.0,
            dayLow = low.toDoubleOrNull() ?: 0.0
        )
    }
}
