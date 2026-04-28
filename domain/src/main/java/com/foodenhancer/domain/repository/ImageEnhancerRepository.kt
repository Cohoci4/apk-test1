package com.foodenhancer.domain.repository

import com.foodenhancer.domain.model.ProcessingResult

interface ImageEnhancerRepository {
    suspend fun enhance(image: ByteArray, mask: ByteArray, styleId: String): Result<ProcessingResult>
    suspend fun enhanceFallback(image: ByteArray, styleId: String): Result<ProcessingResult>
}
