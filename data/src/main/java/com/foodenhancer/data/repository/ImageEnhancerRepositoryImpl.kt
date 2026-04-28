package com.foodenhancer.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.foodenhancer.core.util.ImageCompositor
import com.foodenhancer.domain.model.ProcessingResult
import com.foodenhancer.domain.repository.ImageEnhancerRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

class ImageEnhancerRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ImageEnhancerRepository {

    override suspend fun enhance(
        image: ByteArray,
        mask: ByteArray,
        styleId: String
    ): Result<ProcessingResult> {
        return withContext(Dispatchers.Default) {
            try {
                val original = BitmapFactory.decodeByteArray(image, 0, image.size)
                    ?: return@withContext Result.failure(Exception("Failed to decode image"))

                val maskBitmap = BitmapFactory.decodeByteArray(mask, 0, mask.size)
                    ?: return@withContext Result.failure(Exception("Failed to decode mask"))

                val backgroundPath = getBackgroundPath(styleId)
                val backgroundBitmap = context.assets.open(backgroundPath).use { stream ->
                    BitmapFactory.decodeStream(stream)
                } ?: return@withContext Result.failure(Exception("Failed to load background"))

                val composited = ImageCompositor.composite(original, maskBitmap, backgroundBitmap)

                backgroundBitmap.recycle()

                val outputFile = File(context.cacheDir, "enhanced_${UUID.randomUUID()}.jpg")
                FileOutputStream(outputFile).use { fos ->
                    composited.compress(Bitmap.CompressFormat.JPEG, 95, fos)
                }
                composited.recycle()

                val fileUri = outputFile.absolutePath
                Result.success(ProcessingResult(resultUri = fileUri, thumbnailUri = fileUri))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun getBackgroundPath(styleId: String): String {
        val filename = when (styleId) {
            "rustic" -> "rustic.jpg"
            "minimal" -> "minimal.jpg"
            "italian" -> "italian.jpg"
            "pop_art" -> "popart.jpg"
            "dark_mood" -> "dark.jpg"
            else -> "minimal.jpg"
        }
        return "backgrounds/$filename"
    }
}
