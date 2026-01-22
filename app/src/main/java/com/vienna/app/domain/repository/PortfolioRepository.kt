package com.vienna.app.domain.repository

import com.vienna.app.domain.model.PortfolioHolding
import kotlinx.coroutines.flow.Flow

interface PortfolioRepository {
    fun getAllHoldings(): Flow<List<PortfolioHolding>>
    suspend fun getHoldingsBySymbol(symbol: String): List<PortfolioHolding>
    suspend fun addHolding(symbol: String, companyName: String, price: Double): Long
    suspend fun removeHolding(id: Long)
    suspend fun isInPortfolio(symbol: String): Boolean
}
