package com.vienna.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClaudeRequest(
    @SerialName("model")
    val model: String = "claude-sonnet-4-5-20250514",
    @SerialName("max_tokens")
    val maxTokens: Int = 1024,
    @SerialName("messages")
    val messages: List<ClaudeMessage>
)

@Serializable
data class ClaudeMessage(
    @SerialName("role")
    val role: String,
    @SerialName("content")
    val content: String
)

@Serializable
data class ClaudeResponse(
    @SerialName("id")
    val id: String = "",
    @SerialName("type")
    val type: String = "",
    @SerialName("role")
    val role: String = "",
    @SerialName("content")
    val content: List<ClaudeContent> = emptyList(),
    @SerialName("model")
    val model: String = "",
    @SerialName("stop_reason")
    val stopReason: String? = null
)

@Serializable
data class ClaudeContent(
    @SerialName("type")
    val type: String = "",
    @SerialName("text")
    val text: String = ""
)
