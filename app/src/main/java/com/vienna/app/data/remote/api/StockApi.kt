package com.vienna.app.data.remote.api

import com.vienna.app.data.remote.dto.CompanyOverviewResponse
import com.vienna.app.data.remote.dto.GlobalQuoteResponse
import com.vienna.app.data.remote.dto.SymbolSearchResponse
import com.vienna.app.data.remote.dto.TopGainersLosersResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface StockApi {

    @GET("query")
    suspend fun getTopGainersLosers(
        @Query("function") function: String = "TOP_GAINERS_LOSERS",
        @Query("apikey") apiKey: String
    ): TopGainersLosersResponse

    @GET("query")
    suspend fun getGlobalQuote(
        @Query("function") function: String = "GLOBAL_QUOTE",
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String
    ): GlobalQuoteResponse

    @GET("query")
    suspend fun searchSymbol(
        @Query("function") function: String = "SYMBOL_SEARCH",
        @Query("keywords") keywords: String,
        @Query("apikey") apiKey: String
    ): SymbolSearchResponse

    @GET("query")
    suspend fun getCompanyOverview(
        @Query("function") function: String = "OVERVIEW",
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String
    ): CompanyOverviewResponse

    companion object {
        const val BASE_URL = "https://www.alphavantage.co/"
    }
}
