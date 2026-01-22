package com.vienna.app.domain.model

data class MarketData(
    val topGainers: List<Stock>,
    val topLosers: List<Stock>,
    val mostActive: List<Stock>,
    val lastUpdated: Long = System.currentTimeMillis()
)

enum class SortOption {
    VOLUME,
    GAINERS,
    LOSERS
}
