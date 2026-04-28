package com.foodenhancer.domain.model

data class BatchEnhanceResult(
    val currentIndex: Int,
    val total: Int,
    val processedUris: List<String>,
    val errors: List<String>,
    val isComplete: Boolean
)
