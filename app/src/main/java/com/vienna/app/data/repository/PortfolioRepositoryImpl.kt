package com.vienna.app.data.repository

import com.vienna.app.data.local.database.dao.PortfolioDao
import com.vienna.app.data.local.database.entity.PortfolioHoldingEntity
import com.vienna.app.domain.model.PortfolioHolding
import com.vienna.app.domain.repository.PortfolioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PortfolioRepositoryImpl @Inject constructor(
    private val portfolioDao: PortfolioDao
) : PortfolioRepository {

    override fun getAllHoldings(): Flow<List<PortfolioHolding>> {
        return portfolioDao.getAllHoldings().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getHoldingsBySymbol(symbol: String): List<PortfolioHolding> {
        return portfolioDao.getHoldingsBySymbol(symbol).map { it.toDomain() }
    }

    override suspend fun addHolding(symbol: String, companyName: String, price: Double): Long {
        return portfolioDao.insertHolding(
            PortfolioHoldingEntity(
                symbol = symbol,
                companyName = companyName,
                purchasePrice = price,
                purchaseDate = System.currentTimeMillis(),
                shares = 1
            )
        )
    }

    override suspend fun removeHolding(id: Long) {
        portfolioDao.deleteHoldingById(id)
    }

    override suspend fun isInPortfolio(symbol: String): Boolean {
        return portfolioDao.getHoldingCountBySymbol(symbol) > 0
    }

    private fun PortfolioHoldingEntity.toDomain(): PortfolioHolding {
        return PortfolioHolding(
            id = id,
            symbol = symbol,
            companyName = companyName,
            purchasePrice = purchasePrice,
            purchaseDate = purchaseDate,
            shares = shares
        )
    }
}
