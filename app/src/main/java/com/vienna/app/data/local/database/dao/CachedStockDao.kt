package com.vienna.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vienna.app.data.local.database.entity.CachedStockEntity

@Dao
interface CachedStockDao {

    @Query("SELECT * FROM cached_stocks WHERE symbol = :symbol")
    suspend fun getCachedStock(symbol: String): CachedStockEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedStock(stock: CachedStockEntity)

    @Query("DELETE FROM cached_stocks WHERE cached_at < :timestamp")
    suspend fun deleteExpiredCache(timestamp: Long)

    @Query("DELETE FROM cached_stocks")
    suspend fun clearAll()
}
