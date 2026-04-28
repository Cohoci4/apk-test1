package com.foodenhancer.app.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodenhancer.domain.model.FoodImage
import com.foodenhancer.domain.usecase.GetHistoryUseCase
import com.foodenhancer.domain.usecase.GetRemainingDemoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    fun onImageSelected(uri: Uri) {
        viewModelScope.launch {
            val encoded = Uri.encode(uri.toString())
            _navigationEvent.emit(encoded)
        }
    }
}
