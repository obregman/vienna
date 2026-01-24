package com.vienna.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GlobalQuoteResponse(
    @SerialName("Global Quote")
    val globalQuote: GlobalQuoteDto? = null
)

@Serializable
data class GlobalQuoteDto(
    @SerialName("01. symbol")
    val symbol: String = "",
    @SerialName("02. open")
    val open: String = "0",
    @SerialName("03. high")
    val high: String = "0",
    @SerialName("04. low")
    val low: String = "0",
    @SerialName("05. price")
    val price: String = "0",
    @SerialName("06. volume")
    val volume: String = "0",
    @SerialName("07. latest trading day")
    val latestTradingDay: String = "",
    @SerialName("08. previous close")
    val previousClose: String = "0",
    @SerialName("09. change")
    val change: String = "0",
    @SerialName("10. change percent")
    val changePercent: String = "0%"
)

@Serializable
data class TopGainersLosersResponse(
    @SerialName("metadata")
    val metadata: String? = null,
    @SerialName("last_updated")
    val lastUpdated: String? = null,
    @SerialName("top_gainers")
    val topGainers: List<MarketMoverDto> = emptyList(),
    @SerialName("top_losers")
    val topLosers: List<MarketMoverDto> = emptyList(),
    @SerialName("most_actively_traded")
    val mostActivelyTraded: List<MarketMoverDto> = emptyList()
)

@Serializable
data class MarketMoverDto(
    @SerialName("ticker")
    val ticker: String = "",
    @SerialName("price")
    val price: String = "0",
    @SerialName("change_amount")
    val changeAmount: String = "0",
    @SerialName("change_percentage")
    val changePercentage: String = "0%",
    @SerialName("volume")
    val volume: String = "0"
)

@Serializable
data class SymbolSearchResponse(
    @SerialName("bestMatches")
    val bestMatches: List<SearchMatchDto> = emptyList()
)

@Serializable
data class SearchMatchDto(
    @SerialName("1. symbol")
    val symbol: String = "",
    @SerialName("2. name")
    val name: String = "",
    @SerialName("3. type")
    val type: String = "",
    @SerialName("4. region")
    val region: String = "",
    @SerialName("5. marketOpen")
    val marketOpen: String = "",
    @SerialName("6. marketClose")
    val marketClose: String = "",
    @SerialName("7. timezone")
    val timezone: String = "",
    @SerialName("8. currency")
    val currency: String = "",
    @SerialName("9. matchScore")
    val matchScore: String = "0"
)

@Serializable
data class CompanyOverviewResponse(
    @SerialName("Symbol")
    val symbol: String = "",
    @SerialName("Name")
    val name: String = "",
    @SerialName("Description")
    val description: String = "",
    @SerialName("Exchange")
    val exchange: String = "",
    @SerialName("Sector")
    val sector: String = "",
    @SerialName("Industry")
    val industry: String = "",
    @SerialName("MarketCapitalization")
    val marketCapitalization: String = "0",
    @SerialName("52WeekHigh")
    val week52High: String = "0",
    @SerialName("52WeekLow")
    val week52Low: String = "0"
)

@Serializable
data class TimeSeriesDailyResponse(
    @SerialName("Meta Data")
    val metaData: TimeSeriesMetaData? = null,
    @SerialName("Time Series (Daily)")
    val timeSeries: Map<String, DailyPriceDto>? = null
)

@Serializable
data class TimeSeriesIntradayResponse(
    @SerialName("Meta Data")
    val metaData: TimeSeriesMetaData? = null,
    @SerialName("Time Series (5min)")
    val timeSeries: Map<String, DailyPriceDto>? = null
)

@Serializable
data class TimeSeriesMetaData(
    @SerialName("1. Information")
    val information: String = "",
    @SerialName("2. Symbol")
    val symbol: String = "",
    @SerialName("3. Last Refreshed")
    val lastRefreshed: String = ""
)

@Serializable
data class DailyPriceDto(
    @SerialName("1. open")
    val open: String = "0",
    @SerialName("2. high")
    val high: String = "0",
    @SerialName("3. low")
    val low: String = "0",
    @SerialName("4. close")
    val close: String = "0",
    @SerialName("5. volume")
    val volume: String = "0"
)
