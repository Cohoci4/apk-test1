package com.foodenhancer.data.repository

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.foodenhancer.core.util.WatermarkUtil
import com.foodenhancer.domain.repository.WatermarkApplier
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

class WatermarkApplierImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : WatermarkApplier {

    override suspend fun applyWatermark(imageUri: String): String {
        return withContext(Dispatchers.IO) {
            val bitmap = when {
                imageUri.startsWith("content://") -> {
                    val uri = Uri.parse(imageUri)
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    } ?: throw Exception("Failed to open content URI")
                }
                imageUri.startsWith("/") -> {
                    BitmapFactory.decodeFile(imageUri)
                        ?: throw Exception("Failed to decode file")
                }
                imageUri.startsWith("file:///android_asset/") -> {
                    val assetPath = imageUri.removePrefix("file:///android_asset/")
                    context.assets.open(assetPath).use { stream ->
                        BitmapFactory.decodeStream(stream)
                    } ?: throw Exception("Failed to decode asset")
                }
                else -> {
                    BitmapFactory.decodeFile(imageUri)
                        ?: throw Exception("Failed to decode image at $imageUri")
                }
            }

            val watermarked = WatermarkUtil.applyWatermark(bitmap)
            bitmap.recycle()
            val outputFile = File(context.cacheDir, "watermarked_${UUID.randomUUID()}.jpg")
            FileOutputStream(outputFile).use { fos ->
                watermarked.compress(android.graphics.Bitmap.CompressFormat.JPEG, 95, fos)
            }
            watermarked.recycle()
            outputFile.absolutePath
        }
    }
}
