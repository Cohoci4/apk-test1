package com.foodenhancer.data.repository

import com.foodenhancer.data.local.db.FoodImageDao
import com.foodenhancer.data.local.db.FoodImageEntity
import com.foodenhancer.domain.model.EnhancementStyle
import com.foodenhancer.domain.model.FoodImage
import com.foodenhancer.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HistoryRepositoryImpl @Inject constructor(
    private val dao: FoodImageDao
) : HistoryRepository {

    override suspend fun save(image: FoodImage) {
        dao.insert(
            FoodImageEntity(
                id = image.id,
                originalUri = image.originalUri,
                processedUri = image.processedUri,
                styleId = image.style?.id,
                styleName = image.style?.name,
                createdAt = image.createdAt
            )
        )
    }

    override fun getAll(): Flow<List<FoodImage>> {
        return dao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun delete(id: String) {
        dao.delete(id)
    }

    private fun FoodImageEntity.toDomain(): FoodImage {
        return FoodImage(
            id = id,
            originalUri = originalUri,
            processedUri = processedUri,
            style = if (styleId != null && styleName != null) {
                EnhancementStyle(id = styleId, name = styleName, previewUrl = "")
            } else {
                null
            },
            createdAt = createdAt
        )
    }
}
