package com.foodenhancer.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodenhancer.domain.model.FoodImage
import com.foodenhancer.domain.repository.HistoryRepository
import com.foodenhancer.domain.usecase.GetHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    getHistoryUseCase: GetHistoryUseCase,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    val history: StateFlow<List<FoodImage>> = getHistoryUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteImage(id: String) {
        viewModelScope.launch {
            historyRepository.delete(id)
        }
    }
}
