package com.vienna.app.data.repository

import com.vienna.app.data.local.ErrorLogManager
import com.vienna.app.data.local.database.dao.CachedStockDao
import com.vienna.app.data.local.database.dao.SearchHistoryDao
import com.vienna.app.data.local.datastore.SettingsDataStore
import com.vienna.app.data.local.database.entity.CachedStockEntity
import com.vienna.app.data.local.database.entity.SearchHistoryEntity
import com.vienna.app.data.remote.api.FinnhubApi
import com.vienna.app.domain.model.MarketData
import com.vienna.app.domain.model.PriceHistory
import com.vienna.app.domain.model.PricePoint
import com.vienna.app.domain.model.SearchResult
import com.vienna.app.domain.model.Stock
import com.vienna.app.domain.model.TimeRange
import com.vienna.app.domain.repository.StockRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class StockRepositoryImpl @Inject constructor(
    private val finnhubApi: FinnhubApi,
    private val cachedStockDao: CachedStockDao,
    private val searchHistoryDao: SearchHistoryDao,
    private val settingsDataStore: SettingsDataStore,
    private val json: Json,
    private val errorLogManager: ErrorLogManager
) : StockRepository {

    private var cachedMarketData: MarketData? = null
    private var marketDataTimestamp: Long = 0
    private val cacheValidityMs = 5 * 60 * 1000L // 5 minutes

    // Popular stocks for market overview (since Finnhub doesn't have top movers endpoint)
    private val popularStocks = listOf(
        "AAPL", "MSFT", "GOOGL", "AMZN", "NVDA", "META", "TSLA", "BRK.B",
        "JPM", "V", "UNH", "XOM", "JNJ", "WMT", "MA", "PG", "HD", "CVX",
        "MRK", "ABBV", "KO", "PEP", "BAC", "COST", "LLY", "TMO", "AVGO",
        "MCD", "DIS", "CSCO", "ADBE", "ACN", "NKE", "AMD", "INTC", "CRM"
    )

    override suspend fun getMarketData(forceRefresh: Boolean): Result<MarketData> {
        return try {
            val now = System.currentTimeMillis()
            if (!forceRefresh && cachedMarketData != null && (now - marketDataTimestamp) < cacheValidityMs) {
                return Result.success(cachedMarketData!!)
            }

            val apiKey = settingsDataStore.getFinnhubApiKey()
            if (apiKey.isBlank()) {
                return Result.failure(Exception("Please configure your Finnhub API key in Settings"))
            }

            // Fetch quotes for popular stocks in parallel
            val stocks = coroutineScope {
                popularStocks.take(20).map { symbol ->
                    async {
                        try {
                            val quote = finnhubApi.getQuote(symbol = symbol, apiKey = apiKey)
                            if (quote.currentPrice > 0) {
                                Stock(
                                    symbol = symbol,
                                    companyName = symbol,
                                    currentPrice = quote.currentPrice,
                                    priceChange = quote.change ?: 0.0,
                                    percentChange = quote.percentChange ?: 0.0,
                                    volume = 0L, // Finnhub quote doesn't include volume
                                    dayHigh = quote.highPrice,
                                    dayLow = quote.lowPrice
                                )
                            } else null
                        } catch (e: Exception) {
                            errorLogManager.logError("StockRepository", "Failed to fetch $symbol", e)
                            null
                        }
                    }
                }.awaitAll().filterNotNull()
            }

            // Sort to create gainers, losers, and most active lists
            val topGainers = stocks.filter { it.percentChange > 0 }
                .sortedByDescending { it.percentChange }
                .take(10)

            val topLosers = stocks.filter { it.percentChange < 0 }
                .sortedBy { it.percentChange }
                .take(10)

            val mostActive = stocks.sortedByDescending { kotlin.math.abs(it.percentChange) }
                .take(10)

            val marketData = MarketData(
                topGainers = topGainers,
                topLosers = topLosers,
                mostActive = mostActive,
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
                val cachedQuote = json.decodeFromString<CachedQuoteData>(cached.dataJson)
                return Result.success(cachedQuote.toStock())
            }

            val apiKey = settingsDataStore.getFinnhubApiKey()
            if (apiKey.isBlank()) {
                return Result.failure(Exception("Please configure your Finnhub API key in Settings"))
            }

            val quote = finnhubApi.getQuote(symbol = symbol, apiKey = apiKey)

            // Finnhub returns zeros for invalid symbols
            if (quote.currentPrice == 0.0 && quote.previousClose == 0.0) {
                return Result.failure(Exception("Stock '$symbol' not found. Please verify the symbol is correct."))
            }

            // Try to get company name from profile
            val companyName = try {
                val profile = finnhubApi.getCompanyProfile(symbol = symbol, apiKey = apiKey)
                profile.name.ifBlank { symbol }
            } catch (e: Exception) {
                symbol
            }

            val cachedQuoteData = CachedQuoteData(
                symbol = symbol,
                companyName = companyName,
                currentPrice = quote.currentPrice,
                priceChange = quote.change ?: 0.0,
                percentChange = quote.percentChange ?: 0.0,
                dayHigh = quote.highPrice,
                dayLow = quote.lowPrice
            )

            // Cache the result
            cachedStockDao.insertCachedStock(
                CachedStockEntity(
                    symbol = symbol,
                    dataJson = json.encodeToString(cachedQuoteData),
                    cachedAt = now
                )
            )

            Result.success(cachedQuoteData.toStock())
        } catch (e: Exception) {
            errorLogManager.logError("StockRepository", "Failed to get stock quote for $symbol", e)
            // Try to return cached data on error
            val cached = cachedStockDao.getCachedStock(symbol)
            if (cached != null) {
                val cachedQuote = json.decodeFromString<CachedQuoteData>(cached.dataJson)
                Result.success(cachedQuote.toStock())
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun searchStocks(query: String): Result<List<SearchResult>> {
        return try {
            val apiKey = settingsDataStore.getFinnhubApiKey()
            if (apiKey.isBlank()) {
                return Result.failure(Exception("Please configure your Finnhub API key in Settings"))
            }

            val response = finnhubApi.searchSymbol(query = query, apiKey = apiKey)
            val results = response.result
                .filter { it.type == "Common Stock" || it.type.isEmpty() }
                .map { match ->
                    SearchResult(
                        symbol = match.symbol,
                        name = match.description,
                        type = match.type.ifBlank { "Stock" },
                        region = "", // Finnhub doesn't provide region in search
                        matchScore = 1.0
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
            val cacheKey = "${symbol}_history_${timeRange.days}"
            val now = System.currentTimeMillis()
            val oneDayMs = 24 * 60 * 60 * 1000L

            // Check cache - history data is valid for 1 day
            val cached = cachedStockDao.getCachedStock(cacheKey)
            if (cached != null && (now - cached.cachedAt) < oneDayMs) {
                val cachedData = json.decodeFromString<CachedPriceHistory>(cached.dataJson)
                return Result.success(
                    PriceHistory(
                        symbol = symbol,
                        prices = cachedData.prices.map { PricePoint(it.timestamp, it.price, it.volume) },
                        timeRange = timeRange
                    )
                )
            }

            val apiKey = settingsDataStore.getFinnhubApiKey()
            if (apiKey.isBlank()) {
                return Result.failure(Exception("Please configure your Finnhub API key in Settings"))
            }

            // Calculate from/to timestamps based on time range
            val toTimestamp = now / 1000 // Convert to seconds
            val daysBack = if (timeRange.days > 0) timeRange.days else 365 // Default to 1 year for "All"
            val fromTimestamp = toTimestamp - (daysBack * 24 * 60 * 60L)

            // Use daily resolution for most time ranges
            val resolution = when {
                timeRange.days <= 7 -> "60" // Hourly for week or less
                else -> "D" // Daily for longer periods
            }

            val response = finnhubApi.getStockCandles(
                symbol = symbol,
                resolution = resolution,
                from = fromTimestamp,
                to = toTimestamp,
                apiKey = apiKey
            )

            if (response.status != "ok" || response.closePrices == null || response.timestamps == null) {
                return Result.failure(Exception("No price history available for $symbol"))
            }

            val prices = response.timestamps.indices.mapNotNull { i ->
                val timestamp = (response.timestamps[i] * 1000) // Convert to milliseconds
                val price = response.closePrices.getOrNull(i) ?: return@mapNotNull null
                val volume = response.volumes?.getOrNull(i) ?: 0L
                PricePoint(timestamp = timestamp, price = price, volume = volume)
            }.sortedBy { it.timestamp }

            // Cache the history
            val cachedPriceHistory = CachedPriceHistory(
                prices = prices.map { CachedPricePoint(it.timestamp, it.price, it.volume) }
            )
            cachedStockDao.insertCachedStock(
                CachedStockEntity(
                    symbol = cacheKey,
                    dataJson = json.encodeToString(cachedPriceHistory),
                    cachedAt = now
                )
            )

            Result.success(
                PriceHistory(
                    symbol = symbol,
                    prices = prices,
                    timeRange = timeRange
                )
            )
        } catch (e: Exception) {
            errorLogManager.logError("StockRepository", "Failed to get price history for $symbol", e)
            Result.failure(e)
        }
    }

    @Serializable
    private data class CachedQuoteData(
        val symbol: String,
        val companyName: String,
        val currentPrice: Double,
        val priceChange: Double,
        val percentChange: Double,
        val dayHigh: Double = 0.0,
        val dayLow: Double = 0.0
    ) {
        fun toStock(): Stock = Stock(
            symbol = symbol,
            companyName = companyName,
            currentPrice = currentPrice,
            priceChange = priceChange,
            percentChange = percentChange,
            volume = 0L,
            dayHigh = dayHigh,
            dayLow = dayLow
        )
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
}
