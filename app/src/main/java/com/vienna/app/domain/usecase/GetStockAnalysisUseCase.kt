package com.vienna.app.domain.usecase

import com.vienna.app.domain.model.StockAnalysis
import com.vienna.app.domain.repository.AnalysisRepository
import javax.inject.Inject

class GetStockAnalysisUseCase @Inject constructor(
    private val analysisRepository: AnalysisRepository
) {
    suspend operator fun invoke(
        symbol: String,
        companyName: String,
        forceRefresh: Boolean = false
    ): Result<StockAnalysis> {
        return analysisRepository.getStockAnalysis(symbol, companyName, forceRefresh)
    }
}
