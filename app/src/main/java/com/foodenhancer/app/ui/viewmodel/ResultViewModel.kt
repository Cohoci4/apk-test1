package com.foodenhancer.app.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodenhancer.domain.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    val originalUri: String = Uri.decode(savedStateHandle.get<String>("originalUri") ?: "")
    val processedUri: String = Uri.decode(savedStateHandle.get<String>("processedUri") ?: "")
    val styleName: String = Uri.decode(savedStateHandle.get<String>("styleName") ?: "")

    val isPro: StateFlow<Boolean> = subscriptionRepository.isPro()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
}
