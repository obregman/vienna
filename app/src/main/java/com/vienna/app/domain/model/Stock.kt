package com.vienna.app.domain.model

data class Stock(
    val symbol: String,
    val companyName: String,
    val currentPrice: Double,
    val priceChange: Double,
    val percentChange: Double,
    val volume: Long,
    val marketCap: Long? = null,
    val dayHigh: Double = 0.0,
    val dayLow: Double = 0.0,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val isPositiveChange: Boolean
        get() = priceChange >= 0
}
