package com.vienna.app.domain.usecase

import com.vienna.app.domain.model.AlgorithmPrediction
import com.vienna.app.domain.repository.AlgorithmRepository
import javax.inject.Inject

class GetAlgorithmPredictionsUseCase @Inject constructor(
    private val algorithmRepository: AlgorithmRepository
) {
    suspend operator fun invoke(
        algorithmId: String,
        forceRefresh: Boolean = false
    ): Result<List<AlgorithmPrediction>> {
        return algorithmRepository.getPredictions(algorithmId, forceRefresh)
    }
}
