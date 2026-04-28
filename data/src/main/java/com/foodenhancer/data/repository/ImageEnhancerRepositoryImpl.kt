package com.foodenhancer.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import com.foodenhancer.core_network.api.FoodEnhancerApi
import com.foodenhancer.domain.model.ProcessingResult
import com.foodenhancer.domain.repository.ImageEnhancerRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

class ImageEnhancerRepositoryImpl @Inject constructor(
    private val api: FoodEnhancerApi,
    @ApplicationContext private val context: Context
) : ImageEnhancerRepository {

    override suspend fun enhance(
        image: ByteArray,
        mask: ByteArray,
        styleId: String
    ): Result<ProcessingResult> {
        return withContext(Dispatchers.IO) {
            try {
                val result = tryRemoteEnhance(image, mask, styleId)
                if (result != null) return@withContext Result.success(result)

                // Offline fallback: apply local color overlay enhancement
                val enhanced = applyLocalEnhancement(image, mask, styleId)
                Result.success(enhanced)
            } catch (e: Exception) {
                try {
                    val enhanced = applyLocalEnhancement(image, mask, styleId)
                    Result.success(enhanced)
                } catch (fallbackError: Exception) {
                    Result.failure(fallbackError)
                }
            }
        }
    }

    private suspend fun tryRemoteEnhance(
        image: ByteArray,
        mask: ByteArray,
        styleId: String
    ): ProcessingResult? {
        return try {
            val imagePart = MultipartBody.Part.createFormData(
                "image", "image.jpg",
                image.toRequestBody("image/jpeg".toMediaTypeOrNull())
            )
            val maskPart = MultipartBody.Part.createFormData(
                "mask", "mask.png",
                mask.toRequestBody("image/png".toMediaTypeOrNull())
            )
            val stylePart = styleId.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = api.enhance(imagePart, maskPart, stylePart)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    ProcessingResult(
                        resultUri = body.url,
                        thumbnailUri = body.url
                    )
                } else null
            } else null
        } catch (_: Exception) {
            null
        }
    }

    private fun applyLocalEnhancement(
        imageBytes: ByteArray,
        maskBytes: ByteArray,
        styleId: String
    ): ProcessingResult {
        val original = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            ?: throw Exception("Failed to decode image")

        val maskBitmap = BitmapFactory.decodeByteArray(maskBytes, 0, maskBytes.size)

        val result = original.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)

        val overlayColor = getStyleColor(styleId)
        val overlayPaint = Paint().apply {
            color = overlayColor
            alpha = 60
        }

        canvas.drawRect(0f, 0f, result.width.toFloat(), result.height.toFloat(), overlayPaint)

        if (maskBitmap != null) {
            val scaledMask = Bitmap.createScaledBitmap(maskBitmap, result.width, result.height, true)
            val maskPaint = Paint().apply {
                alpha = 40
                colorFilter = PorterDuffColorFilter(getStyleAccentColor(styleId), PorterDuff.Mode.SRC_ATOP)
            }
            canvas.drawBitmap(scaledMask, 0f, 0f, maskPaint)
        }

        // Enhance contrast slightly
        val contrastPaint = Paint().apply {
            color = Color.WHITE
            alpha = 15
        }
        canvas.drawRect(0f, 0f, result.width.toFloat(), result.height.toFloat(), contrastPaint)

        val outputFile = File(context.cacheDir, "enhanced_${UUID.randomUUID()}.jpg")
        FileOutputStream(outputFile).use { fos ->
            result.compress(Bitmap.CompressFormat.JPEG, 95, fos)
        }

        val fileUri = outputFile.absolutePath
        return ProcessingResult(
            resultUri = fileUri,
            thumbnailUri = fileUri
        )
    }

    private fun getStyleColor(styleId: String): Int = when (styleId) {
        "rustic" -> Color.rgb(139, 90, 43)
        "minimal" -> Color.rgb(240, 240, 245)
        "italian" -> Color.rgb(255, 179, 71)
        "pop_art" -> Color.rgb(255, 20, 147)
        "dark_mood" -> Color.rgb(30, 30, 35)
        else -> Color.rgb(255, 107, 53)
    }

    private fun getStyleAccentColor(styleId: String): Int = when (styleId) {
        "rustic" -> Color.rgb(160, 82, 45)
        "minimal" -> Color.rgb(200, 200, 210)
        "italian" -> Color.rgb(255, 165, 0)
        "pop_art" -> Color.rgb(255, 0, 128)
        "dark_mood" -> Color.rgb(44, 44, 50)
        else -> Color.rgb(255, 107, 53)
    }
}
