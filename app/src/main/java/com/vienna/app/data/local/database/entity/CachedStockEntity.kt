package com.vienna.app.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_stocks")
data class CachedStockEntity(
    @PrimaryKey
    @ColumnInfo(name = "symbol")
    val symbol: String,

    @ColumnInfo(name = "data_json")
    val dataJson: String,

    @ColumnInfo(name = "cached_at")
    val cachedAt: Long
)
