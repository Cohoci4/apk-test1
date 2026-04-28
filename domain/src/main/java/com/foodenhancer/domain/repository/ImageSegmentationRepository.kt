package com.foodenhancer.domain.repository

interface ImageSegmentationRepository {
    suspend fun segment(image: ByteArray): Result<ByteArray>
}
