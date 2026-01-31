package com.vienna.app.domain.model

/**
 * Represents a stock prediction algorithm that uses various signals to predict stocks.
 */
data class Algorithm(
    val id: String,
    val name: String,
    val description: String,
    val signals: List<SignalType>,
    val accuracy: Double? = null // Historical accuracy percentage if available
)

/**
 * Types of signals used by prediction algorithms.
 */
enum class SignalType(val displayName: String) {
    WEB_SEARCH("Web Search Trends"),
    TWITTER_TRENDS("Twitter/X Trends"),
    STOCK_PERFORMANCE("Stock Performance"),
    NEWS_SENTIMENT("News Sentiment"),
    VOLUME_ANALYSIS("Volume Analysis")
}

/**
 * Represents a stock prediction from an algorithm.
 */
data class AlgorithmPrediction(
    val stock: Stock,
    val algorithm: Algorithm,
    val confidence: Double, // 0.0 to 1.0
    val signal: String, // Description of why this stock was predicted
    val predictedAt: Long = System.currentTimeMillis()
)
