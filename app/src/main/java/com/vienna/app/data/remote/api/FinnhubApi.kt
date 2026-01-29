package com.vienna.app.data.remote.api

import com.vienna.app.data.remote.dto.FinnhubCandleResponse
import com.vienna.app.data.remote.dto.FinnhubCompanyProfile
import com.vienna.app.data.remote.dto.FinnhubQuoteResponse
import com.vienna.app.data.remote.dto.FinnhubSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Finnhub Stock API
 * Free tier: 60 API calls/minute
 * Docs: https://finnhub.io/docs/api
 */
interface FinnhubApi {

    /**
     * Get real-time quote data for US stocks
     */
    @GET("quote")
    suspend fun getQuote(
        @Query("symbol") symbol: String,
        @Query("token") apiKey: String
    ): FinnhubQuoteResponse

    /**
     * Search for symbols
     */
    @GET("search")
    suspend fun searchSymbol(
        @Query("q") query: String,
        @Query("token") apiKey: String
    ): FinnhubSearchResponse

    /**
     * Get stock candles (OHLCV data)
     * @param resolution Supported resolution: 1, 5, 15, 30, 60, D, W, M
     * @param from UNIX timestamp (seconds)
     * @param to UNIX timestamp (seconds)
     */
    @GET("stock/candle")
    suspend fun getStockCandles(
        @Query("symbol") symbol: String,
        @Query("resolution") resolution: String,
        @Query("from") from: Long,
        @Query("to") to: Long,
        @Query("token") apiKey: String
    ): FinnhubCandleResponse

    /**
     * Get company profile
     */
    @GET("stock/profile2")
    suspend fun getCompanyProfile(
        @Query("symbol") symbol: String,
        @Query("token") apiKey: String
    ): FinnhubCompanyProfile

    companion object {
        const val BASE_URL = "https://finnhub.io/api/v1/"
    }
}
