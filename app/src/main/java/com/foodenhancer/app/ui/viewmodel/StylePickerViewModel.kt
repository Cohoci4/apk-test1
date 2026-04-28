package com.foodenhancer.app.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
import javax.inject.Inject

data class StyleItem(
    val id: String,
    val name: String,
    val description: String,
    val color: Long
)

data class StylePickerUiState(
    val selectedStyleId: String? = null,
    val isLoading: Boolean = false,
    val resultOriginalUri: String? = null,
    val resultProcessedUri: String? = null,
    val resultStyleName: String? = null,
    val error: String? = null
)

@HiltViewModel
class StylePickerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val enhanceFoodImageUseCase: EnhanceFoodImageUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val imageUri: String = Uri.decode(savedStateHandle.get<String>("imageUri") ?: "")

    private val _uiState = MutableStateFlow(StylePickerUiState())
    val uiState: StateFlow<StylePickerUiState> = _uiState.asStateFlow()

    val styles = listOf(
        StyleItem("rustic", "Rustic", "Wooden table", 0xFF8B4513),
        StyleItem("minimal", "Minimal", "Clean white", 0xFFE0E0E0),
        StyleItem("italian", "Italian Evening", "Candlelight", 0xFFFFB347),
        StyleItem("pop_art", "Pop Art", "Bright colors", 0xFFFF1493),
        StyleItem("dark_mood", "Dark Mood", "Gourmet dark", 0xFF2C2C2C)
    )

    fun selectStyle(styleId: String) {
        _uiState.value = _uiState.value.copy(selectedStyleId = styleId)
    }

    fun onEnhance() {
        val selectedId = _uiState.value.selectedStyleId ?: return
        val styleName = styles.find { it.id == selectedId }?.name ?: return

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val imageBytes = loadImageBytes(imageUri)
            if (imageBytes == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load image"
                )
                return@launch
            }

            val result = enhanceFoodImageUseCase(
                imageBytes = imageBytes,
                imageUri = imageUri,
                styleId = selectedId
            )

            result.fold(
                onSuccess = { processingResult ->
                    val encodedOriginal = Uri.encode(imageUri)
                    val encodedProcessed = Uri.encode(processingResult.resultUri)
                    val encodedStyle = Uri.encode(styleName)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        resultOriginalUri = encodedOriginal,
                        resultProcessedUri = encodedProcessed,
                        resultStyleName = encodedStyle
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Enhancement failed"
                    )
                }
            )
        }
    }

    private suspend fun loadImageBytes(uri: String): ByteArray? {
        return withContext(Dispatchers.IO) {
            try {
                val bitmap = when {
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
                    uri.startsWith("/") -> {
                        BitmapFactory.decodeFile(uri)
                    }
                    else -> {
                        val parsedUri = Uri.parse(uri)
                        context.contentResolver.openInputStream(parsedUri)?.use { stream ->
                            BitmapFactory.decodeStream(stream)
                        }
                    }
                } ?: return@withContext null

                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                outputStream.toByteArray()
            } catch (_: Exception) {
                null
            }
        }
    }
}
