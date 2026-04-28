package com.foodenhancer.app.ui.viewmodel

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodenhancer.domain.model.BatchEnhanceResult
import com.foodenhancer.domain.model.StyleCategory
import com.foodenhancer.domain.repository.StyleRepository
import com.foodenhancer.domain.usecase.BatchEnhanceUseCase
import com.foodenhancer.domain.usecase.BatchItem
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

data class BatchStyleUiState(
    val photoUris: List<String> = emptyList(),
    val isUniformMode: Boolean = true,
    val uniformStyleId: String? = null,
    val individualStyleIds: Map<Int, String> = emptyMap(),
    val isProcessing: Boolean = false,
    val progress: BatchEnhanceResult? = null,
    val resultUris: List<String> = emptyList(),
    val error: String? = null,
    val isComplete: Boolean = false
)

@HiltViewModel
class BatchStyleViewModel @Inject constructor(
    private val batchEnhanceUseCase: BatchEnhanceUseCase,
    private val styleRepository: StyleRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(BatchStyleUiState())
    val uiState: StateFlow<BatchStyleUiState> = _uiState.asStateFlow()

    val categoriesWithStyles: StateFlow<List<CategoryWithStyles>> =
        styleRepository.getCategories().let { categoriesFlow ->
            kotlinx.coroutines.flow.combine(
                categoriesFlow,
                styleRepository.getAllStyles()
            ) { categories, allStyles ->
                categories
                    .filter { it.id != "favorites" }
                    .sortedBy { it.displayOrder }
                    .mapNotNull { category ->
                        val styles = allStyles.filter { it.categoryId == category.id }
                        if (styles.isNotEmpty()) CategoryWithStyles(category, styles) else null
                    }
            }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        }

    fun setPhotos(uris: List<String>) {
        _uiState.value = _uiState.value.copy(photoUris = uris)
    }

    fun setUniformMode(uniform: Boolean) {
        _uiState.value = _uiState.value.copy(isUniformMode = uniform)
    }

    fun setUniformStyle(styleId: String) {
        _uiState.value = _uiState.value.copy(uniformStyleId = styleId)
    }

    fun setIndividualStyle(index: Int, styleId: String) {
        val current = _uiState.value.individualStyleIds.toMutableMap()
        current[index] = styleId
        _uiState.value = _uiState.value.copy(individualStyleIds = current)
    }

    fun enhanceAll() {
        val state = _uiState.value
        val photos = state.photoUris
        if (photos.isEmpty()) return

        _uiState.value = state.copy(isProcessing = true, error = null)

        viewModelScope.launch {
            try {
                val batchItems = photos.mapIndexed { index, uri ->
                    val styleId = if (state.isUniformMode) {
                        state.uniformStyleId ?: "minimal"
                    } else {
                        state.individualStyleIds[index] ?: "minimal"
                    }

                    val imageBytes = loadImageBytes(uri)
                        ?: throw Exception("Failed to load photo ${index + 1}")

                    BatchItem(
                        imageBytes = imageBytes,
                        imageUri = uri,
                        styleId = styleId
                    )
                }

                batchEnhanceUseCase(batchItems).collect { result ->
                    _uiState.value = _uiState.value.copy(
                        progress = result,
                        isProcessing = !result.isComplete,
                        isComplete = result.isComplete,
                        resultUris = result.processedUris,
                        error = result.errors.firstOrNull()
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = e.message ?: "Batch processing failed"
                )
            }
        }
    }

    private suspend fun loadImageBytes(uri: String): ByteArray? {
        return withContext(Dispatchers.IO) {
            try {
                val bitmap = when {
                    uri.startsWith("content://") -> {
                        context.contentResolver.openInputStream(Uri.parse(uri))?.use { stream ->
                            BitmapFactory.decodeStream(stream)
                        }
                    }
                    uri.startsWith("/") -> BitmapFactory.decodeFile(uri)
                    else -> {
                        context.contentResolver.openInputStream(Uri.parse(uri))?.use { stream ->
                            BitmapFactory.decodeStream(stream)
                        }
                    }
                } ?: return@withContext null

                val outputStream = ByteArrayOutputStream()
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, outputStream)
                bitmap.recycle()
                outputStream.toByteArray()
            } catch (_: Exception) {
                null
            }
        }
    }
}
