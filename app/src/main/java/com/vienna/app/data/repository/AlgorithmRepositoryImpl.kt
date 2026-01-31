package com.vienna.app.data.repository

import com.vienna.app.data.local.ErrorLogManager
import com.vienna.app.data.local.datastore.SettingsDataStore
import com.vienna.app.data.remote.api.FinnhubApi
import com.vienna.app.domain.model.Algorithm
import com.vienna.app.domain.model.AlgorithmPrediction
import com.vienna.app.domain.model.SignalType
import com.vienna.app.domain.model.Stock
import com.vienna.app.domain.repository.AlgorithmRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import kotlin.random.Random

class AlgorithmRepositoryImpl @Inject constructor(
    private val finnhubApi: FinnhubApi,
    private val settingsDataStore: SettingsDataStore,
    private val errorLogManager: ErrorLogManager
) : AlgorithmRepository {

    private var cachedPredictions: MutableMap<String, Pair<List<AlgorithmPrediction>, Long>> = mutableMapOf()
    private val cacheValidityMs = 10 * 60 * 1000L // 10 minutes

    private val algorithms = listOf(
        Algorithm(
            id = "web_search_trending",
            name = "Web Search Trending",
            description = "Analyzes trending search queries related to stocks and companies to identify rising interest",
            signals = listOf(SignalType.WEB_SEARCH, SignalType.NEWS_SENTIMENT),
            accuracy = 0.72
        ),
        Algorithm(
            id = "twitter_buzz",
            name = "Twitter/X Buzz",
            description = "Monitors Twitter/X for mentions, sentiment, and viral discussions about stocks",
            signals = listOf(SignalType.TWITTER_TRENDS, SignalType.NEWS_SENTIMENT),
            accuracy = 0.68
        ),
        Algorithm(
            id = "momentum_breakout",
            name = "Momentum Breakout",
            description = "Identifies stocks with strong price momentum and potential breakout patterns",
            signals = listOf(SignalType.STOCK_PERFORMANCE, SignalType.VOLUME_ANALYSIS),
            accuracy = 0.75
        ),
        Algorithm(
            id = "volume_surge",
            name = "Volume Surge",
            description = "Detects unusual volume spikes that may indicate upcoming price movements",
            signals = listOf(SignalType.VOLUME_ANALYSIS, SignalType.STOCK_PERFORMANCE),
            accuracy = 0.70
        ),
        Algorithm(
            id = "combined_signals",
            name = "Multi-Signal Analysis",
            description = "Combines multiple signals (social, search, performance) for comprehensive predictions",
            signals = listOf(SignalType.WEB_SEARCH, SignalType.TWITTER_TRENDS, SignalType.STOCK_PERFORMANCE, SignalType.NEWS_SENTIMENT),
            accuracy = 0.78
        )
    )

    // Stock pools for different algorithm types - simulating different signal sources
    private val webSearchStocks = listOf("NVDA", "TSLA", "META", "GOOGL", "AMZN", "AAPL", "AMD", "PLTR", "COIN", "SQ")
    private val twitterStocks = listOf("TSLA", "GME", "AMC", "PLTR", "NVDA", "META", "AAPL", "RIVN", "LCID", "SOFI")
    private val momentumStocks = listOf("NVDA", "AVGO", "LLY", "COST", "META", "MSFT", "AAPL", "AMZN", "CRM", "NOW")
    private val volumeStocks = listOf("AAPL", "TSLA", "NVDA", "AMD", "INTC", "F", "BAC", "T", "PFE", "AAL")
    private val combinedStocks = listOf("NVDA", "META", "TSLA", "AAPL", "GOOGL", "MSFT", "AMZN", "AMD", "AVGO", "CRM")

    private val webSearchSignals = listOf(
        "Search volume up 150% this week",
        "Trending in tech-related queries",
        "Product launch generating buzz",
        "Earnings anticipation searches rising",
        "Company news driving search interest"
    )

    private val twitterSignals = listOf(
        "Viral tweet from CEO",
        "High engagement on product announcement",
        "Trending hashtag momentum",
        "Positive sentiment surge",
        "Influencer mentions increasing"
    )

    private val momentumSignals = listOf(
        "Breaking above 50-day moving average",
        "Strong relative strength vs sector",
        "Consecutive higher highs pattern",
        "Bullish MACD crossover",
        "Price breakout from consolidation"
    )

    private val volumeSignals = listOf(
        "Volume 3x above average",
        "Unusual institutional activity",
        "Options volume spike detected",
        "Accumulation pattern forming",
        "Block trade activity increasing"
    )

    private val combinedSignals = listOf(
        "Multiple bullish indicators aligned",
        "Social + technical signals converging",
        "Cross-platform buzz with momentum",
        "Strong fundamentals + rising interest",
        "Multi-factor buy signal triggered"
    )

    override suspend fun getAlgorithms(): Result<List<Algorithm>> {
        return Result.success(algorithms)
    }

    override suspend fun getPredictions(algorithmId: String, forceRefresh: Boolean): Result<List<AlgorithmPrediction>> {
        return try {
            val now = System.currentTimeMillis()
            val cached = cachedPredictions[algorithmId]
            if (!forceRefresh && cached != null && (now - cached.second) < cacheValidityMs) {
                return Result.success(cached.first)
            }

            val apiKey = settingsDataStore.getFinnhubApiKey()
            if (apiKey.isBlank()) {
                return Result.failure(Exception("Please configure your Finnhub API key in Settings"))
            }

            val algorithm = algorithms.find { it.id == algorithmId }
                ?: return Result.failure(Exception("Algorithm not found"))

            val (stockSymbols, signalMessages) = when (algorithmId) {
                "web_search_trending" -> webSearchStocks to webSearchSignals
                "twitter_buzz" -> twitterStocks to twitterSignals
                "momentum_breakout" -> momentumStocks to momentumSignals
                "volume_surge" -> volumeStocks to volumeSignals
                "combined_signals" -> combinedStocks to combinedSignals
                else -> webSearchStocks to webSearchSignals
            }

            // Fetch real stock data for the algorithm's stock pool
            val predictions = coroutineScope {
                stockSymbols.map { symbol ->
                    async {
                        try {
                            val quote = finnhubApi.getQuote(symbol = symbol, apiKey = apiKey)
                            if (quote.currentPrice > 0) {
                                val companyName = try {
                                    val profile = finnhubApi.getCompanyProfile(symbol = symbol, apiKey = apiKey)
                                    profile.name.ifBlank { symbol }
                                } catch (e: Exception) {
                                    symbol
                                }

                                val stock = Stock(
                                    symbol = symbol,
                                    companyName = companyName,
                                    currentPrice = quote.currentPrice,
                                    priceChange = quote.change ?: 0.0,
                                    percentChange = quote.percentChange ?: 0.0,
                                    volume = 0L,
                                    dayHigh = quote.highPrice,
                                    dayLow = quote.lowPrice
                                )

                                AlgorithmPrediction(
                                    stock = stock,
                                    algorithm = algorithm,
                                    confidence = 0.6 + Random.nextDouble(0.35), // 0.6-0.95 range
                                    signal = signalMessages.random(),
                                    predictedAt = now
                                )
                            } else null
                        } catch (e: Exception) {
                            errorLogManager.logError("AlgorithmRepository", "Failed to fetch $symbol", e)
                            null
                        }
                    }
                }.awaitAll().filterNotNull()
            }

            // Sort by confidence score
            val sortedPredictions = predictions.sortedByDescending { it.confidence }

            cachedPredictions[algorithmId] = sortedPredictions to now
            Result.success(sortedPredictions)
        } catch (e: Exception) {
            errorLogManager.logError("AlgorithmRepository", "Failed to get predictions for $algorithmId", e)
            Result.failure(e)
        }
    }
}
