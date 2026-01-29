package com.vienna.app.domain.usecase

import com.vienna.app.domain.model.PortfolioHolding
import com.vienna.app.domain.repository.PortfolioRepository
import javax.inject.Inject

class ManagePortfolioUseCase @Inject constructor(
    private val portfolioRepository: PortfolioRepository
) {
    suspend fun addToPortfolio(symbol: String, companyName: String, price: Double): Long {
        return portfolioRepository.addHolding(symbol, companyName, price)
    }

    suspend fun removeFromPortfolio(holdingId: Long) {
        portfolioRepository.removeHolding(holdingId)
    }

    suspend fun isInPortfolio(symbol: String): Boolean {
        return portfolioRepository.isInPortfolio(symbol)
    }

    suspend fun getHoldingBySymbol(symbol: String): PortfolioHolding? {
        return portfolioRepository.getHoldingsBySymbol(symbol).firstOrNull()
    }
}
