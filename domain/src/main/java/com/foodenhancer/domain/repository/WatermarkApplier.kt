package com.foodenhancer.domain.repository

interface WatermarkApplier {
    suspend fun applyWatermark(imageUri: String): String
}
