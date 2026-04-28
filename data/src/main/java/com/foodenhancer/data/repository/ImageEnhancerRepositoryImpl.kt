package com.foodenhancer.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.foodenhancer.core.util.ImageCompositor
import com.foodenhancer.domain.model.ProcessingResult
import com.foodenhancer.domain.repository.ImageEnhancerRepository
import com.google.gson.Gson
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

    private data class CatalogJson(val styles: List<StyleEntry>)
    private data class StyleEntry(val id: String, val backgroundImage: String)

    private val backgroundMap: Map<String, String> by lazy {
        try {
            val json = context.assets.open("styles_catalog.json").bufferedReader().use { it.readText() }
            val catalog = Gson().fromJson(json, CatalogJson::class.java)
            catalog.styles.associate { it.id to it.backgroundImage }
        } catch (_: Exception) {
            emptyMap()
        }
    }

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

                val backgroundPath = backgroundMap[styleId] ?: "backgrounds/minimal.jpg"
                val backgroundBitmap = context.assets.open(backgroundPath).use { stream ->
                    BitmapFactory.decodeStream(stream)
                } ?: return@withContext Result.failure(Exception("Failed to load background"))

                val composited = ImageCompositor.composite(original, maskBitmap, backgroundBitmap)

                original.recycle()
                maskBitmap.recycle()
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
}
