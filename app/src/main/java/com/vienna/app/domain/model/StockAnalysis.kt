package com.vienna.app.domain.model

data class StockAnalysis(
    val symbol: String,
    val summary: String,
    val sentiment: Sentiment,
    val keyPoints: List<String>,
    val newsArticles: List<NewsArticle>,
    val generatedAt: Long,
    val cachedUntil: Long
)

enum class Sentiment {
    BULLISH,
    BEARISH,
    NEUTRAL
}

data class NewsArticle(
    val title: String,
    val source: String,
    val url: String,
    val publishedAt: Long,
    val snippet: String,
    val sentiment: Sentiment? = null
)
