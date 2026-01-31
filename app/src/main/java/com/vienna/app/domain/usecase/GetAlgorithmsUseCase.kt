package com.vienna.app.domain.usecase

import com.vienna.app.domain.model.Algorithm
import com.vienna.app.domain.repository.AlgorithmRepository
import javax.inject.Inject

class GetAlgorithmsUseCase @Inject constructor(
    private val algorithmRepository: AlgorithmRepository
) {
    suspend operator fun invoke(): Result<List<Algorithm>> {
        return algorithmRepository.getAlgorithms()
    }
}
