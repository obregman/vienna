package com.vienna.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vienna.app.data.local.database.entity.PortfolioHoldingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PortfolioDao {

    @Query("SELECT * FROM portfolio_holdings ORDER BY purchase_date DESC")
    fun getAllHoldings(): Flow<List<PortfolioHoldingEntity>>

    @Query("SELECT * FROM portfolio_holdings WHERE symbol = :symbol")
    suspend fun getHoldingsBySymbol(symbol: String): List<PortfolioHoldingEntity>

    @Query("SELECT * FROM portfolio_holdings WHERE id = :id")
    suspend fun getHoldingById(id: Long): PortfolioHoldingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolding(holding: PortfolioHoldingEntity): Long

    @Delete
    suspend fun deleteHolding(holding: PortfolioHoldingEntity)

    @Query("DELETE FROM portfolio_holdings WHERE id = :id")
    suspend fun deleteHoldingById(id: Long)

    @Query("SELECT COUNT(*) FROM portfolio_holdings WHERE symbol = :symbol")
    suspend fun getHoldingCountBySymbol(symbol: String): Int
}
