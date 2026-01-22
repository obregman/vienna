package com.vienna.app.data.repository

import com.vienna.app.BuildConfig
import com.vienna.app.data.local.database.dao.AnalysisCacheDao
import com.vienna.app.data.local.database.entity.AnalysisCacheEntity
import com.vienna.app.data.remote.api.ClaudeApi
import com.vienna.app.data.remote.dto.ClaudeMessage
import com.vienna.app.data.remote.dto.ClaudeRequest
import com.vienna.app.domain.model.NewsArticle
import com.vienna.app.domain.model.Sentiment
import com.vienna.app.domain.model.StockAnalysis
import com.vienna.app.domain.repository.AnalysisRepository
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class AnalysisRepositoryImpl @Inject constructor(
    private val claudeApi: ClaudeApi,
    private val analysisCacheDao: AnalysisCacheDao,
    private val json: Json
) : AnalysisRepository {

    private val cacheValidityMs = 6 * 60 * 60 * 1000L // 6 hours

    override suspend fun getStockAnalysis(
        symbol: String,
        companyName: String,
        forceRefresh: Boolean
    ): Result<StockAnalysis> {
        return try {
            val now = System.currentTimeMillis()

            // Check cache first
            if (!forceRefresh) {
                val cached = analysisCacheDao.getValidAnalysis(symbol, now)
                if (cached != null) {
                    val analysis = json.decodeFromString<CachedAnalysis>(cached.analysisJson)
                    return Result.success(analysis.toDomain(symbol))
                }
            }

            // Generate new analysis
            val prompt = buildAnalysisPrompt(symbol, companyName)
            val request = ClaudeRequest(
                messages = listOf(
                    ClaudeMessage(role = "user", content = prompt)
                )
            )

            val response = claudeApi.createMessage(
                apiKey = BuildConfig.CLAUDE_API_KEY,
                request = request
            )

            val responseText = response.content.firstOrNull()?.text
                ?: return Result.failure(Exception("Empty response from AI"))

            val analysis = parseAnalysisResponse(symbol, responseText, now)

            // Cache the result
            val cachedAnalysis = CachedAnalysis(
                summary = analysis.summary,
                sentiment = analysis.sentiment.name,
                keyPoints = analysis.keyPoints,
                generatedAt = analysis.generatedAt,
                cachedUntil = analysis.cachedUntil
            )
            analysisCacheDao.insertAnalysis(
                AnalysisCacheEntity(
                    symbol = symbol,
                    analysisJson = json.encodeToString(cachedAnalysis),
                    generatedAt = now,
                    expiresAt = now + cacheValidityMs
                )
            )

            Result.success(analysis)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun invalidateCache(symbol: String) {
        analysisCacheDao.deleteAnalysis(symbol)
    }

    private fun buildAnalysisPrompt(symbol: String, companyName: String): String {
        return """
            Analyze the stock $symbol ($companyName). Please provide a structured analysis with the following:

            1. **Summary**: A brief 2-3 sentence overview of the company and its current market position.

            2. **Sentiment**: Based on general market conditions and the company's fundamentals, classify the outlook as BULLISH, BEARISH, or NEUTRAL.

            3. **Key Points**: List 3-5 important investment considerations as bullet points.

            Please format your response as follows:
            SUMMARY: [Your summary here]
            SENTIMENT: [BULLISH/BEARISH/NEUTRAL]
            KEY_POINTS:
            - [Point 1]
            - [Point 2]
            - [Point 3]
        """.trimIndent()
    }

    private fun parseAnalysisResponse(symbol: String, response: String, timestamp: Long): StockAnalysis {
        val summaryMatch = Regex("SUMMARY:\\s*(.+?)(?=SENTIMENT:|$)", RegexOption.DOT_MATCHES_ALL)
            .find(response)?.groupValues?.get(1)?.trim() ?: "Analysis not available"

        val sentimentMatch = Regex("SENTIMENT:\\s*(BULLISH|BEARISH|NEUTRAL)", RegexOption.IGNORE_CASE)
            .find(response)?.groupValues?.get(1)?.uppercase() ?: "NEUTRAL"

        val keyPointsMatch = Regex("KEY_POINTS:(.+?)$", RegexOption.DOT_MATCHES_ALL)
            .find(response)?.groupValues?.get(1) ?: ""

        val keyPoints = keyPointsMatch.lines()
            .map { it.trim() }
            .filter { it.startsWith("-") || it.startsWith("•") }
            .map { it.removePrefix("-").removePrefix("•").trim() }
            .filter { it.isNotBlank() }

        return StockAnalysis(
            symbol = symbol,
            summary = summaryMatch,
            sentiment = when (sentimentMatch) {
                "BULLISH" -> Sentiment.BULLISH
                "BEARISH" -> Sentiment.BEARISH
                else -> Sentiment.NEUTRAL
            },
            keyPoints = keyPoints.ifEmpty { listOf("Analysis details not available") },
            newsArticles = emptyList(), // News would be fetched separately in a real app
            generatedAt = timestamp,
            cachedUntil = timestamp + cacheValidityMs
        )
    }

    @Serializable
    private data class CachedAnalysis(
        val summary: String,
        val sentiment: String,
        val keyPoints: List<String>,
        val generatedAt: Long,
        val cachedUntil: Long
    ) {
        fun toDomain(symbol: String): StockAnalysis {
            return StockAnalysis(
                symbol = symbol,
                summary = summary,
                sentiment = when (sentiment) {
                    "BULLISH" -> Sentiment.BULLISH
                    "BEARISH" -> Sentiment.BEARISH
                    else -> Sentiment.NEUTRAL
                },
                keyPoints = keyPoints,
                newsArticles = emptyList(),
                generatedAt = generatedAt,
                cachedUntil = cachedUntil
            )
        }
    }
}
