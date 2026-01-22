package com.vienna.app.domain.model

data class PortfolioHolding(
    val id: Long = 0,
    val symbol: String,
    val companyName: String,
    val purchasePrice: Double,
    val purchaseDate: Long,
    val shares: Int = 1,
    val currentPrice: Double? = null
) {
    val totalCost: Double
        get() = purchasePrice * shares

    val currentValue: Double?
        get() = currentPrice?.let { it * shares }

    val gainLoss: Double?
        get() = currentValue?.let { it - totalCost }

    val gainLossPercent: Double?
        get() = gainLoss?.let { (it / totalCost) * 100 }

    val daysHeld: Long
        get() = (System.currentTimeMillis() - purchaseDate) / (1000 * 60 * 60 * 24)

    val isPositive: Boolean
        get() = (gainLoss ?: 0.0) >= 0
}
