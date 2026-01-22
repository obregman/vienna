package com.vienna.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vienna.app.data.local.database.entity.AnalysisCacheEntity

@Dao
interface AnalysisCacheDao {

    @Query("SELECT * FROM analysis_cache WHERE symbol = :symbol AND expires_at > :currentTime")
    suspend fun getValidAnalysis(symbol: String, currentTime: Long): AnalysisCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysis(analysis: AnalysisCacheEntity)

    @Query("DELETE FROM analysis_cache WHERE expires_at < :currentTime")
    suspend fun deleteExpiredCache(currentTime: Long)

    @Query("DELETE FROM analysis_cache WHERE symbol = :symbol")
    suspend fun deleteAnalysis(symbol: String)

    @Query("DELETE FROM analysis_cache")
    suspend fun clearAll()
}
