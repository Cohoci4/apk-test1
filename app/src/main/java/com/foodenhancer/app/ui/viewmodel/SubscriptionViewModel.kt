package com.foodenhancer.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodenhancer.domain.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    val isPro: StateFlow<Boolean> = subscriptionRepository.isPro()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val remainingDemo: StateFlow<Int> = subscriptionRepository.getRemainingDemo()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5)
}
