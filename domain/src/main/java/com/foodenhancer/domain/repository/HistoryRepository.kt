package com.foodenhancer.domain.repository

import com.foodenhancer.domain.model.FoodImage
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    suspend fun save(image: FoodImage)
    fun getAll(): Flow<List<FoodImage>>
    suspend fun delete(id: String)
}
