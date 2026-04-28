package com.foodenhancer.data.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.foodenhancer.core_ml.FoodSegmenter
import com.foodenhancer.domain.repository.ImageSegmentationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class ImageSegmentationRepositoryImpl @Inject constructor(
    private val foodSegmenter: FoodSegmenter
) : ImageSegmentationRepository {

    override suspend fun segment(image: ByteArray): Result<ByteArray> {
        return withContext(Dispatchers.Default) {
            try {
                val bitmap = BitmapFactory.decodeByteArray(image, 0, image.size)
                    ?: return@withContext Result.failure(Exception("Failed to decode image"))

                val maskBitmap = foodSegmenter.segment(bitmap)
                bitmap.recycle()

                val outputStream = ByteArrayOutputStream()
                maskBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                maskBitmap.recycle()
                Result.success(outputStream.toByteArray())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
