package com.foodenhancer.data.repository

import com.foodenhancer.core_network.api.FoodEnhancerApi
import com.foodenhancer.domain.model.ProcessingResult
import com.foodenhancer.domain.repository.ImageEnhancerRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class ImageEnhancerRepositoryImpl @Inject constructor(
    private val api: FoodEnhancerApi
) : ImageEnhancerRepository {

    override suspend fun enhance(
        image: ByteArray,
        mask: ByteArray,
        styleId: String
    ): Result<ProcessingResult> {
        return try {
            // Prepare multipart parts (kept for future real API usage)
            val imagePart = MultipartBody.Part.createFormData(
                "image", "image.jpg",
                image.toRequestBody("image/jpeg".toMediaTypeOrNull())
            )
            val maskPart = MultipartBody.Part.createFormData(
                "mask", "mask.png",
                mask.toRequestBody("image/png".toMediaTypeOrNull())
            )
            val stylePart = styleId.toRequestBody("text/plain".toMediaTypeOrNull())

            // Stub: return placeholder instead of calling actual API
            Result.success(
                ProcessingResult(
                    resultUri = "https://placeholder.foodenhancer.com/result.jpg",
                    thumbnailUri = "https://placeholder.foodenhancer.com/thumb.jpg"
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
