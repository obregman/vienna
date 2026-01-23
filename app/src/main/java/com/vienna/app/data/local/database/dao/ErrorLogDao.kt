package com.vienna.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.vienna.app.data.local.database.entity.ErrorLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ErrorLogDao {

    @Query("SELECT * FROM error_log ORDER BY timestamp DESC")
    fun getAllErrors(): Flow<List<ErrorLogEntity>>

    @Query("SELECT * FROM error_log ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentErrors(limit: Int = 100): Flow<List<ErrorLogEntity>>

    @Insert
    suspend fun insertError(error: ErrorLogEntity)

    @Query("DELETE FROM error_log WHERE id = :id")
    suspend fun deleteError(id: Long)

    @Query("DELETE FROM error_log")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM error_log")
    suspend fun getErrorCount(): Int
}
