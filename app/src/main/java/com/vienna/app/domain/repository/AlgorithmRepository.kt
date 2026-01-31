package com.vienna.app.domain.repository

import com.vienna.app.domain.model.Algorithm
import com.vienna.app.domain.model.AlgorithmPrediction

interface AlgorithmRepository {
    suspend fun getAlgorithms(): Result<List<Algorithm>>
    suspend fun getPredictions(algorithmId: String, forceRefresh: Boolean = false): Result<List<AlgorithmPrediction>>
}
