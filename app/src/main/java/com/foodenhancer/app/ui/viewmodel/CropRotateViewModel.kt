package com.foodenhancer.app.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodenhancer.domain.usecase.EnhanceFoodImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

data class CropRotateUiState(
    val rotation: Float = 0f,
    val scale: Float = 1f,
    val offset: Offset = Offset.Zero,
    val isProcessing: Boolean = false,
    val resultOriginalUri: String? = null,
    val resultProcessedUri: String? = null,
    val resultStyleName: String? = null,
    val error: String? = null
)

@HiltViewModel
class CropRotateViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val enhanceFoodImageUseCase: EnhanceFoodImageUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val imageUri: String = Uri.decode(savedStateHandle.get<String>("imageUri") ?: "")
    val styleId: String = Uri.decode(savedStateHandle.get<String>("styleId") ?: "")

    private val _uiState = MutableStateFlow(CropRotateUiState())
    val uiState: StateFlow<CropRotateUiState> = _uiState.asStateFlow()

    fun updateTransform(rotation: Float, scale: Float, offset: Offset) {
        _uiState.value = _uiState.value.copy(
            rotation = rotation,
            scale = scale,
            offset = offset
        )
    }

    fun rotate90() {
        val current = _uiState.value
        _uiState.value = current.copy(rotation = current.rotation + 90f)
    }

    fun reset() {
        _uiState.value = CropRotateUiState()
    }

    fun applyAndEnhance() {
        _uiState.value = _uiState.value.copy(isProcessing = true, error = null)

        viewModelScope.launch {
            try {
                val croppedBytes = withContext(Dispatchers.Default) {
                    val bitmap = loadBitmap(imageUri)
                        ?: throw Exception("Failed to load image")

                    val state = _uiState.value

                    val matrix = Matrix()
                    matrix.postRotate(state.rotation, bitmap.width / 2f, bitmap.height / 2f)

                    val rotated = Bitmap.createBitmap(
                        bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                    )
                    if (rotated !== bitmap) bitmap.recycle()

                    val side = minOf(rotated.width, rotated.height)
                    val cropX = (rotated.width - side) / 2
                    val cropY = (rotated.height - side) / 2
                    val cropped = Bitmap.createBitmap(rotated, cropX, cropY, side, side)
                    if (cropped !== rotated) rotated.recycle()

                    val outputStream = ByteArrayOutputStream()
                    cropped.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                    cropped.recycle()

                    outputStream.toByteArray()
                }

                val croppedFile = withContext(Dispatchers.IO) {
                    val file = File(context.cacheDir, "cropped_${UUID.randomUUID()}.jpg")
                    FileOutputStream(file).use { fos -> fos.write(croppedBytes) }
                    file.absolutePath
                }

                val result = enhanceFoodImageUseCase(
                    imageBytes = croppedBytes,
                    imageUri = croppedFile,
                    styleId = styleId
                )

                result.fold(
                    onSuccess = { processingResult ->
                        _uiState.value = _uiState.value.copy(
                            isProcessing = false,
                            resultOriginalUri = Uri.encode(croppedFile),
                            resultProcessedUri = Uri.encode(processingResult.resultUri),
                            resultStyleName = Uri.encode(styleId)
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isProcessing = false,
                            error = error.message ?: "Enhancement failed"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = e.message ?: "Processing failed"
                )
            }
        }
    }

    private fun loadBitmap(uri: String): Bitmap? {
        return when {
            uri.startsWith("content://") -> {
                val parsedUri = Uri.parse(uri)
                context.contentResolver.openInputStream(parsedUri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }
            uri.startsWith("file:///android_asset/") -> {
                val assetPath = uri.removePrefix("file:///android_asset/")
                context.assets.open(assetPath).use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }
            uri.startsWith("/") -> BitmapFactory.decodeFile(uri)
            else -> {
                val parsedUri = Uri.parse(uri)
                context.contentResolver.openInputStream(parsedUri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }
        }
    }
}
