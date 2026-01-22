package com.vienna.app.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "analysis_cache")
data class AnalysisCacheEntity(
    @PrimaryKey
    @ColumnInfo(name = "symbol")
    val symbol: String,

    @ColumnInfo(name = "analysis_json")
    val analysisJson: String,

    @ColumnInfo(name = "generated_at")
    val generatedAt: Long,

    @ColumnInfo(name = "expires_at")
    val expiresAt: Long
)
