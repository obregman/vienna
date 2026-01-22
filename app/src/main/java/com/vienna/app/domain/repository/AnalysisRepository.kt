package com.vienna.app.domain.repository

import com.vienna.app.domain.model.StockAnalysis

interface AnalysisRepository {
    suspend fun getStockAnalysis(symbol: String, companyName: String, forceRefresh: Boolean = false): Result<StockAnalysis>
    suspend fun invalidateCache(symbol: String)
}
