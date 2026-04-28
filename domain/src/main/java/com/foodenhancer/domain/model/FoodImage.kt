package com.foodenhancer.domain.model

data class FoodImage(
    val id: String,
    val originalUri: String,
    val processedUri: String?,
    val style: EnhancementStyle?,
    val createdAt: Long
)
