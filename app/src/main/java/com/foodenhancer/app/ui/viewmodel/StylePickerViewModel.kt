package com.foodenhancer.app.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodenhancer.domain.model.EnhancementStyle
import com.foodenhancer.domain.model.StyleCategory
import com.foodenhancer.domain.repository.StyleRepository
import com.foodenhancer.domain.usecase.EnhanceFoodImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

data class CategoryWithStyles(
    val category: StyleCategory,
    val styles: List<EnhancementStyle>
)

data class StylePickerUiState(
    val selectedStyleId: String? = null,
    val isLoading: Boolean = false,
    val resultOriginalUri: String? = null,
    val resultProcessedUri: String? = null,
    val resultStyleName: String? = null,
    val navigateToCrop: String? = null,
    val error: String? = null
)

@HiltViewModel
class StylePickerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val enhanceFoodImageUseCase: EnhanceFoodImageUseCase,
    private val styleRepository: StyleRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val imageUri: String = Uri.decode(savedStateHandle.get<String>("imageUri") ?: "")

    private val _uiState = MutableStateFlow(StylePickerUiState())
    val uiState: StateFlow<StylePickerUiState> = _uiState.asStateFlow()

    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())

    val categoriesWithStyles: StateFlow<List<CategoryWithStyles>> =
        combine(
            styleRepository.getCategories(),
            styleRepository.getAllStyles(),
            styleRepository.getFavoriteStyles()
        ) { categories, allStyles, favoriteStyles ->
            val result = mutableListOf<CategoryWithStyles>()

            if (favoriteStyles.isNotEmpty()) {
                val favCategory = categories.find { it.id == "favorites" }
                    ?: StyleCategory("favorites", "Favorites", 0)
                result.add(CategoryWithStyles(favCategory, favoriteStyles))
            }

            categories
                .filter { it.id != "favorites" }
                .sortedBy { it.displayOrder }
                .forEach { category ->
                    val styles = allStyles.filter { it.categoryId == category.id }
                    if (styles.isNotEmpty()) {
                        result.add(CategoryWithStyles(category, styles))
                    }
                }
            result
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    init {
        viewModelScope.launch {
            styleRepository.getFavoriteStyles().collect { favStyles ->
                _favoriteIds.value = favStyles.map { it.id }.toSet()
            }
        }
    }

    fun selectStyle(styleId: String) {
        _uiState.value = _uiState.value.copy(selectedStyleId = styleId)
    }

    fun toggleFavorite(styleId: String) {
        viewModelScope.launch {
            styleRepository.toggleFavorite(styleId)
        }
    }

    fun onNext() {
        val selectedId = _uiState.value.selectedStyleId ?: return
        _uiState.value = _uiState.value.copy(navigateToCrop = selectedId)
    }

    fun onCropNavigated() {
        _uiState.value = _uiState.value.copy(navigateToCrop = null)
    }

    fun onEnhance() {
        val selectedId = _uiState.value.selectedStyleId ?: return

        val allCategories = categoriesWithStyles.value
        val styleName = allCategories
            .flatMap { it.styles }
            .find { it.id == selectedId }?.name ?: return

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
                bitmap.recycle()
                outputStream.toByteArray()
            } catch (_: Exception) {
                null
            }
        }
    }
}
