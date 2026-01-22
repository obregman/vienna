package com.vienna.app.domain.model

data class SearchResult(
    val symbol: String,
    val name: String,
    val type: String,
    val region: String,
    val matchScore: Double
)
