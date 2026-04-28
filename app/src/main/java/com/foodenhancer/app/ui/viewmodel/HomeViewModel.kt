package com.foodenhancer.app.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodenhancer.domain.model.FoodImage
import com.foodenhancer.domain.usecase.GetHistoryUseCase
import com.foodenhancer.domain.usecase.GetRemainingDemoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getRemainingDemoUseCase: GetRemainingDemoUseCase,
    getHistoryUseCase: GetHistoryUseCase
) : ViewModel() {

    val remainingDemo: StateFlow<Int> = getRemainingDemoUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5)

    val recentHistory: StateFlow<List<FoodImage>> = getHistoryUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _navigationEvent = MutableSharedFlow<String>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _isBatchMode = MutableStateFlow(false)
    val isBatchMode: StateFlow<Boolean> = _isBatchMode.asStateFlow()

    private val _batchPhotos = MutableStateFlow<List<String>>(emptyList())
    val batchPhotos: StateFlow<List<String>> = _batchPhotos.asStateFlow()

    fun toggleBatchMode() {
        _isBatchMode.value = !_isBatchMode.value
        if (!_isBatchMode.value) {
            _batchPhotos.value = emptyList()
        }
    }

    fun addBatchPhoto(uri: Uri) {
        val current = _batchPhotos.value
        if (current.size < 10) {
            _batchPhotos.value = current + uri.toString()
        }
    }

    fun removeBatchPhoto(index: Int) {
        val current = _batchPhotos.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _batchPhotos.value = current
        }
    }

    fun onImageSelected(uri: Uri) {
        viewModelScope.launch {
            val encoded = Uri.encode(uri.toString())
            _navigationEvent.emit(encoded)
        }
    }

    fun clearBatch() {
        _batchPhotos.value = emptyList()
    }
}
