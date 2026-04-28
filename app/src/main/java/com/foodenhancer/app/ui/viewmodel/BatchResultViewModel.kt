package com.foodenhancer.app.ui.viewmodel

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodenhancer.core.util.StorageHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

data class BatchResultUiState(
    val processedUris: List<String> = emptyList(),
    val selectedIndices: Set<Int> = emptySet(),
    val isSaving: Boolean = false,
    val saveComplete: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class BatchResultViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(BatchResultUiState())
    val uiState: StateFlow<BatchResultUiState> = _uiState.asStateFlow()

    fun setResults(uris: List<String>) {
        _uiState.value = _uiState.value.copy(processedUris = uris)
    }

    fun toggleSelection(index: Int) {
        val current = _uiState.value.selectedIndices.toMutableSet()
        if (index in current) current.remove(index) else current.add(index)
        _uiState.value = _uiState.value.copy(selectedIndices = current)
    }

    fun saveAll() {
        if (!StorageHelper.hasEnoughStorage()) {
            _uiState.value = _uiState.value.copy(error = context.getString(com.foodenhancer.app.R.string.error_insufficient_storage))
            return
        }
        val uris = _uiState.value.processedUris
        _uiState.value = _uiState.value.copy(isSaving = true)

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    uris.forEachIndexed { index, uri ->
                        saveToMediaStore(uri, "enhanced_batch_${index + 1}")
                    }
                }
                _uiState.value = _uiState.value.copy(isSaving = false, saveComplete = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Save failed"
                )
            }
        }
    }

    fun shareSelected() {
        val state = _uiState.value
        val indices = if (state.selectedIndices.isEmpty()) {
            state.processedUris.indices.toSet()
        } else {
            state.selectedIndices
        }

        val urisToShare = indices.mapNotNull { idx ->
            state.processedUris.getOrNull(idx)?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                } else null
            }
        }

        if (urisToShare.isNotEmpty()) {
            val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "image/jpeg"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(urisToShare))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Enhanced Photos").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }

    private fun saveToMediaStore(filePath: String, displayName: String) {
        val bitmap = BitmapFactory.decodeFile(filePath) ?: return

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$displayName.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/FoodEnhancer")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            resolver.openOutputStream(it)?.use { os ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 95, os)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(it, contentValues, null, null)
            }
        }
        bitmap.recycle()
    }
}
