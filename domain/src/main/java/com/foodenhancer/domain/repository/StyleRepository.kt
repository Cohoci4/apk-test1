package com.foodenhancer.domain.repository

import com.foodenhancer.domain.model.EnhancementStyle
import com.foodenhancer.domain.model.StyleCategory
import kotlinx.coroutines.flow.Flow

interface StyleRepository {
    fun getCategories(): Flow<List<StyleCategory>>
    fun getStylesByCategory(categoryId: String): Flow<List<EnhancementStyle>>
    fun getAllStyles(): Flow<List<EnhancementStyle>>
    fun getFavoriteStyles(): Flow<List<EnhancementStyle>>
    suspend fun toggleFavorite(styleId: String)
    fun isFavorite(styleId: String): Flow<Boolean>
}
