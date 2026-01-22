package com.vienna.app.data.remote.api

import com.vienna.app.data.remote.dto.ClaudeRequest
import com.vienna.app.data.remote.dto.ClaudeResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ClaudeApi {

    @POST("v1/messages")
    suspend fun createMessage(
        @Header("x-api-key") apiKey: String,
        @Header("anthropic-version") version: String = "2023-06-01",
        @Header("content-type") contentType: String = "application/json",
        @Body request: ClaudeRequest
    ): ClaudeResponse

    companion object {
        const val BASE_URL = "https://api.anthropic.com/"
    }
}
