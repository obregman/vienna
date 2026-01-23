package com.vienna.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vienna.app.data.local.database.dao.AnalysisCacheDao
import com.vienna.app.data.local.database.dao.CachedStockDao
import com.vienna.app.data.local.database.dao.ErrorLogDao
import com.vienna.app.data.local.database.dao.PortfolioDao
import com.vienna.app.data.local.database.dao.SearchHistoryDao
import com.vienna.app.data.local.database.entity.AnalysisCacheEntity
import com.vienna.app.data.local.database.entity.CachedStockEntity
import com.vienna.app.data.local.database.entity.ErrorLogEntity
import com.vienna.app.data.local.database.entity.PortfolioHoldingEntity
import com.vienna.app.data.local.database.entity.SearchHistoryEntity

@Database(
    entities = [
        PortfolioHoldingEntity::class,
        CachedStockEntity::class,
        SearchHistoryEntity::class,
        AnalysisCacheEntity::class,
        ErrorLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class ViennaDatabase : RoomDatabase() {
    abstract fun portfolioDao(): PortfolioDao
    abstract fun cachedStockDao(): CachedStockDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun analysisCacheDao(): AnalysisCacheDao
    abstract fun errorLogDao(): ErrorLogDao

    companion object {
        const val DATABASE_NAME = "vienna_database"
    }
}
