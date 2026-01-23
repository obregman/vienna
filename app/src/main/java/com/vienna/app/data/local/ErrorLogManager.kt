package com.vienna.app.data.local

import com.vienna.app.data.local.database.dao.ErrorLogDao
import com.vienna.app.data.local.database.entity.ErrorLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ErrorLogManager @Inject constructor(
    private val errorLogDao: ErrorLogDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun logError(tag: String, message: String, throwable: Throwable? = null) {
        scope.launch {
            val errorEntity = ErrorLogEntity(
                timestamp = System.currentTimeMillis(),
                tag = tag,
                message = message,
                stackTrace = throwable?.stackTraceToString()
            )
            errorLogDao.insertError(errorEntity)
        }
    }

    fun logError(tag: String, throwable: Throwable) {
        logError(tag, throwable.message ?: "Unknown error", throwable)
    }

    fun getErrors(): Flow<List<ErrorLogEntity>> {
        return errorLogDao.getAllErrors()
    }

    fun getRecentErrors(limit: Int = 100): Flow<List<ErrorLogEntity>> {
        return errorLogDao.getRecentErrors(limit)
    }

    suspend fun clearErrors() {
        errorLogDao.clearAll()
    }

    suspend fun deleteError(id: Long) {
        errorLogDao.deleteError(id)
    }

    suspend fun getErrorCount(): Int {
        return errorLogDao.getErrorCount()
    }
}
