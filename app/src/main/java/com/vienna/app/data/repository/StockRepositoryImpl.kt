package com.vienna.app.data.repository

import com.vienna.app.data.local.ErrorLogManager
import com.vienna.app.data.local.database.dao.CachedStockDao
import com.vienna.app.data.local.database.dao.SearchHistoryDao
import com.vienna.app.data.local.datastore.SettingsDataStore
import com.vienna.app.data.local.database.entity.CachedStockEntity
import com.vienna.app.data.local.database.entity.SearchHistoryEntity
import com.vienna.app.data.remote.api.StockApi
import com.vienna.app.data.remote.dto.DailyPriceDto
import com.vienna.app.data.remote.dto.GlobalQuoteDto
import com.vienna.app.data.remote.dto.MarketMoverDto
import com.vienna.app.domain.model.MarketData
import com.vienna.app.domain.model.PriceHistory
import com.vienna.app.domain.model.PricePoint
import com.vienna.app.domain.model.SearchResult
import com.vienna.app.domain.model.Stock
import com.vienna.app.domain.model.TimeRange
import com.vienna.app.domain.repository.StockRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class StockRepositoryImpl @Inject constructor(
    private val stockApi: StockApi,
    private val cachedStockDao: CachedStockDao,
    private val searchHistoryDao: SearchHistoryDao,
    private val settingsDataStore: SettingsDataStore,
    private val json: Json,
    private val errorLogManager: ErrorLogManager
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

            val apiKey = settingsDataStore.getAlphaVantageApiKey()
            if (apiKey.isBlank()) {
                return Result.failure(Exception("Please configure your Alpha Vantage API key in Settings"))
            }

            val response = stockApi.getTopGainersLosers(apiKey = apiKey)
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
            errorLogManager.logError("StockRepository", "Failed to get market data", e)
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

            val apiKey = settingsDataStore.getAlphaVantageApiKey()
            if (apiKey.isBlank()) {
                return Result.failure(Exception("Please configure your Alpha Vantage API key in Settings"))
            }

            val response = stockApi.getGlobalQuote(symbol = symbol, apiKey = apiKey)
            val quote = response.globalQuote

            // Alpha Vantage returns an empty GlobalQuote object (not null) when stock is not found
            if (quote == null || quote.symbol.isBlank()) {
                return Result.failure(Exception("Stock '$symbol' not found. Please verify the symbol is correct."))
            }

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
            errorLogManager.logError("StockRepository", "Failed to get stock quote for $symbol", e)
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
            val apiKey = settingsDataStore.getAlphaVantageApiKey()
            if (apiKey.isBlank()) {
                return Result.failure(Exception("Please configure your Alpha Vantage API key in Settings"))
            }

            val response = stockApi.searchSymbol(keywords = query, apiKey = apiKey)
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
            errorLogManager.logError("StockRepository", "Failed to search stocks for query: $query", e)
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

    override suspend fun getPriceHistory(symbol: String, timeRange: TimeRange): Result<PriceHistory> {
        return try {
            val cacheKey = "${symbol}_history"
            val now = System.currentTimeMillis()
            val oneDayMs = 24 * 60 * 60 * 1000L

            // Check cache - history data is valid for 1 day
            val cached = cachedStockDao.getCachedStock(cacheKey)
            if (cached != null && (now - cached.cachedAt) < oneDayMs) {
                val cachedData = json.decodeFromString<CachedPriceHistory>(cached.dataJson)
                val filteredPrices = filterByTimeRange(cachedData.prices, timeRange)
                return Result.success(
                    PriceHistory(
                        symbol = symbol,
                        prices = filteredPrices,
                        timeRange = timeRange
                    )
                )
            }

            val apiKey = settingsDataStore.getAlphaVantageApiKey()
            if (apiKey.isBlank()) {
                return Result.failure(Exception("Please configure your Alpha Vantage API key in Settings"))
            }

            // Fetch daily history with retry logic for rate limiting
            val maxRetries = 3
            var lastException: Exception? = null

            for (attempt in 0 until maxRetries) {
                val response = stockApi.getTimeSeriesDaily(
                    symbol = symbol,
                    outputSize = "compact",
                    apiKey = apiKey
                )

                // Check for rate limiting response
                if (response.note != null || response.information != null) {
                    if (attempt < maxRetries - 1) {
                        // Wait with exponential backoff before retry
                        val delayMs = (attempt + 1) * 2000L // 2s, 4s, 6s
                        delay(delayMs)
                        continue
                    } else {
                        return Result.failure(Exception("API rate limit reached. Please try again in a moment."))
                    }
                }

                val timeSeries = response.timeSeries
                if (timeSeries == null) {
                    if (attempt < maxRetries - 1) {
                        // Retry if no data returned
                        val delayMs = (attempt + 1) * 2000L
                        delay(delayMs)
                        continue
                    } else {
                        return Result.failure(Exception("No price history available"))
                    }
                }

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val allPrices = timeSeries.entries
                    .mapNotNull { (dateStr, priceData) ->
                        try {
                            val date = dateFormat.parse(dateStr)
                            PricePoint(
                                timestamp = date?.time ?: 0L,
                                price = priceData.close.toDoubleOrNull() ?: 0.0,
                                volume = priceData.volume.toLongOrNull() ?: 0L
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    .sortedBy { it.timestamp }

                // Cache the full history
                val cachedPriceHistory = CachedPriceHistory(
                    prices = allPrices.map { CachedPricePoint(it.timestamp, it.price, it.volume) }
                )
                cachedStockDao.insertCachedStock(
                    CachedStockEntity(
                        symbol = cacheKey,
                        dataJson = json.encodeToString(cachedPriceHistory),
                        cachedAt = now
                    )
                )

                val filteredPrices = filterByTimeRange(cachedPriceHistory.prices, timeRange)
                return Result.success(
                    PriceHistory(
                        symbol = symbol,
                        prices = filteredPrices,
                        timeRange = timeRange
                    )
                )
            }

            Result.failure(lastException ?: Exception("Failed to load price history after retries"))
        } catch (e: Exception) {
            errorLogManager.logError("StockRepository", "Failed to get price history for $symbol", e)
            Result.failure(e)
        }
    }

    private fun filterByTimeRange(prices: List<CachedPricePoint>, timeRange: TimeRange): List<PricePoint> {
        val now = System.currentTimeMillis()
        val cutoff = if (timeRange.days > 0) {
            now - (timeRange.days * 24 * 60 * 60 * 1000L)
        } else {
            0L // All time
        }
        return prices
            .filter { it.timestamp >= cutoff }
            .map { PricePoint(it.timestamp, it.price, it.volume) }
    }

    @Serializable
    private data class CachedPriceHistory(
        val prices: List<CachedPricePoint>
    )

    @Serializable
    private data class CachedPricePoint(
        val timestamp: Long,
        val price: Double,
        val volume: Long
    )

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
