package com.foodenhancer.domain.usecase

import com.foodenhancer.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRemainingDemoUseCase @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) {
    operator fun invoke(): Flow<Int> = subscriptionRepository.getRemainingDemo()
}
