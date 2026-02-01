package com.vienna.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Finnhub Quote Response
 * GET /quote?symbol=AAPL
 */
@Serializable
data class FinnhubQuoteResponse(
    @SerialName("c")
    val currentPrice: Double = 0.0,
    @SerialName("d")
    val change: Double? = null,
    @SerialName("dp")
    val percentChange: Double? = null,
    @SerialName("h")
    val highPrice: Double = 0.0,
    @SerialName("l")
    val lowPrice: Double = 0.0,
    @SerialName("o")
    val openPrice: Double = 0.0,
    @SerialName("pc")
    val previousClose: Double = 0.0,
    @SerialName("t")
    val timestamp: Long = 0
)

/**
 * Finnhub Symbol Search Response
 * GET /search?q=apple
 */
@Serializable
data class FinnhubSearchResponse(
    @SerialName("count")
    val count: Int = 0,
    @SerialName("result")
    val result: List<FinnhubSearchResult> = emptyList()
)

@Serializable
data class FinnhubSearchResult(
    @SerialName("description")
    val description: String = "",
    @SerialName("displaySymbol")
    val displaySymbol: String = "",
    @SerialName("symbol")
    val symbol: String = "",
    @SerialName("type")
    val type: String = ""
)

/**
 * Finnhub Stock Candles Response (OHLCV data)
 * GET /stock/candle?symbol=AAPL&resolution=D&from=xxx&to=xxx
 */
@Serializable
data class FinnhubCandleResponse(
    @SerialName("c")
    val closePrices: List<Double>? = null,
    @SerialName("h")
    val highPrices: List<Double>? = null,
    @SerialName("l")
    val lowPrices: List<Double>? = null,
    @SerialName("o")
    val openPrices: List<Double>? = null,
    @SerialName("t")
    val timestamps: List<Long>? = null,
    @SerialName("v")
    val volumes: List<Double>? = null,
    @SerialName("s")
    val status: String = "ok"
)

/**
 * Finnhub Company Profile Response
 * GET /stock/profile2?symbol=AAPL
 */
@Serializable
data class FinnhubCompanyProfile(
    @SerialName("country")
    val country: String = "",
    @SerialName("currency")
    val currency: String = "",
    @SerialName("exchange")
    val exchange: String = "",
    @SerialName("finnhubIndustry")
    val industry: String = "",
    @SerialName("ipo")
    val ipo: String = "",
    @SerialName("logo")
    val logo: String = "",
    @SerialName("marketCapitalization")
    val marketCapitalization: Double = 0.0,
    @SerialName("name")
    val name: String = "",
    @SerialName("phone")
    val phone: String = "",
    @SerialName("shareOutstanding")
    val shareOutstanding: Double = 0.0,
    @SerialName("ticker")
    val ticker: String = "",
    @SerialName("weburl")
    val weburl: String = ""
)

/**
 * Finnhub Market Status Response
 * GET /stock/market-status?exchange=US
 */
@Serializable
data class FinnhubMarketStatus(
    @SerialName("exchange")
    val exchange: String = "",
    @SerialName("holiday")
    val holiday: String? = null,
    @SerialName("isOpen")
    val isOpen: Boolean = false,
    @SerialName("session")
    val session: String = "",
    @SerialName("timezone")
    val timezone: String = ""
)

/**
 * Finnhub Symbol List Response
 * GET /stock/symbol?exchange=US
 */
@Serializable
data class FinnhubSymbol(
    @SerialName("currency")
    val currency: String = "",
    @SerialName("description")
    val description: String = "",
    @SerialName("displaySymbol")
    val displaySymbol: String = "",
    @SerialName("figi")
    val figi: String = "",
    @SerialName("mic")
    val mic: String = "",
    @SerialName("symbol")
    val symbol: String = "",
    @SerialName("type")
    val type: String = ""
)
