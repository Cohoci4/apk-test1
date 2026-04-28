package com.foodenhancer.domain.usecase

import com.foodenhancer.domain.model.FoodImage
import com.foodenhancer.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetHistoryUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    operator fun invoke(): Flow<List<FoodImage>> = historyRepository.getAll()
}
