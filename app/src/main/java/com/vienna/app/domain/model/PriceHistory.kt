package com.vienna.app.domain.model

data class PricePoint(
    val timestamp: Long,
    val price: Double,
    val volume: Long = 0
)

data class PriceHistory(
    val symbol: String,
    val prices: List<PricePoint>,
    val timeRange: TimeRange
)

enum class TimeRange(val label: String, val days: Int) {
    DAY_1("1D", 1),
    WEEK_1("1W", 7),
    MONTH_1("1M", 30),
    MONTH_6("6M", 180),
    ALL("All", -1)
}
